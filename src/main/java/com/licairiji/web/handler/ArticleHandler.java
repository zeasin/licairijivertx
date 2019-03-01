package com.licairiji.web.handler;

import com.licairiji.web.entity.ArticleEntity;
import com.licairiji.web.entity.InvestLogEntity;
import com.licairiji.web.vo.ArticlePublishVo;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 文章Handler
 *
 * @author qlp
 * @date 2019-01-23 09:14
 */
public class ArticleHandler extends AbstractHandler {

    public ArticleHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    public void handleList(RoutingContext routingContext) {
        String sql = "SELECT * FROM article";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.query(sql, query -> {
                JsonObject jsonObject = new JsonObject();
                List<JsonObject> list = query.result().getRows();
                jsonObject.put("code", 0).put("msg", "OK").put("data", list);
                HttpServerResponse response = routingContext.response();
                response.setStatusCode(200);
                response.putHeader("Content-Type", "application/json");
                response.end(jsonObject.encode());
//                routingContext.put("welcome", "欢迎光临");
//
//                render(routingContext, "/article/publish");
            });

        });
    }

    /**
     * 发布文章GET
     *
     * @param routingContext
     */
    public void handlePublishGet(RoutingContext routingContext) {


        routingContext.put("welcome", "欢迎光临");

        render(routingContext, "/article/publish");
    }

    /**
     * 发布文章POST
     *
     * @param routingContext
     */
    public void handlePublishPost(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String title = request.getParam("title");
        String image = request.getParam("image");
        String content = request.getParam("content");

//        String content = routingContext.getBodyAsString();
        //返回内容
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        if (StringUtil.isNullOrEmpty(title)|| StringUtil.isNullOrEmpty(content)) {
            jsonObject.put("code", 1).put("msg", "标题和内容不能为空");
            response.end(jsonObject.encode());
        }
        ArticlePublishVo vo = new ArticlePublishVo();
//        try {
            vo.setTitle(title);
            vo.setContent(content);
            vo.setImage(image);
//            vo = Json.decodeValue(content, ArticlePublishVo.class);
//        } catch (Exception e) {
//            vo = null;
//        }
//        if (vo == null) {
//            jsonObject.put("code", 1).put("msg", "参数错误");
//            response.end(jsonObject.encode());
//        } else if (StringUtil.isNullOrEmpty(vo.getTitle()) || StringUtil.isNullOrEmpty(vo.getContent())) {
//            jsonObject.put("code", 1).put("msg", "参数错误");
//            response.end(jsonObject.encode());
//        }

        //加入数据库
//        Future<SQLConnection> sqlConnectionFuture = Future.future();
//
//        mySQLClient.getConnection(sqlConnectionFuture);


//        ArticlePublishVo finalVo = vo;
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();
                String sql = "INSERT INTO article (title,image,content,create_on) VALUE (?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(vo.getTitle());
                params.add(vo.getImage());
                params.add(vo.getContent());
                params.add(System.currentTimeMillis() / 1000);

                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        conn.close();
                        jsonObject.put("code", 0).put("msg", "");


                        response.end(jsonObject.encode());
                    }
                });
            } else {
                connection.cause().printStackTrace();
                System.err.println(connection.cause().getMessage());
            }
        });
//        sqlConnectionFuture.complete();


    }


    /**
     * 文章详情
     *
     * @param routingContext
     */
    public void handleDetailGet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String id = request.getParam("id");
        String sql = "SELECT * FROM article WHERE id=?";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            JsonArray params = new JsonArray();
            params.add(id);

            conn.querySingleWithParams(sql,params, query -> {
                JsonObject jsonObject = new JsonObject();
                JsonArray child = query.result();

                ArticleEntity entity = new ArticleEntity();
                entity.setId(child.getInteger(0));
                entity.setTitle(child.getString(1));
                entity.setImage(child.getString(2));
                entity.setContent(child.getString(3));
                entity.setCreateOn(child.getInteger(4));


                conn.close();
                routingContext.put("article", entity);

                render(routingContext, "/article/detail");
            });

        });

//        routingContext.put("welcome", "欢迎光临");


    }
}
