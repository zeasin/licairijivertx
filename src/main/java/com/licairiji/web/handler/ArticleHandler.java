package com.licairiji.web.handler;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.text.pdf.BaseFont;
import com.licairiji.web.entity.ArticleEntity;
import com.licairiji.web.entity.InvestLogEntity;
import com.licairiji.web.entity.StockEntity;
import com.licairiji.web.entity.TopicEntity;
import com.licairiji.web.utils.DownloadPdf;
import com.licairiji.web.utils.HTMLSpirit;
import com.licairiji.web.vo.ArticlePublishVo;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 文章Handler
 *
 * @author qlp
 * @date 2019-01-23 09:14
 */
public class ArticleHandler extends AbstractHandler {

    public ArticleHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    public void handleList(RoutingContext routingContext) {
        String sql = "SELECT * FROM article";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();

            conn.query(sql, query -> {
                JsonObject jsonObject = new JsonObject();
                List<JsonObject> list = query.result().getRows();
                List<ArticleEntity> articles = new ArrayList<>();
                for (JsonObject child : list) {
                    ArticleEntity entity = new ArticleEntity();
                    entity.setId(child.getInteger("id"));
                    entity.setTitle(child.getString("title"));
                    entity.setImage(child.getString("image"));
                    entity.setContent(child.getString("content"));
                    entity.setCreateOn(child.getInteger("create_on"));
                    entity.setTags(child.getString("tags"));
                    articles.add(entity);
                }
//                jsonObject.put("code", 0).put("msg", "OK").put("data", list);
//                HttpServerResponse response = routingContext.response();
//                response.setStatusCode(200);
//                response.putHeader("Content-Type", "application/json");
//                response.end(jsonObject.encode());

                routingContext.put("articles", articles);
//
                render(routingContext, "/article/list");
            });

        });
    }

    /**
     * 发布文章GET
     *
     * @param routingContext
     */
    public void handlePublishGet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String title = request.getParam("title");

        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();

                conn.query("SELECT * FROM tag", query1 -> {
                    List<JsonObject> list2 = query1.result().getRows();
                    List<TopicEntity> topics = new ArrayList<>();
                    for (JsonObject child : list2) {
                        TopicEntity entity2 = new TopicEntity();
                        entity2.setId(child.getInteger("id"));
                        entity2.setTitle(child.getString("title"));
                        entity2.setContent(child.getString("content"));
                        topics.add(entity2);
                    }


                    routingContext.put("title", title);
                    routingContext.put("tags", topics);

                    render(routingContext, "/article/publish");

                });

            }
        });

    }

    /**
     * 发布文章POST
     *
     * @param routingContext
     */
    public void handlePublishPost(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String title = request.getParam("title");
        String image = request.getParam("image");
        String content = request.getParam("content");
        String tags = request.getParam("tags");

//        String content = routingContext.getBodyAsString();
        //返回内容
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        if (StringUtil.isNullOrEmpty(title) || StringUtil.isNullOrEmpty(content)) {
            jsonObject.put("code", 1).put("msg", "标题和内容不能为空");
            response.end(jsonObject.encode());
        }
        ArticlePublishVo vo = new ArticlePublishVo();
//        try {
        vo.setTitle(title);
        vo.setContent(content);
        vo.setImage(image);
        vo.setTags(tags);
//            vo = Json.decodeValue(content, ArticlePublishVo.class);
//        } catch (Exception e) {
//            vo = null;
//        }
//        if (vo == null) {
//            jsonObject.put("code", 1).put("msg", "参数错误");
//            response.end(jsonObject.encode());
//        } else if (StringUtil.isNullOrEmpty(vo.getTitle()) || StringUtil.isNullOrEmpty(vo.getContent())) {
//            jsonObject.put("code", 1).put("msg", "参数错误");
//            response.end(jsonObject.encode());
//        }

        //加入数据库
//        Future<SQLConnection> sqlConnectionFuture = Future.future();
//
//        mySQLClient.getConnection(sqlConnectionFuture);


//        ArticlePublishVo finalVo = vo;
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();
                String sql = "INSERT INTO article (title,image,tags,content,create_on) VALUE (?,?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(vo.getTitle());
                params.add(vo.getImage());
                params.add(vo.getTags());
                params.add(vo.getContent());
                params.add(System.currentTimeMillis() / 1000);
                //返回自增id
                conn.setOptions(new SQLOptions().setAutoGeneratedKeys(true));

                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        JsonArray js = r.result().getKeys();
                        Integer articleId = js.getInteger(0);
                        //加入动态
                        String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on) VALUE (?,?,?,?,?,?,?,?,?)";
                        String dContent = HTMLSpirit.delHTMLTag(vo.getContent());
                        if (dContent.length() > 150) dContent = dContent.substring(0, 150);

                        JsonArray dParams = new JsonArray();
                        dParams.add(0);
                        dParams.add(vo.getTitle());
                        dParams.add(dContent);
                        dParams.add(vo.getImage());
                        dParams.add(vo.getTags());
                        dParams.add(1);
                        dParams.add(articleId);
                        dParams.add("/article/detail?id=" + articleId);
                        dParams.add(System.currentTimeMillis() / 1000);
                        conn.updateWithParams(dSql, dParams, r1 -> {
                        });

                        conn.close();
                        jsonObject.put("code", 0).put("msg", "");


                        response.end(jsonObject.encode());
                    }
                });
            } else {
                connection.cause().printStackTrace();
                System.err.println(connection.cause().getMessage());
            }
        });
//        sqlConnectionFuture.complete();


    }



    public void handleAddPDFGet(RoutingContext routingContext) {
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();

                conn.query("SELECT * FROM stock", query1 -> {
                    List<JsonObject> list2 = query1.result().getRows();
                    List<StockEntity> topics = new ArrayList<>();
                    for (JsonObject child : list2) {
                        StockEntity entity2 = new StockEntity();
                        entity2.setId(child.getInteger("id"));
                        entity2.setCode(child.getString("code"));
                        entity2.setName(child.getString("name"));
                        topics.add(entity2);
                    }
                    routingContext.put("stocks", topics);

                    render(routingContext, "/article/add_url");

                });

            }
        });
    }

    public byte[] convert(String html) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ConverterProperties props = new ConverterProperties();
        FontProvider fp = new FontProvider(); // 提供解析用的字体
        fp.addStandardPdfFonts(); // 添加标准字体库、无中文
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        String s= classLoader.getResource(File.separator).getPath();
        String s = ArticleHandler.class.getResource(File.separator).getPath();
        fp.addDirectory(s+"/fonts/"); // 自定义字体路径、解决中文,可先用绝对路径测试。
        props.setFontProvider(fp);
        // props.setBaseUri(baseResource); // 设置html资源的相对路径
        HtmlConverter.convertToPdf(html, outputStream, props); // 无法灵活设置页边距等
        byte[] result = outputStream.toByteArray();
        outputStream.close();
        return result;
    }
    /**
     * 发布文章POST
     *
     * @param routingContext
     */
    public void handleAddPDFPost(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String title = request.getParam("title");
        String content = request.getParam("content");
        String tags = request.getParam("tags");

//        String content = routingContext.getBodyAsString();
        //返回内容
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        if (StringUtil.isNullOrEmpty(title) || StringUtil.isNullOrEmpty(content)) {
            jsonObject.put("code", 1).put("msg", "标题和内容不能为空");
            response.end(jsonObject.encode());
        }

//        String[] info = new String[4];
        String  blogURL ="https://mp.weixin.qq.com/s?src=11&timestamp=1554135447&ver=1520&signature=vdGI2wKdwBnQ2IkZSYuc*Q01yQSI3NQCD97STrNT7imM1UqFHtnmdIENd3P5JqnQaRj22QxZXkhMAYQXoQ12qQ7yzsHN9J4Kvbwbn5UxcTLKG09zt1p33YXVIvPUVQYU&new=1";
//        org.jsoup.nodes.Document doc = null;
//        try {
////            doc = Jsoup.connect(blogURL).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").timeout(10000).get();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        org.jsoup.nodes.Element e_title = doc.select("title").first();
//        info[0] = e_title.text();


//        OutputStream os = new FileOutputStream(outputFile);
//        ITextRenderer renderer = new ITextRenderer();
//        InputStream inputStream = new ByteArrayInputStream(Jsoup.connect(url).get().html().getBytes());
//        String urlStr = IOUtils.toString(inputStream);
//        renderer.setDocumentFromString(urlStr);
//        try {
//            DownloadPdf.downLoadByUrl(blogURL,"abc.pdf",ArticleHandler.class.getResource(File.separator).getPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        OutputStream os = null;
        String b = ArticleHandler.class.getResource("").getPath();
//        getServletContext()
        String a = getClass().getResource(File.separator).getPath();
//        ClassLoaber.getResource()
//         null;
        try {
//            org.jsoup.nodes.Document doc = Jsoup.connect(blogURL).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").timeout(10000).get();
//            os = new FileOutputStream("/Users/qiliping/Project/licairijivertx/aaa.pdf");
//            ITextRenderer renderer = new ITextRenderer();
            InputStream inputStream = new ByteArrayInputStream(Jsoup.connect(blogURL).get().html().getBytes());
            String urlStr = IOUtils.toString(inputStream,"UTF-8");

            String html = "<p><span style=\"font-family: Microsoft YaHei;\">微软雅黑: 粗体前A<strong>A粗体A</strong>A粗体后</span></p>\n" +
                    "<p><span style=\"font-family: SimSun;\">宋体: 粗体前A<strong>A粗体A</strong>A粗体后</span></p>\n" +
                    "<p><span style=\"font-family: STHeiti;\">黑体: 粗体前A<strong>A粗体A</strong>A粗体后</span></p>" +
                    "<p><span style=\"font-family: Times New Roman;\">Times New Roman: pre bdA<strong>AbdA</strong>Aaft bd</span></p>\n";
            FileOutputStream fileOutputStream = new FileOutputStream(a+"/a.pdf");
            fileOutputStream.write(convert(urlStr));
            fileOutputStream.close();
//             convert(urlStr);
//            renderer.setDocumentFromString(urlStr);
//            renderer.setDocument((Document) doc,blogURL);

//            ITextRenderer render = new ITextRenderer();
//            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
//        // 解决中文支持问题
//            ITextFontResolver fontResolver = renderer.getFontResolver();
//            fontResolver.addFont(SystemConstant.local_dir + "/simsun.ttc", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            //解决图片的相对路径问题，绝对路径不需要写
//        renderer.getSharedContext().setBaseURL("file:/D:/");
//            renderer.layout();
//            try {
//                renderer.createPDF(os);
//            } catch (DocumentException e) {
//                e.printStackTrace();
//            }

//            os.flush();
//            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



        jsonObject.put("code", 0).put("msg", "SUCCESS");
        response.end(jsonObject.encode());
    }
    /**
     * 文章详情
     *
     * @param routingContext
     */
    public void handleDetailGet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("id");
        String sql = "SELECT id,title,tags,image,content,create_on FROM article WHERE id=?";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            JsonArray params = new JsonArray();
            params.add(id);

            conn.querySingleWithParams(sql, params, query -> {
                JsonObject jsonObject = new JsonObject();
                JsonArray child = query.result();

                ArticleEntity entity = new ArticleEntity();
                entity.setId(child.getInteger(0));

                entity.setTitle(child.getString(1));
                entity.setTags(child.getString(2));
                entity.setImage(child.getString(3));

                entity.setContent(child.getString(4));
                entity.setCreateOn(child.getInteger(5));


                conn.close();
                routingContext.put("article", entity);

                render(routingContext, "/article/detail");
            });

        });

//        routingContext.put("welcome", "欢迎光临");


    }
}
