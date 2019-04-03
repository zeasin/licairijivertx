package com.licairiji.web.handler;

import com.licairiji.web.entity.UserAccountEntity;
import com.licairiji.web.vo.UserAccountGetVo;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

/**
 * 描述：
 * 用户账户Handler
 *
 * @author qlp
 * @date 2019-04-03 17:39
 */
public class UserAccountHandler extends AbstractHandler {
    public UserAccountHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    /***
     * 获取用户资金账户
     * @param routingContext
     */
    public void handleGetAccount(RoutingContext routingContext) {
        String content = routingContext.getBodyAsString();
        //返回内容
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        if (StringUtil.isNullOrEmpty(content)) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        }

        UserAccountGetVo vo = null;
        try {
            vo = Json.decodeValue(content, UserAccountGetVo.class);
        } catch (Exception e) {
            vo = null;
        }

        if (vo == null) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        } else if (vo.getUserId() == null) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        }

        //查询用户账户
        String sql = "SELECT * FROM user_account WHERE id=?";
        UserAccountGetVo finalVo = vo;
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }

            SQLConnection conn = res.result();
            JsonArray params = new JsonArray();
            params.add(finalVo.getUserId());

            conn.querySingleWithParams(sql, params, query -> {
                if (query.result() != null) {
                    UserAccountEntity account = new UserAccountEntity();
                    JsonArray child = query.result();
                    account.setId(child.getInteger(0));
                    account.setUserName(child.getString(1));
                    account.setMobile(child.getString(2));
                    account.setCapitalTotal(child.getDouble(4));
                    account.setInvestAmount(child.getDouble(5));
                    account.setIncomeAmount(child.getDouble(6));
                    jsonObject.put("code", 0).put("msg", "SUCCESS");
                    jsonObject.put("data",JsonObject.mapFrom(account));
                    response.end(jsonObject.encode());
                }

            });
        });

    }
}
