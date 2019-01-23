package com.licairiji.web;

import com.licairiji.web.handler.ArticleHandler;
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
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

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
//
//        vertx.deployVerticle(new MyVerticle());
    }

    //资讯Handler
    private ArticleHandler articleHandler;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        //加载模版引擎
        templateEngine = ThymeleafTemplateEngine.create(vertx);
        // 定时模板解析器,表示从类加载路径下找模板
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // 设置模板的前缀，我们设置的是templates目录
        templateResolver.setPrefix("templates");
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
        router.get("/").handler(routingContext -> {
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

            JsonObject jsonObject = new JsonObject();

            JsonObject data = new JsonObject();
            data.put("src", "https://imgsa.baidu.com/news/q%3D100/sign=8b21d5584a10b912b9c1f2fef3fcfcb5/f636afc379310a55f5294c17ba4543a9832610c7.jpg");
            data.put("title", "");

            jsonObject.put("code", 0).put("msg", "").put("data", data);

//            jsonObject.put("success", true).put("pages", pages);

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.putHeader("Content-Type", "application/json");
            response.end(jsonObject.encode());
        });

        //上传文件
        router.post("/upload_word").handler(routingContext -> {
            JsonObject jsonObject = new JsonObject();

            JsonObject data = new JsonObject();
            data.put("src", "fff");
            data.put("title", "sadsdc");


            if (routingContext.fileUploads() == null || routingContext.fileUploads().isEmpty()) {
                jsonObject.put("code", 1).put("msg", "请上传文件").put("data", data);
            } else {

                jsonObject.put("code", 0).put("msg", "").put("data", data);
                FileUpload file = (FileUpload) routingContext.fileUploads().toArray()[0];
            }


//            jsonObject.put("success", true).put("pages", pages);

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.putHeader("Content-Type", "application/json");
            response.end(jsonObject.encode());
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

        router.get("/article/list").handler(articleHandler::handleList);
        router.get("/article/publish").handler(articleHandler::handlePublishGet);
        router.post("/article/publish").handler(articleHandler::handlePublishPost);

        //定义服务器
        HttpServer server = vertx.createHttpServer();

        //在服务器中加入路由router
        server.requestHandler(router);
        //启动
        server.listen(8080);

        System.out.println("服务器启动成功http://127.0.0.1:8080");
    }
}
