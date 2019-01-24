package com.licairiji.web.handler;

import io.vertx.core.Context;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-01-24 17:32
 */
public class HomeHandler extends AbstractHandler {
    public HomeHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    public void handleHome(RoutingContext routingContext) {
        routingContext.put("welcome", "欢迎光临");

        render(routingContext, "/home");
    }
}
