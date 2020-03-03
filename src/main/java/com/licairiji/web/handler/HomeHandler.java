package com.licairiji.web.handler;

import com.licairiji.web.entity.ArticleEntity;
import com.licairiji.web.entity.TopicEntity;
import com.licairiji.web.entity.UserDynamicEntity;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import io.vertx.reactivex.FlowableHelper;
import io.vertx.reactivex.ObservableHelper;
import io.vertx.reactivex.RxHelper;

import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;

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
        HttpServerRequest request = routingContext.request();

        Observable<Buffer> subscriber =Observable.create(m->{

        });

        var sql = "SELECT * FROM user_dynamic order by id desc limit 0,20;";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }

            SQLConnection conn = res.result();

            conn.query(sql, query -> {
                JsonObject jsonObject = new JsonObject();
                if(query.result()!=null) {
                    List<JsonObject> list = query.result().getRows();
                    List<UserDynamicEntity> dynamics = new ArrayList<>();
                    for (JsonObject child : list) {
                        UserDynamicEntity entity = new UserDynamicEntity();
                        entity.setId(child.getInteger("id"));
                        entity.setTitle(child.getString("title"));
                        entity.setType(child.getInteger("type"));
                        entity.setDataId(child.getString("data_id"));
                        entity.setUrl(child.getString("url"));
                        entity.setImgs(child.getString("imgs"));
                        entity.setTags(child.getString("tags"));
                        entity.setContent(child.getString("content"));
                        entity.setCreateOn(child.getInteger("create_on"));
                        entity.setNumSc(child.getInteger("num_sc"));
                        entity.setNumPing(child.getInteger("num_ping"));
                        entity.setNumZan(child.getInteger("num_zan"));
                        dynamics.add(entity);
                    }
                    routingContext.put("dynamics", dynamics);

                    //查询TAGS
                    conn.query("SELECT * FROM tag", query1 -> {
                        JsonObject jsonObject1 = new JsonObject();
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
                        render(routingContext, "/index");

                    });
                }



            });



        });


//        routingContext.put("welcome", "欢迎光临");
//
//        render(routingContext, "/index");
    }
}
