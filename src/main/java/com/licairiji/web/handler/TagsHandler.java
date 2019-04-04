package com.licairiji.web.handler;

import com.licairiji.web.entity.TopicEntity;
import com.licairiji.web.vo.StockListAjaxVo;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-04-04 13:24
 */
public class TagsHandler extends AbstractHandler {
    public TagsHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    /**
     * tags列表
     * @param routingContext
     */
    public void handleTagsAjax(RoutingContext routingContext) {
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        String sql = "SELECT title,content FROM  tag order by id desc";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            conn.query(sql, query -> {
//                List<JsonObject> list = query.result().getRows();

                List<TopicEntity> tags = new ArrayList<>();

                for (JsonObject child : query.result().getRows()) {
                    TopicEntity entity = new TopicEntity();
                    entity.setTitle(child.getString("title"));
                    entity.setContent(child.getString("content"));
                    tags.add(entity);
                }
                conn.close();
                jsonObject.put("code", 0).put("msg", "SUCCESS");
                jsonObject.put("data",tags);
                response.end(jsonObject.encode());
            });

        });
    }
}
