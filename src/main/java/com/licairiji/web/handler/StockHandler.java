package com.licairiji.web.handler;

import com.licairiji.web.DateUtil;
import com.licairiji.web.entity.StockEntity;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
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
 * 自选股Handler
 *
 * @author qlp
 * @date 2019-03-04 14:11
 */
public class StockHandler extends AbstractHandler {
    public StockHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    /**
     * 自选股列表
     *
     * @param routingContext
     */
    public void handleStockList(RoutingContext routingContext) {
        String sql = "SELECT s.*,i.count FROM  stock s left join invest i on i.code= s.code order by i.id desc";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.query(sql, query -> {
                List<JsonObject> list = query.result().getRows();

                List<StockEntity> investList = new ArrayList<>();

                for (JsonObject child : list) {
                    StockEntity entity = new StockEntity();
                    entity.setCode(child.getString("code"));
                    entity.setName(child.getString("name"));
                    entity.setId(child.getInteger("id"));
                    entity.setCreateOn(DateUtil.stampToDate(child.getLong("create_on")));
                    entity.setJoinPrice(child.getDouble("join_price"));
                    entity.setComment(child.getString("comment"));
                    entity.setEcode(child.getString("ecode"));
                    entity.setPlate(child.getString("plate"));
                    entity.setCount(child.getValue("count") == null ? 0 : child.getInteger("count"));
                    if (child.getString("board").equalsIgnoreCase("ZB"))
                        entity.setBoard("主板");
                    else if (child.getString("board").equalsIgnoreCase("ZSB"))
                        entity.setBoard("中小板");
                    else if (child.getString("board").equalsIgnoreCase("CYB"))
                        entity.setBoard("创业板");

                    investList.add(entity);
                }
                conn.close();
                routingContext.put("stock_list", investList);

                render(routingContext, "/stock/list");
            });

        });

    }

    public void handleStockAddGet(RoutingContext routingContext) {
//        routingContext.put("stock_list", investList);

        render(routingContext, "/stock/add");
    }

    public void handleStockAddPost(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String code = request.getParam("code");
        String name = request.getParam("name");
        String price = request.getParam("price");
        String plate = request.getParam("plate");
        String comment = request.getParam("comment");
        String board = "";
        String ecode = "";
        if (code.startsWith("600") || code.startsWith("601") || code.startsWith("603")) {
            ecode = "sh" + code;
            board = "ZB";
        } else if (code.startsWith("000") || code.startsWith("002") || code.startsWith("300")) {
            ecode = "sz" + code;
            if (code.startsWith("000")) board = "ZB";
            else if (code.startsWith("002")) board = "ZXB";
            else if (code.startsWith("300")) board = "CYB";
        }

        String finalEcode = ecode;
        String finalBoard = board;
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            //没有，新增
            String sql = "INSERT INTO stock (code,ecode,name,board,plate,join_price,comment,create_on) VALUE (?,?,?,?,?,?,?)";
            JsonArray params = new JsonArray();
            params.add(code);
            params.add(finalEcode);
            params.add(finalBoard);
            params.add(name);
            params.add(plate);
            params.add(price);
            params.add(comment);
            params.add(System.currentTimeMillis() / 1000);

            conn.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    HttpServerResponse response = routingContext.response();
                    response.setStatusCode(302);
                    response.headers().add("location", "/stock/list");
                    response.end();
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.setStatusCode(302);
                    response.headers().add("location", "/stock/add");
                    response.end();
                }
            });
        });


    }


}
