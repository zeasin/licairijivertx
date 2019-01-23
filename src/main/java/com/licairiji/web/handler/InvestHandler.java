package com.licairiji.web.handler;

import com.licairiji.web.DateUtil;
import com.licairiji.web.entity.InvestEntity;
import com.licairiji.web.entity.StockEntity;
import com.licairiji.web.vo.InvestAddVo;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 投资Handler
 *
 * @author qlp
 * @date 2019-01-23 16:41
 */
public class InvestHandler extends AbstractHandler {

    public InvestHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    /**
     * 添加投资
     *
     * @param routingContext
     */
    public void handleAddGet(RoutingContext routingContext) {
        String sql = "SELECT * FROM stock";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.query(sql, query -> {
//                JsonObject jsonObject = new JsonObject();
                List<JsonObject> list = query.result().getRows();

                List<StockEntity> stockList = new ArrayList<>();

                for (JsonObject child : list) {
                    StockEntity stock = new StockEntity();
                    stock.setCode(child.getString("code"));
                    stock.setName(child.getString("name"));
                    stockList.add(stock);
                }
//                jsonObject.put("code", 0).put("msg", "OK").put("data", list);
//                HttpServerResponse response = routingContext.response();
//                response.setStatusCode(200);
//                response.putHeader("Content-Type", "application/json");
//                response.end(jsonObject.encode());
                routingContext.put("stock_list", stockList);
//
                render(routingContext, "/invest/add");
            });

        });


    }


    /**
     * 添加投资记录POST
     *
     * @param routingContext
     */
    public void handleInvestAddPost(RoutingContext routingContext) {
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

        InvestAddVo vo = null;
        try {
            vo = Json.decodeValue(content, InvestAddVo.class);
        } catch (Exception e) {
            vo = null;
        }

        if (vo == null) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        } else if (StringUtil.isNullOrEmpty(vo.getCode()) || vo.getPrice_buy() <= 0 || vo.getPrice_cost() <= 0 || vo.getCount() <= 0 || StringUtil.isNullOrEmpty(vo.getTransaction_date())) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        }

        InvestAddVo finalVo = vo;
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();
                String sql = "INSERT INTO invest (code,type,price_buy,price_cost,transaction_time,sell_drop_rate,sell_up_rate,strategy,count,create_on) VALUE (?,?,?,?,?,?,?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(finalVo.getCode());
                params.add(finalVo.getTransaction_type());
                params.add(finalVo.getPrice_buy());
                params.add(finalVo.getPrice_cost());
                params.add(DateUtil.dateToStamp(finalVo.getTransaction_date()));
                params.add(finalVo.getSell_drop_rate());
                params.add(finalVo.getSell_up_rate());
                params.add(finalVo.getStrategy());
                params.add(finalVo.getCount());
                params.add(System.currentTimeMillis() / 1000);


                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        Integer investId = r.result().getKeys().getInteger(0);
                        String logSQL = "INSERT INTO invest_log (invest_id,type,price,comment,create_on) VALUE (?,?,?,?,?)";
                        JsonArray logParams = new JsonArray();
                        logParams.add(investId);
                        logParams.add(finalVo.getTransaction_type());
                        logParams.add(finalVo.getPrice_buy());
                        logParams.add("");
                        logParams.add(System.currentTimeMillis() / 1000);
                        conn.updateWithParams(logSQL, logParams, r1 -> {
                            if (r1.succeeded()) {
                                conn.close();
                            }
                        });

                        jsonObject.put("code", 0).put("msg", "");

                        response.end(jsonObject.encode());
                    }
                });
            } else {
                connection.cause().printStackTrace();
                System.err.println(connection.cause().getMessage());
            }
        });
    }

    public void handleInvestList(RoutingContext routingContext) {
        String sql = "SELECT invest.*,stock.name FROM invest left join stock on stock.code = invest.code ";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.query(sql, query -> {
                List<JsonObject> list = query.result().getRows();

                List<InvestEntity> investList = new ArrayList<>();

                for (JsonObject child : list) {
                    InvestEntity entity = new InvestEntity();
                    entity.setCode(child.getString("code"));
                    entity.setName(child.getString("name"));
                    entity.setId(child.getInteger("id"));
                    entity.setCount(child.getInteger("count"));
                    entity.setCreate_on(child.getInteger("create_on"));
                    entity.setPrice_buy(child.getDouble("price_buy"));
                    entity.setPrice_cost(child.getDouble("price_cost"));
                    entity.setSell_drop_rate(child.getDouble("sell_drop_rate"));
                    entity.setSell_up_rate(child.getDouble("sell_up_rate"));
                    entity.setStrategy(child.getString("strategy"));
                    entity.setTransaction_time(child.getInteger("transaction_time"));
                    entity.setType(child.getInteger("type"));
                    entity.setTransactionTime(DateUtil.stampToDate(child.getLong("transaction_time")));

                    investList.add(entity);
                }

                routingContext.put("invest_list", investList);

                render(routingContext, "/invest/list");
            });

        });

    }
}
