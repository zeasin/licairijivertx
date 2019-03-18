package com.licairiji.web;

import com.google.gson.Gson;
import com.licairiji.web.handler.*;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

/**
 * Hello world!
 */
public class MyVerticle extends AbstractVerticle {
    //Template Engine
    private ThymeleafTemplateEngine templateEngine;
    private SQLClient mySQLClient;//sqlclient

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Verticle myVerticle = new MyVerticle();

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(myVerticle);
    }

    //资讯Handler
    private ArticleHandler articleHandler;
    //投资Handler
    private InvestHandler investHandler;
    private HomeHandler homeHandler;
    private StockHandler stockHandler;
    private StockAnalyseHandler analyseHandler;
    private TopicHandler topicHandler;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        //加载模版引擎
        templateEngine = ThymeleafTemplateEngine.create(vertx);
        // 定时模板解析器,表示从类加载路径下找模板
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // 设置模板的前缀，我们设置的是templates目录
        templateResolver.setPrefix("templates/");
        // 设置后缀为.html文件
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);


        context.put("TemplateEngine", templateEngine);

        JsonObject dbConfig = new JsonObject();
        //配置数据库连接
//        dbConfig.put("username", "root")
//                .put("password", "qbz@123")
//                .put("host", "192.168.1.177")
//                .put("port",3306)
//                .put("database", "huayi");
        mySQLClient = JDBCClient.create(vertx, DataSourceHelper.dataSource());
//        mySQLClient = MySQLClient.createShared(vertx, dbConfig);

        //定义handler
        articleHandler = new ArticleHandler(context, templateEngine, mySQLClient);
        investHandler = new InvestHandler(context, templateEngine, mySQLClient);
        homeHandler = new HomeHandler(context, templateEngine, mySQLClient);
        stockHandler = new StockHandler(context, templateEngine, mySQLClient);
        analyseHandler = new StockAnalyseHandler(context, templateEngine, mySQLClient);
        topicHandler = new TopicHandler(context, templateEngine, mySQLClient);
    }


    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        // Enable multipart form data parsing
        router.route().handler(BodyHandler.create().setUploadsDirectory(System.getProperty("java.io.tmpdir")));
        // 配置静态文件
        router.route("/static/*").handler(StaticHandler.create("static"));

        router.route().handler(routingContext -> {
//            routingContext.put("engine", engine);
            routingContext.next();
        });



        //定义路由
        router.get("/hello").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");
            response.end("Hello");
        });

        //定义REST路由
        router.get("/json").handler(routingContext -> {
            JsonObject jsonObject = new JsonObject();
            List<JsonObject> pages = new ArrayList<>();
            pages.add(new JsonObject().put("id", 122).put("name", "andy"));

            jsonObject.put("success", true).put("pages", pages);

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.putHeader("Content-Type", "application/json");
            response.end(jsonObject.encode());
        });

        //上传图片
        router.post("/upload_image").handler(routingContext -> {

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.putHeader("Content-Type", "application/json");
            JsonObject jsonObject = new JsonObject();
            JsonObject data = new JsonObject();

            if (routingContext.fileUploads() == null || routingContext.fileUploads().isEmpty()) {
                jsonObject.put("code", 1).put("msg", "请上传文件").put("data", data);
                response.end(jsonObject.encode());
                return;
            }


            FileUpload file = (FileUpload) routingContext.fileUploads().toArray()[0];
            String fileSuffix = file.fileName().substring(file.fileName().lastIndexOf(".") + 1, file.fileName().length());

            ArrayList<String> a = new ArrayList<>();
            a.add("gif");
            a.add("jpg");
            a.add("jpeg");
            a.add("png");

            //上传文件到七牛云
            if (a.contains(fileSuffix) == false) {
                jsonObject.put("code", 400).put("msg", "不支持的格式").put("data", data);
                response.end(jsonObject.encode());
                return;
            }

            //...生成上传凭证，然后准备上传
            String accessKey = "sDfL7yNlSy16MqL7vh6M_UaPgKscCbiti5GWZJmu";
            String secretKey = "utM-_huV78h7GaWWsxKDl97P5EFK5jmb0ba-3HIG";
            String bucket = "licairiji";
            Auth auth = Auth.create(accessKey, secretKey);

            String upToken = auth.uploadToken(bucket);//上传的凭据

            //构造一个带指定Zone对象的配置类
            Configuration cfg = new Configuration(Zone.zone2());

            UploadManager uploadManager = new UploadManager(cfg);
            //默认不指定key的情况下，以文件内容的hash值作为文件名
            String key = null;

            try {
                File uploadedFile = new File(file.uploadedFileName());

                Response uploadResponse = uploadManager.put(uploadedFile, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(uploadResponse.bodyString(), DefaultPutRet.class);
//                System.out.println(putRet.key);
//                System.out.println(putRet.hash);
                data.put("src", "http://licai.xiguanapp.com/" + putRet.key);
                data.put("title", "");

                jsonObject.put("code", 0).put("msg", "").put("data", data);
                response.end(jsonObject.encode());

            } catch (QiniuException ex) {
                Response r = ex.response;

                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                    jsonObject.put("code", 500).put("msg", r.bodyString());
                    response.end(jsonObject.encode());
                } catch (QiniuException ex2) {
                    //ignore
                    jsonObject.put("code", 500).put("msg", "图片上传错误");
                    response.end(jsonObject.encode());
                }
            } catch (IOException e) {
                e.printStackTrace();
                jsonObject.put("code", 400).put("msg", "图片格式错误");
                response.end(jsonObject.encode());
//                return new ApiResult<>(400, "图片格式错误");
            }


//            jsonObject.put("code", 0).put("msg", "").put("data", data);

//            jsonObject.put("success", true).put("pages", pages);


//            response.end(jsonObject.encode());
        });

        //上传文件
        router.post("/upload_word").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.putHeader("Content-Type", "application/json");

            JsonObject jsonObject = new JsonObject();

            JsonObject data = new JsonObject();


            if (routingContext.fileUploads() == null || routingContext.fileUploads().isEmpty()) {
                jsonObject.put("code", 1).put("msg", "请上传文件").put("data", data);
                response.end(jsonObject.encode());
                return;
            }


            FileUpload file = (FileUpload) routingContext.fileUploads().toArray()[0];
            String fileSuffix = file.fileName().substring(file.fileName().lastIndexOf(".") + 1, file.fileName().length());

            if (fileSuffix.equals("md")) {
                //markdown
                File uploadedFile = new File(file.uploadedFileName());
                BufferedReader buf = null;
                String line = null;
                StringBuilder sb = new StringBuilder();
                try {
                    buf = new BufferedReader(new InputStreamReader(new FileInputStream(uploadedFile), "utf-8"));

                    while ((line = buf.readLine()) != null) {
//                    line = line.trim(); //去处空格
                        System.out.println(line);
                        sb.append(line);
//                        sb.append("<br />");
                        sb.append(System.getProperty("line.separator"));
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (sb != null && StringUtil.isNullOrEmpty(sb.toString()) == false) {
                    PegDownProcessor md = new PegDownProcessor(Extensions.ALL_WITH_OPTIONALS);
                    String content = md.markdownToHtml(sb.toString());
                    data.put("src", "");
                    data.put("title", content);
                    jsonObject.put("code", 0).put("msg", "").put("data", data);
                    response.end(jsonObject.encode());
                }
            } else if (fileSuffix.equals("txt")) {
                //文本
//                File uploadedFile = new File(file.uploadedFileName());
//                BufferedReader buf = null;
//                String line = null;
//                StringBuilder sb = new StringBuilder();
//                try {
//                    buf = new BufferedReader(new InputStreamReader(new FileInputStream(uploadedFile), "utf-8"));
//
//                    while ((line = buf.readLine()) != null) {
////                    line = line.trim(); //去处空格
//                        System.out.println(line);
//                        sb.append(line);
//                        sb.append("<br />");
//                        sb.append(System.getProperty("line.separator"));
//                    }
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                String charset = "utf-8";
                File uploadedFile = new File(file.uploadedFileName());
                long fileByteLength = uploadedFile.length();
                byte[] content = new byte[(int)fileByteLength];
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(uploadedFile);
                    fileInputStream.read(content);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String str = null;
                try {
                    str = new String(content,charset);
                    System.out.println(str);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                data.put("src", "");
                data.put("title", str != null ? str.replace("\n","<br />") : "");
                jsonObject.put("code", 0).put("msg", "").put("data", data);
                response.end(jsonObject.encode());
            }else if ( fileSuffix.equals("doc")) {


//                InputStream is = null;
                File uploadedFile = new File(file.uploadedFileName());
                String buffer = "";
                try {

                    InputStream is = new FileInputStream(uploadedFile);
                    WordExtractor ex = new WordExtractor(is);
                    buffer = ex.getText();
                    ex.close();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



//                WordExtractor extractor = new WordExtractor(is);


//                String content = readWord(file.uploadedFileName());
                data.put("src", "");
                data.put("title", buffer.replace("\n","<br />"));
                jsonObject.put("code", 0).put("msg", "").put("data", data);
                response.end(jsonObject.encode());
            }else if ( fileSuffix.equals("docx")) {
                File uploadedFile = new File(file.uploadedFileName());
                String text = "";
                try {

//                    OPCPackage opcPackage = POIXMLDocument.openPackage(path);
//                    POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
//                    buffer = extractor.getText();
//                    extractor.close();

                    InputStream is = new FileInputStream(uploadedFile);
                    XWPFDocument doc = new XWPFDocument(is);
                    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                    text = extractor.getText();
                    System.out.println(text);
                    POIXMLProperties.CoreProperties coreProps = extractor.getCoreProperties();
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



//                WordExtractor extractor = new WordExtractor(is);


//                String content = readWord(file.uploadedFileName());
                data.put("src", "");
                data.put("title", text.replace("\n","<br />"));
                jsonObject.put("code", 0).put("msg", "").put("data", data);
                response.end(jsonObject.encode());

            }else{
                jsonObject.put("code", 400).put("msg", "不支持的格式，仅支持doc，docx，md，txt格式");
                response.end(jsonObject.encode());
            }


        });

        //定义thymeleaf模版引擎路由
        router.get("/html").handler(routingContext -> {
            routingContext.put("welcome", "hello world,page好人医生");
            JsonObject jsonObject = new JsonObject();


            templateEngine.render(routingContext.data(), "/index", res -> {
                if (res.succeeded()) {
                    routingContext.response().end(res.result());
                } else {
                    routingContext.fail(res.cause());
                }
            });
        });

        router.get("/").handler(homeHandler::handleHome);

        //文章模块
        router.get("/article/list").handler(articleHandler::handleList);
        router.get("/article/publish").handler(articleHandler::handlePublishGet);
        router.post("/article/publish").handler(articleHandler::handlePublishPost);
        router.get("/article/detail").handler(articleHandler::handleDetailGet);

        //投资模块
        router.get("/invest/add").handler(investHandler::handleAddGet);
        router.get("/invest/list").handler(investHandler::handleInvestList);
        router.post("/invest/add_post").handler(investHandler::handleInvestAddPost);
//        router.post("/invest/add_log_post").handler(investHandler::handleInvestLogAdd);
//        router.get("/invest/log_list").handler(investHandler::handleInvestLogList);

        //自选股
        router.get("/stock/list").handler(stockHandler::handleStockList);
        router.get("/stock/detail/:code").handler(stockHandler::handleStockDetail);
        router.get("/stock/kdata/:code").handler(stockHandler::handleStockKData);
        router.get("/stock/kdata2/:code").handler(stockHandler::handleStockKData2);
        router.get("/stock/add").handler(stockHandler::handleStockAddGet);
        router.post("/stock/add").handler(stockHandler::handleStockAddPost);

        //话题
        router.get("/topic").handler(topicHandler::handleTopic);
        router.get("/topic/:topicId").handler(topicHandler::handleTopic);


        //股票分析
        router.get("/stock/analyse").handler(analyseHandler::handleStockAnalyse);
//        analyseHandler

        //定义服务器
        HttpServer server = vertx.createHttpServer();

        //在服务器中加入路由router
        server.requestHandler(router);
        //启动
        server.listen(80);

        System.out.println("服务器启动成功http://127.0.0.1");
    }


//    public String readWord(String path) {
//        String buffer = "";
//        try {
//            if (path.endsWith(".doc")) {
//                InputStream is = new FileInputStream(new File(path));
//                WordExtractor ex = new WordExtractor(is);
//                buffer = ex.getText();
//                ex.close();
//            } else if (path.endsWith("docx")) {
//                OPCPackage opcPackage = POIXMLDocument.openPackage(path);
//                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
//                buffer = extractor.getText();
//                extractor.close();
//            } else {
//                System.out.println("此文件不是word文件！");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return buffer;
//    }

}
