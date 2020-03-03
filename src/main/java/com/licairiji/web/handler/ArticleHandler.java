package com.licairiji.web.handler;

import com.google.gson.Gson;
import com.licairiji.web.entity.ArticleEntity;
import com.licairiji.web.entity.TopicEntity;
import com.licairiji.web.utils.HTMLSpirit;
import com.licairiji.web.utils.HtmlToPdfInterceptor;
import com.licairiji.web.vo.ArticlePublishVo;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
        String tagStr = "";
        String codeStr = "";//request.getParam("code");
        String plateStr = "";//request.getParam("plate");

        if (StringUtil.isNullOrEmpty(tags) == false) {
            String[] tagsArr = tags.split(",");
            //分拆 S代码股票 T代码tags P代表板块
            for (String t : tagsArr) {
                if (t.startsWith("S")) {
                    codeStr += t.substring(1, 7) + ",";
                } else if (t.startsWith("T")) {
                    tagStr = t.replace("T", "");
                }
                else if (t.startsWith("P")) {
                    plateStr += t.replace("P", "") + ",";
                }
            }
        }

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
        vo.setTags(tagStr);
        vo.setPlate(plateStr);
        vo.setStock(codeStr);
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
                String sql = "INSERT INTO article (title,image,tags,content,create_on,plate,stock_code) VALUE (?,?,?,?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(vo.getTitle());
                params.add(vo.getImage());
                params.add(vo.getTags());
                params.add(vo.getContent());
                params.add(System.currentTimeMillis() / 1000);
                params.add(vo.getPlate());
                params.add(vo.getStock());
                //返回自增id
                conn.setOptions(new SQLOptions().setAutoGeneratedKeys(true));

                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        JsonArray js = r.result().getKeys();
                        Integer articleId = js.getInteger(0);
                        //加入动态
                        String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on,num_sc,num_zan,num_ping) VALUE (?,?,?,?,?,?,?,?,?,0,0,0)";
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

    /**
     * 添加url链接，并保存pdf
     * @param routingContext
     */
    public void handleAddPDFGet(RoutingContext routingContext) {
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

                    routingContext.put("tags", topics);

                    render(routingContext, "/article/add_url");

                });

            }
        });
    }


    /**
     * 发布文章POST
     *
     * @param routingContext
     */
    public void handleAddPDFPost(RoutingContext routingContext) {
//        Future<> future = Future.future();
        HttpServerRequest request = routingContext.request();
        String url = request.getParam("url");
        String tag = request.getParam("tag");
        ArrayList<String> stocks = new ArrayList<>();
        ArrayList<String> plates = new ArrayList<>();
        String tagStr = "";
        String codeStr = "";//request.getParam("code");
        String plateStr = "";//request.getParam("plate");

        if (StringUtil.isNullOrEmpty(tag) == false) {
            String[] tags = tag.split(",");
            //分拆 S代码股票 T代码tags P代表板块
            for (String t : tags) {
                if (t.startsWith("S")) {
                    stocks.add(t.substring(1, 7));
                    codeStr += t.substring(1, 7) + ",";
                } else if (t.startsWith("T")) {
                    tagStr = t.replace("T", "");
                }
                else if (t.startsWith("P")) {
                    plates.add(t.replace("P", ""));
                    plateStr += t.replace("P", "") + ",";
                }
            }
        }

//        String htmlUrl = "";//资讯html 地址
//        String pdfUrl = "";//资讯pdf 地址
        String title = "";//文章标题
        String content ="";//文章内容
        String htmlFilePath = "";//html路径
        String pdfFilePath = "";//pdf路径

//        String content = routingContext.getBodyAsString();
        //返回内容
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        if (StringUtil.isNullOrEmpty(url)) {
            jsonObject.put("code", 1).put("msg", "url不能为空");
            response.end(jsonObject.encode());
        }


        String sysPath = "";
        String rootPath = "";
        try {
            //获取系统
            String osName = System.getProperty("os.name");
            //读取配置文件
            Properties properties = new Properties();
            //当前项目根目录
//            rootPath = System.getProperty("user.dir");

            //获取类运行的根目录
            sysPath = ArticleHandler.class.getResource("/").getPath();
            InputStream in = ArticleHandler.class.getResourceAsStream("/config.properties");

            properties.load(in);
            in.close();

            rootPath =  properties.getProperty("pdfPath");

            if(osName.indexOf("Windows")>-1){
                sysPath = sysPath.substring(1,sysPath.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        /***********开始获取url内容***********/
        try {
            //去除标题特殊字符
            String regEX = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";

            //利用jsoup获取url内容
            org.jsoup.nodes.Document doc = null;
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").timeout(10000).get();

            //获取整个html内容
//            InputStream htmlInputStream = new ByteArrayInputStream(doc.html().getBytes());
            if (url.startsWith("https://mp.weixin.qq.com")) {
//                String htmlStr = IOUtils.toString(htmlInputStream, "UTF-8");
                //微信公众号文章标题
                title = doc.select(".rich_media_title").text();
                title = title.replace(" ", "");
                title = title.replaceAll(regEX,"");
                //微信公众号文章内容
                content = doc.select(".rich_media_wrp").text();
                content = content.replace("data-src=\"", "src=\"");

                pdfFilePath = sysPath + rootPath + "/" + title + ".pdf";

                pdfGenerate(url,pdfFilePath);
                //保存到项目文件夹
//                pdfGenerate(url,pdfFilePath);
                url = "/" +rootPath +"/"+ title + ".pdf";
            } else if (url.startsWith("https://xuangubao.cn")) {
                //文章来自于选股宝
                title = doc.title();
                title = title.replace(" | 选股宝 - 选题材抓龙头，就用选股宝","");
                title = title.replaceAll(regEX,"");

                content = doc.select(".article-content").html();
//                doc.body().html(content);
                pdfFilePath = sysPath+"/"+rootPath + "/" + title + ".pdf";
                pdfGenerate(url,pdfFilePath);
            }else if (url.startsWith("http://stock.10jqka.com.cn")) {

                //文章来自于同花顺
                title = doc.title();
                title = title.replaceAll(regEX,"");
                title = title.replace(" | 选股宝 - 选题材抓龙头，就用选股宝","");
                 content = doc.select(".main-text").html();
//                doc.body().html(content);
//                doc.head().remove();
                pdfFilePath = sysPath+"/"+rootPath + "/" + title + ".pdf";
                pdfGenerate(url,pdfFilePath);
            }
            else {
                title = doc.title();
                title = title.replaceAll(regEX,"");
                jsonObject.put("code", 500).put("msg", "暂时不支持该网站文章提取"+url);
                response.end(jsonObject.encode());
            }

            //保存数据库
            saveSQLData(title,"",tagStr,content,url,htmlFilePath,pdfFilePath,codeStr,plateStr);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 500).put("msg", e.getMessage());
            response.end(jsonObject.encode());
        }


        jsonObject.put("code", 0).put("msg", "SUCCESS");
        response.end(jsonObject.encode());
    }

    /**
     * 保存数据库
     * @param title 文章标题
     * @param image 文章图片
     * @param tag 文章标签
     * @param content 文章内容
     * @param url 文章url
     * @param htmlFilePath html保存路径
     * @param pdfFilePath pdf保存路径
     * @param stockCode 股票代码
     * @param plate 相关板块
     */
    private void saveSQLData(String title,String image,String tag,String content,String url,String htmlFilePath,String pdfFilePath,String stockCode,String plate){
        //写入数据库
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();
                String sql = "INSERT INTO article (title,image,tags,content,create_on,html_path,html_url,pdf_path,pdf_url,stock_code,plate) VALUE (?,?,?,?,?,?,?,?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(title);
                params.add(image);
                params.add(tag);
                params.add(content);
                params.add(System.currentTimeMillis() / 1000);
                params.add(htmlFilePath);
                params.add(url);
                params.add(pdfFilePath);
                params.add(pdfFilePath);
                params.add(stockCode);
                params.add(plate);
                //返回自增id
                conn.setOptions(new SQLOptions().setAutoGeneratedKeys(true));

                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        JsonArray js = r.result().getKeys();
                        Integer articleId = js.getInteger(0);
                        String dcontent = title;
                        if (StringUtil.isNullOrEmpty(stockCode) == false) {
                            dcontent += "【相关股票：" + stockCode + "】";
                        }

                        if (StringUtil.isNullOrEmpty(plate) == false) {
                            dcontent += "【板块：" + plate + "】";
                        }

                        //加入动态
                        String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on,num_sc,num_zan,num_ping) VALUE (?,?,?,?,?,?,?,?,?,0,0,0)";

                        JsonArray dParams = new JsonArray();
                        dParams.add(1);
                        dParams.add(title);
                        dParams.add(dcontent);
                        dParams.add("");
                        dParams.add(tag);
                        dParams.add(1);
                        dParams.add(articleId);
                        dParams.add(url);
                        dParams.add(System.currentTimeMillis() / 1000);
                        conn.updateWithParams(dSql, dParams, r1 -> {
                            String s = "";
                        });

                        conn.close();
                    }else{
                        System.out.println("MariDB数据保存错误："+r.cause().getMessage());
                        conn.close();
                    }
                });
            } else {
                connection.cause().printStackTrace();
                System.err.println(connection.cause().getMessage());
            }
        });
    }
    /**
     * 生成PDF
     * @param url
     * @param pdfFilePath
     * @throws IOException
     * @throws InterruptedException
     */
    private void pdfGenerate(String url,String pdfFilePath) throws IOException, InterruptedException {
        //生产pdf
        String command = String.format("wkhtmltopdf %s %s", url, pdfFilePath);

        Process proc = Runtime.getRuntime().exec(command);
        HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
        HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
        error.start();
        output.start();
        proc.waitFor();
        System.out.println("生成PDF完成，"+pdfFilePath);
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

    private String stringTextCut(String s)
    {

        String afterString = s;

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (String.valueOf(chars[i]).getBytes().length < 2)
            {
                continue;
            }
            else
            {
                afterString = afterString.replaceAll(String.valueOf(chars[i]), "");
            }
        }


        return afterString;
    }

}
