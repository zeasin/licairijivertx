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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
                } else if (t.startsWith("T")) tagStr += t.replace("T", "") + ",";
                else if (t.startsWith("P")) {
                    plates.add(t.replace("P", ""));
                    plateStr += t.replace("P", "") + ",";
                }
            }
        }

        String htmlUrl = "";//资讯html 地址
        String pdfUrl = "";//资讯pdf 地址
        String title = "";//文章标题
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

        String rootPath = getClass().getResource(File.separator).getPath().replace("classes/","").replace("target/","");
        String filePath = rootPath + "static/yanbao/";
        try {
            org.jsoup.nodes.Document doc = null;
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").timeout(10000).get();

            if (url.startsWith("https://mp.weixin.qq.com")) {
                InputStream inputStream = new ByteArrayInputStream(doc.html().getBytes());
                String urlStr = IOUtils.toString(inputStream, "UTF-8");
                //微信公众号文章
                title = doc.select(".rich_media_title").text();
                title = title.replace(" ", "");
                if (StringUtil.isNullOrEmpty(title) == false) {
                    //Jsoup.connect(url).get().html().getBytes()
                    urlStr = urlStr.replace("data-src=\"", "src=\"");
                    htmlFilePath = filePath + title + ".html";
                    pdfFilePath = filePath + title + ".pdf";
                    //生成html
                    FileOutputStream fileOutputStream = new FileOutputStream(htmlFilePath);
                    fileOutputStream.write(urlStr.getBytes("UTF-8"));
                    fileOutputStream.close();

                    //获取系统
                    String osName = System.getProperty("os.name");
                    String command = String.format("wkhtmltopdf %s %s", htmlFilePath, pdfFilePath);


//                    Process process = Runtime.getRuntime().exec(command);
//
//                    new Thread(new ClearBufferThread(process.getInputStream())).start();
//                    new Thread(new ClearBufferThread(process.getErrorStream())).start();
//                    process.waitFor();
                    Process proc = Runtime.getRuntime().exec(command);
                    HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
                    HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
                    error.start();
                    output.start();
                    proc.waitFor();
                }
            } else if (url.startsWith("https://xuangubao.cn")) {
                //文章来自于选股宝
                title = doc.select(".article-meta-title").text();
                title = title.replace(" ", "");
                String content = doc.select(".article").html();
                String body = doc.select("body").html();
                doc.select("body").html(content);

                InputStream inputStream = new ByteArrayInputStream(doc.html().getBytes());
                String urlStr = IOUtils.toString(inputStream, "UTF-8");


                urlStr = urlStr.replace(doc.select("body").html(), content);

                if (StringUtil.isNullOrEmpty(title) == false) {
                    htmlFilePath = filePath + title + ".html";
                    pdfFilePath = filePath + title + ".pdf";
                    //生成html
                    FileOutputStream fileOutputStream = new FileOutputStream(htmlFilePath);
                    fileOutputStream.write(urlStr.getBytes("UTF-8"));
                    fileOutputStream.close();
                    //获取系统
                    String osName = System.getProperty("os.name");
                    String command = String.format("wkhtmltopdf %s %s", htmlFilePath, pdfFilePath);

                    Process proc = Runtime.getRuntime().exec(command);
                    HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
                    HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
                    error.start();
                    output.start();
                    proc.waitFor();
                }

            } else {
                jsonObject.put("code", 500).put("msg", "暂时只支持微信公众号和选股宝文章");
                response.end(jsonObject.encode());
            }

            //...生成上传凭证，然后准备上传
            String accessKey = "sDfL7yNlSy16MqL7vh6M_UaPgKscCbiti5GWZJmu";
            String secretKey = "utM-_huV78h7GaWWsxKDl97P5EFK5jmb0ba-3HIG";
            String bucket = "licairiji";
            String imgDomain = "http://licai.xiguanapp.com/";
            Auth auth = Auth.create(accessKey, secretKey);

            String upToken = auth.uploadToken(bucket);//上传的凭据

            //构造一个带指定Zone对象的配置类
            Configuration cfg = new Configuration(Zone.zone2());

            UploadManager uploadManager = new UploadManager(cfg);
            //默认不指定key的情况下，以文件内容的hash值作为文件名
//                    String key = null;
            //将html上传到七牛云
            if (StringUtil.isNullOrEmpty(htmlFilePath) == false) {

                try {
                    Response qiniu_response = uploadManager.put(htmlFilePath, null, upToken);
                    //解析上传成功的结果
                    DefaultPutRet putRet = new Gson().fromJson(qiniu_response.bodyString(), DefaultPutRet.class);
                    System.out.println(putRet.key);
                    System.out.println(putRet.hash);
                    htmlUrl = imgDomain + putRet.key;
//                    UploadImageResult uploadImageResult = new UploadImageResult();
//                    uploadImageResult.setSrc(imgDomain + putRet.key);


                } catch (QiniuException ex) {
                    Response r = ex.response;

                    System.err.println(r.toString());
                }

            }

            //将pdf上传到七牛云
            if (StringUtil.isNullOrEmpty(pdfFilePath) == false) {

                try {
                    Response qiniu_response = uploadManager.put(pdfFilePath, null, upToken);
                    //解析上传成功的结果
                    DefaultPutRet putRet = new Gson().fromJson(qiniu_response.bodyString(), DefaultPutRet.class);
                    System.out.println(putRet.key);
                    System.out.println(putRet.hash);
                    pdfUrl = imgDomain + putRet.key;
//                    UploadImageResult uploadImageResult = new UploadImageResult();
//                    uploadImageResult.setSrc(imgDomain + putRet.key);


                } catch (QiniuException ex) {
                    Response r = ex.response;

                    System.err.println(r.toString());
                }

            }

            //写入数据库
            String finalTitle = title;
            String finalHtmlFilePath = htmlFilePath;
            String finalHtmlUrl = htmlUrl;
            String finalPdfFilePath = pdfFilePath;
            String finalPdfUrl = pdfUrl;
            String finalCodeStr = codeStr;
            String finalPlateStr = plateStr;
            mySQLClient.getConnection(connection -> {
                if (connection.succeeded()) {
                    SQLConnection conn = connection.result();
                    String sql = "INSERT INTO article (title,image,tags,content,create_on,html_path,html_url,pdf_path,pdf_url,stock_code,plate) VALUE (?,?,?,?,?,?,?,?,?,?,?)";
                    JsonArray params = new JsonArray();
                    params.add(finalTitle);
                    params.add("");
                    params.add(tag);
                    params.add("");
                    params.add(System.currentTimeMillis() / 1000);
                    params.add(finalHtmlFilePath);
                    params.add(finalHtmlUrl);
                    params.add(finalPdfFilePath);
                    params.add(finalPdfUrl);
                    params.add(finalCodeStr);
                    params.add(finalPlateStr);
                    //返回自增id
                    conn.setOptions(new SQLOptions().setAutoGeneratedKeys(true));

                    conn.updateWithParams(sql, params, r -> {
                        if (r.succeeded()) {
                            JsonArray js = r.result().getKeys();
                            Integer articleId = js.getInteger(0);
                            String dcontent = finalTitle;
                            if (StringUtil.isNullOrEmpty(finalCodeStr) == false) {
                                dcontent += "【相关股票：" + finalCodeStr + "】";
                            }

                            if (StringUtil.isNullOrEmpty(finalPlateStr) == false) {
                                dcontent += "【板块：" + finalPlateStr + "】";
                            }

                            //加入动态
                            String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on,num_sc,num_zan,num_ping) VALUE (?,?,?,?,?,?,?,?,?,0,0,0)";

                            JsonArray dParams = new JsonArray();
                            dParams.add(1);
                            dParams.add(finalTitle);
                            dParams.add(dcontent);
                            dParams.add("");
                            dParams.add(tag);
                            dParams.add(1);
                            dParams.add(articleId);
                            dParams.add(finalPdfUrl);
                            dParams.add(System.currentTimeMillis() / 1000);
                            conn.updateWithParams(dSql, dParams, r1 -> {
                                String s = "";
                            });

                            conn.close();
                        }
                    });
                } else {
                    connection.cause().printStackTrace();
                    System.err.println(connection.cause().getMessage());
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 500).put("msg", e.getMessage());
            response.end(jsonObject.encode());
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
