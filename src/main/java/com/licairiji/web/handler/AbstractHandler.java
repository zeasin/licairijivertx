package com.licairiji.web.handler;

import io.vertx.core.Context;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

public abstract class AbstractHandler {

    protected ThymeleafTemplateEngine templateEngine;
    protected SQLClient mySQLClient;

    public AbstractHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
//        this.templateEngine = context.get("TemplateEngine");
        this.templateEngine = templateEngine;
        this.mySQLClient = mySQLClient;
    }

    /**
     * @param routingContext
     * @param templateFileName 模版文件名
     */
    protected void render(RoutingContext routingContext, String templateFileName) {
        templateEngine.render(routingContext.data(), templateFileName, res -> {
            if (res.succeeded()) {
                routingContext.response().end(res.result());
            } else {
                routingContext.fail(res.cause());
            }
        });
    }

    protected void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
