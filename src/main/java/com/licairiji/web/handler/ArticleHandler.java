package com.licairiji.web.handler;

import com.licairiji.web.entity.ArticleEntity;
import com.licairiji.web.entity.InvestLogEntity;
import com.licairiji.web.utils.HTMLSpirit;
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
import io.vertx.ext.sql.SQLOptions;
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
                List<ArticleEntity> articles = new ArrayList<>();
                for (JsonObject child : list) {
                    ArticleEntity entity = new ArticleEntity();
                    entity.setId(child.getInteger("id"));
                    entity.setTitle(child.getString("title"));
                    entity.setImage(child.getString("image"));
                    entity.setContent(child.getString("content"));
                    entity.setCreateOn(child.getInteger("create_on"));
                    entity.setTags(child.getString("tags"));
                    articles.add(entity);
                }
//                jsonObject.put("code", 0).put("msg", "OK").put("data", list);
//                HttpServerResponse response = routingContext.response();
//                response.setStatusCode(200);
//                response.putHeader("Content-Type", "application/json");
//                response.end(jsonObject.encode());

                routingContext.put("articles", articles);
//
                render(routingContext, "/article/list");
            });

        });
    }

    /**
     * 发布文章GET
     *
     * @param routingContext
     */
    public void handlePublishGet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String title = request.getParam("title");

        routingContext.put("title", title);

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
        String tags = request.getParam("tags");

//        String content = routingContext.getBodyAsString();
        //返回内容
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        if (StringUtil.isNullOrEmpty(title) || StringUtil.isNullOrEmpty(content)) {
            jsonObject.put("code", 1).put("msg", "标题和内容不能为空");
            response.end(jsonObject.encode());
        }
        ArticlePublishVo vo = new ArticlePublishVo();
//        try {
        vo.setTitle(title);
        vo.setContent(content);
        vo.setImage(image);
        vo.setTags(tags);
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
                String sql = "INSERT INTO article (title,image,tags,content,create_on) VALUE (?,?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(vo.getTitle());
                params.add(vo.getImage());
                params.add(vo.getTags());
                params.add(vo.getContent());
                params.add(System.currentTimeMillis() / 1000);
                //返回自增id
                conn.setOptions(new SQLOptions().setAutoGeneratedKeys(true));

                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        JsonArray js = r.result().getKeys();
                        Integer articleId = js.getInteger(0);
                        //加入动态
                        String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on) VALUE (?,?,?,?,?,?,?,?,?)";
                        String dContent = HTMLSpirit.delHTMLTag(vo.getContent());
                        if (dContent.length() > 150) dContent = dContent.substring(0, 150);

                        JsonArray dParams = new JsonArray();
                        dParams.add(0);
                        dParams.add(vo.getTitle());
                        dParams.add(dContent);
                        dParams.add(vo.getImage());
                        dParams.add(vo.getTags());
                        dParams.add(1);
                        dParams.add(articleId);
                        dParams.add("/article/detail?id="+articleId);
                        dParams.add(System.currentTimeMillis() / 1000);
                        conn.updateWithParams(dSql, dParams, r1 -> {
                        });

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
        String sql = "SELECT id,title,tags,image,content,create_on FROM article WHERE id=?";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            JsonArray params = new JsonArray();
            params.add(id);

            conn.querySingleWithParams(sql, params, query -> {
                JsonObject jsonObject = new JsonObject();
                JsonArray child = query.result();

                ArticleEntity entity = new ArticleEntity();
                entity.setId(child.getInteger(0));

                entity.setTitle(child.getString(1));
                entity.setTags(child.getString(2));
                entity.setImage(child.getString(3));

                entity.setContent(child.getString(4));
                entity.setCreateOn(child.getInteger(5));


                conn.close();
                routingContext.put("article", entity);

                render(routingContext, "/article/detail");
            });

        });

//        routingContext.put("welcome", "欢迎光临");


    }
}
