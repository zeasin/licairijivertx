package com.licairiji.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
//import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class MyVerticle extends AbstractVerticle {
    //Template Engine
    private ThymeleafTemplateEngine templateEngine;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Verticle myVerticle = new MyVerticle();


        Vertx vertx = Vertx.vertx();


        vertx.deployVerticle(myVerticle);
//
//        vertx.deployVerticle(new MyVerticle());
    }

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
        templateResolver.setTemplateMode("HTML5");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);


        context.put("TemplateEngine", templateEngine);
    }


    @Override
    public void start() throws Exception {


        Router router = Router.router(vertx);



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
            pages.add(new JsonObject().put("id",122).put("name","andy"));

            jsonObject.put("success", true).put("pages", pages);

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.putHeader("Content-Type", "application/json");
            response.end(jsonObject.encode());
        });

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

        //定义服务器
        HttpServer server = vertx.createHttpServer();

        //在服务器中加入路由router
        server.requestHandler(router);
        //启动
        server.listen(8080);

        System.out.println("服务器启动成功http://127.0.0.1:8080");
    }
}
