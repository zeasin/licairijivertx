package com.licairiji.web.handler;

import com.licairiji.web.entity.PlateEntity;
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
 * @date 2019-04-04 13:59
 */
public class CommonAjaxHanlder extends AbstractHandler  {
    public CommonAjaxHanlder(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    public void handleTagsStocksPlatesAjax(RoutingContext routingContext) {
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        String sql = "SELECT title FROM tag order by id desc;SELECT code,name FROM stock;SELECT title FROM plate;";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            conn.query(sql, query -> {
                List<JsonObject> tags = query.result().getRows();
                List<JsonObject> stocks = query.result().getNext().getRows();
                List<JsonObject> plates = query.result().getNext().getNext().getRows();

                List<TopicEntity> topics = new ArrayList<>();

                for (JsonObject child : tags) {
                    TopicEntity entity = new TopicEntity();
                    entity.setTitle(child.getString("title"));
                    topics.add(entity);
                }

                List<StockListAjaxVo> stockList = new ArrayList<>();
                for (JsonObject child1 : stocks) {
                    StockListAjaxVo entity = new StockListAjaxVo();
                    entity.setCode(child1.getString("code"));
                    entity.setName(child1.getString("name"));
                    stockList.add(entity);
                }

                List<PlateEntity> plateList = new ArrayList<>();
                for (JsonObject child2 : plates) {
                    PlateEntity entity = new PlateEntity();
                    entity.setTitle(child2.getString("title"));
                    plateList.add(entity);
                }

                conn.close();
                jsonObject.put("code", 0).put("msg", "SUCCESS");
                jsonObject.put("tag",topics);
                jsonObject.put("stock",stockList);
                jsonObject.put("plate",plateList);
                response.end(jsonObject.encode());
            });

        });
    }

}
