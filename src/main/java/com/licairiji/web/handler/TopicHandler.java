package com.licairiji.web.handler;

import com.licairiji.web.entity.TopicEntity;
import com.licairiji.web.entity.UserDynamicEntity;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: licairiji-vertx
 * @description: 话题
 * @author: Mr.Qi
 * @create: 2019-03-15 14:27
 **/
public class TopicHandler extends AbstractHandler {
    public TopicHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    public void handleTopic(RoutingContext routingContext) {

        HttpServerRequest request = routingContext.request();
        String tag = request.getParam("topicId");
        String sql = "SELECT * FROM user_dynamic order by id desc limit 0,20 ";
        JsonArray params = new JsonArray();
//        String tagSql = "SELECT * FROM tag";
        if(StringUtils.isEmpty(tag)==false){
            sql = "SELECT * FROM user_dynamic WHERE tags = ? order by id desc limit 0,20";
            params.add(tag);
            routingContext.put("topicTitle", tag);
        }


        String finalSql = sql;
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();



            conn.queryWithParams(finalSql,params, query -> {
                if(query.result() !=null) {

                    List<JsonObject> list = query.result().getRows();

                    List<UserDynamicEntity> dynamics = new ArrayList<>();
                    for (JsonObject child : list) {
                        UserDynamicEntity entity = new UserDynamicEntity();
                        entity.setId(child.getInteger("id"));
                        entity.setTitle(child.getString("title"));
                        entity.setImgs(child.getString("imgs"));
                        entity.setTags(child.getString("tags"));
                        entity.setType(child.getInteger("type"));
                        entity.setDataId(child.getString("data_id"));
                        entity.setUrl(child.getString("url"));
                        entity.setContent(child.getString("content"));
                        entity.setCreateOn(child.getInteger("create_on"));
                        entity.setNumSc(child.getInteger("num_sc"));
                        entity.setNumPing(child.getInteger("num_ping"));
                        entity.setNumZan(child.getInteger("num_zan"));
                        dynamics.add(entity);
                    }
                    routingContext.put("dynamics", dynamics);
                }

                conn.query("SELECT * FROM tag",q->{
                    List<TopicEntity> tags = new ArrayList<>();
                    List<JsonObject> list1 = q.result().getRows();
                    for (JsonObject child1 : list1) {
                        TopicEntity entity = new TopicEntity();
                        entity.setId(child1.getInteger("id"));
                        entity.setTitle(child1.getString("title"));
                        entity.setContent(child1.getString("content"));
                        entity.setCreateOn(child1.getInteger("create_on"));
                        tags.add(entity);
                    }

                    routingContext.put("tags", tags);
                    render(routingContext, "/topic");

                });

//                JsonObject jsonObject = new JsonObject();



            });

        });
    }
}
