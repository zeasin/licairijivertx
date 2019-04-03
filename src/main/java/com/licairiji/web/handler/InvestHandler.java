package com.licairiji.web.handler;

import com.licairiji.web.DateUtil;
import com.licairiji.web.entity.InvestEntity;
import com.licairiji.web.entity.InvestLogEntity;
import com.licairiji.web.entity.StockEntity;
import com.licairiji.web.vo.InvestAddVo;
import com.licairiji.web.vo.InvestLogAddVo;
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
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
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
     * 买入股票
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
                render(routingContext, "/invest/buy");
            });

        });


    }


    /**
     * 买入股票POST
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
        } else if (StringUtil.isNullOrEmpty(vo.getCode()) || vo.getPrice() <= 0 || vo.getCount() <= 0 || StringUtil.isNullOrEmpty(vo.getTransaction_date())) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        }

        InvestAddVo finalVo = vo;
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();


                //查询股票
                conn.querySingleWithParams("SELECT quantity,avg_price FROM stock WHERE code=?", new JsonArray().add(finalVo.getCode()), query -> {
                    if (query.result() != null) {
                        StockEntity stock = new StockEntity();
                        stock.setQuantity(query.result().getInteger(0));
                        stock.setAvgPrice(query.result().getDouble(1));

                        //计算均价 =(购买数量*购买价格 + 已够数量*已购价格)总额 / (购买数量+已购数量)总数量
                        Double avgPrice = (finalVo.getPrice() * finalVo.getCount() + stock.getQuantity() * stock.getAvgPrice()) / (finalVo.getCount() + stock.getQuantity());

                        //添加投资记录
                        String sql = "INSERT INTO stock_trade (code,type,price,transaction_time,strategy,comment,count,create_on,amount) VALUE (?,?,?,?,?,?,?,?,?)";
                        JsonArray params = new JsonArray();
                        params.add(finalVo.getCode());
                        params.add(1);//买入
                        params.add(finalVo.getPrice());
                        params.add(DateUtil.dateToStamp(finalVo.getTransaction_date()));
                        params.add(finalVo.getStrategy());
                        params.add(finalVo.getComment());
                        params.add(finalVo.getCount());
                        params.add(System.currentTimeMillis() / 1000);
                        params.add(finalVo.getAmount());

                        conn.updateWithParams(sql, params, r -> {
                            if (r.succeeded()) {
                                //更新stock 自选股持股数量
                                JsonArray upParams = new JsonArray();
                                upParams.add(finalVo.getCount());
                                upParams.add(avgPrice);
                                upParams.add(System.currentTimeMillis() / 1000);
                                upParams.add(finalVo.getCode());
                                conn.updateWithParams("UPDATE stock SET quantity=quantity+?,avg_price=?,last_trade_time=? WHERE code=?", upParams, r1 -> {
                                });

                                //添加用户动态
                                String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on) VALUE (?,?,?,?,?,?,?,?,?)";
                                JsonArray dParams = new JsonArray();
                                dParams.add(1);
//                        if (finalVo.getTransaction_type() == 1) {
                                dParams.add("买入股票【" + finalVo.getCode() + "】");
                                dParams.add("买入股票" + finalVo.getCode() + "价格：￥" + finalVo.getPrice() + "，数量：" + finalVo.getCount() + "，交易时间：" + finalVo.getTransaction_date() + "，设定策略：" + finalVo.getStrategy() + "  " + finalVo.getComment());
//                        } else {
//                            dParams.add("卖出股票【" + finalVo.getCode() + "】");
//                            dParams.add("卖出股票" + finalVo.getCode() + "价格：￥" + finalVo.getPrice() + "，数量：" + finalVo.getCount() + "，交易时间：" + finalVo.getTransaction_date() + "，备注：" + finalVo.getComment());
//                        }
                                dParams.add("");
                                dParams.add("交易");
                                dParams.add(2);
                                dParams.add(finalVo.getCode());
                                dParams.add("/stock/detail/" + finalVo.getCode());
                                dParams.add(System.currentTimeMillis() / 1000);
                                conn.updateWithParams(dSql, dParams, r1 -> {
                                });

                                //更新资金账户的投资金额
                                JsonArray accountParams = new JsonArray();
                                accountParams.add(finalVo.getAmount());
                                accountParams.add(1);
                                conn.updateWithParams("UPDATE user_account SET invest_amount=invest_amount+? WHERE id=?", accountParams, r3 -> {
                                });

                                jsonObject.put("code", 0).put("msg", "SUCCESS");

                                response.end(jsonObject.encode());
                            }
                        });
                    }else{
                        connection.cause().printStackTrace();
                        System.err.println(connection.cause().getMessage());
                        jsonObject.put("code", 404).put("msg", "没找到股票信息");
                        response.end(jsonObject.encode());
                    }
                });


            } else {
                connection.cause().printStackTrace();
                System.err.println(connection.cause().getMessage());
            }
        });
    }


    /**
     * 卖出股票
     *
     * @param routingContext
     */
    public void handleSellGet(RoutingContext routingContext) {
        String sql = "SELECT * FROM stock WHERE quantity>0";
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
                render(routingContext, "/invest/sell");
            });

        });


    }


    /**
     * 卖出股票POST
     *
     * @param routingContext
     */
    public void handleInvestSellPost(RoutingContext routingContext) {
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
        } else if (StringUtil.isNullOrEmpty(vo.getCode()) || vo.getPrice() <= 0 || vo.getCount() <= 0 || StringUtil.isNullOrEmpty(vo.getTransaction_date())) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        }

        InvestAddVo finalVo = vo;
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();

//                JsonArray params1 = new JsonArray();
//                params1.add(finalVo.getCode());
                //查询股票持仓数据
                conn.querySingleWithParams("SELECT quantity,avg_price FROM stock WHERE code=?", new JsonArray().add(finalVo.getCode()), query -> {
                    if (query.result() != null) {
                        StockEntity stock = new StockEntity();
                        stock.setQuantity(query.result().getInteger(0));
                        stock.setAvgPrice(query.result().getDouble(1));

                        if (stock.getQuantity() < finalVo.getCount()) {
                            jsonObject.put("code", 404).put("msg", "持股数量不足");
                            response.end(jsonObject.encode());
                        }
                        //投资金额 = 卖出数量 * 持股均价
                        Double investAmount = stock.getAvgPrice().doubleValue() * finalVo.getCount();
                        //计算盈利 = 卖出数量*卖出价格 - 投资金额
                        Double profit = finalVo.getPrice() * finalVo.getCount() - investAmount;

                        //添加投资记录
                        String sql = "INSERT INTO stock_trade (code,type,price,transaction_time,comment,count,create_on,amount,profit) VALUE (?,?,?,?,?,?,?,?,?)";
                        JsonArray params = new JsonArray();
                        params.add(finalVo.getCode());
                        params.add(2);//卖出
                        params.add(finalVo.getPrice());
                        params.add(DateUtil.dateToStamp(finalVo.getTransaction_date()));
//                params.add(finalVo.getStrategy());
                        params.add(finalVo.getComment());
                        params.add(finalVo.getCount());
                        params.add(System.currentTimeMillis() / 1000);
                        params.add(finalVo.getPrice() * finalVo.getCount());
                        params.add(profit);

                        conn.updateWithParams(sql, params, r -> {
                            if (r.succeeded()) {
                                //更新stock 自选股持股数量
                                JsonArray upParams = new JsonArray();
                                upParams.add(finalVo.getCount());
                                upParams.add(System.currentTimeMillis() / 1000);
                                upParams.add(finalVo.getCode());
                                conn.updateWithParams("UPDATE stock SET quantity=quantity-?,last_trade_time=? WHERE code=?", upParams, r1 -> {
                                });

                                //添加用户动态
                                String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on) VALUE (?,?,?,?,?,?,?,?,?)";
                                JsonArray dParams = new JsonArray();
                                dParams.add(1);
//                        if (finalVo.getTransaction_type() == 1) {
//                        dParams.add("买入股票【" + finalVo.getCode() + "】");
//                        dParams.add("买入股票" + finalVo.getCode() + "价格：￥" + finalVo.getPrice() + "，数量：" + finalVo.getCount() + "，交易时间：" + finalVo.getTransaction_date() + "，设定策略：" + finalVo.getStrategy() + "  " + finalVo.getComment());
//                        } else {
                                dParams.add("卖出股票【" + finalVo.getCode() + "】");
                                dParams.add("卖出股票" + finalVo.getCode() + "价格：￥" + finalVo.getPrice() + "，数量：" + finalVo.getCount() + "，交易时间：" + finalVo.getTransaction_date() + "，备注：" + finalVo.getComment());
//                        }
                                dParams.add("");
                                dParams.add("交易");
                                dParams.add(2);
                                dParams.add(finalVo.getCode());
                                dParams.add("/stock/detail/" + finalVo.getCode());
                                dParams.add(System.currentTimeMillis() / 1000);
                                conn.updateWithParams(dSql, dParams, r1 -> {
                                });

                                //更新资金账户的投资金额、计算收益
                                JsonArray accountParams = new JsonArray();
                                accountParams.add(investAmount);
                                accountParams.add(profit);
                                accountParams.add(1);
                                conn.updateWithParams("UPDATE user_account SET invest_amount=invest_amount-?,income_amount=income_amount+? WHERE id=?", accountParams, r3 -> {
                                });

                                jsonObject.put("code", 0).put("msg", "SUCCESS");

                                response.end(jsonObject.encode());
                            }
                        });
                    } else {
                        connection.cause().printStackTrace();
                        System.err.println(connection.cause().getMessage());
                        jsonObject.put("code", 404).put("msg", "该股票没有持仓数据");
                        response.end(jsonObject.encode());
                    }
                });
            }
        });
    }

    /**
     * 投资记录列表
     *
     * @param routingContext
     */
    public void handleInvestList(RoutingContext routingContext) {

        String code = routingContext.request().getParam("code");
        String sql = "SELECT tr.*,stock.name FROM stock_trade tr left join stock on stock.code = tr.code ";
        JsonArray params = new JsonArray();
        if (StringUtils.isEmpty(code) == false) {
            sql += " WHERE tr.code=?";
            params.add(code);
        }
        sql += " order by tr.id desc";


        String finalSql = sql;
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.queryWithParams(finalSql, params, query -> {
                if (query.succeeded()) {
                    List<JsonObject> list = query.result().getRows();

                    List<InvestEntity> investList = new ArrayList<>();

                    for (JsonObject child : list) {
                        InvestEntity entity = new InvestEntity();
                        entity.setCode(child.getString("code"));
                        entity.setName(child.getString("name"));
                        entity.setId(child.getInteger("id"));
                        entity.setCount(child.getInteger("count"));
                        entity.setCreate_on(child.getInteger("create_on"));
                        entity.setPrice(child.getDouble("price"));
//                    entity.setPrice_cost(child.getDouble("price_cost"));
//                    entity.setSell_drop_rate(child.getDouble("sell_drop_rate"));
//                    entity.setSell_up_rate(child.getDouble("sell_up_rate"));
                        entity.setStrategy(child.getString("strategy"));
                        entity.setComment(child.getString("comment"));
                        entity.setTransaction_time(child.getInteger("transaction_time"));
                        entity.setType(child.getInteger("type"));
                        entity.setTransactionTime(DateUtil.stampToDate(child.getInteger("transaction_time"), "yyyy-MM-dd HH:mm:ss"));

                        investList.add(entity);
                    }
                    conn.close();
//                    routingContext.put("name", investList.get(0).getName());
                    routingContext.put("invest_list", investList);
                }

                render(routingContext, "/invest/list");
            });

        });

    }


    /**
     * 添加投资日志
     *
     * @param routingContext
     */
    public void handleInvestLogAdd(RoutingContext routingContext) {
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

        InvestLogAddVo vo = null;
        try {
            vo = Json.decodeValue(content, InvestLogAddVo.class);
        } catch (Exception e) {
            vo = null;
        }

        if (vo == null) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        } else if (vo.getPrice() <= 0 || vo.getInvestId() <= 0) {
            jsonObject.put("code", 1).put("msg", "参数错误");
            response.end(jsonObject.encode());
        }

        InvestLogAddVo finalVo = vo;
        mySQLClient.getConnection(connection -> {
            if (connection.succeeded()) {
                SQLConnection conn = connection.result();
                //查询今天有没有添加，当天有添加就修改，没有添加就新增
                String selectSQL = "SELECT * FROM invest_log WHERE invest_id=? and date=?";
                JsonArray params1 = new JsonArray();
                params1.add(finalVo.getInvestId());
                params1.add(DateUtil.dateToString(new Date(), "yyyy-MM-dd"));

                conn.queryWithParams(selectSQL, params1, res -> {
                    if (res.succeeded()) {
                        List<JsonObject> list = res.result().getRows();
                        if (list == null || list.size() == 0) {
                            //没有，新增
                            String sql = "INSERT INTO invest_log (invest_id,date,type,price,rate,comment,create_on) VALUE (?,?,?,?,?,?,?)";
                            JsonArray params = new JsonArray();
                            params.add(finalVo.getInvestId());
                            params.add(DateUtil.dateToString(new Date(), "yyyy-MM-dd"));
                            params.add(0);
                            params.add(finalVo.getPrice());
                            params.add(finalVo.getRate());
                            params.add(finalVo.getComment());
                            params.add(System.currentTimeMillis() / 1000);


                            conn.updateWithParams(sql, params, r -> {
                                if (r.succeeded()) {
                                    jsonObject.put("code", 0).put("msg", "");
                                    response.end(jsonObject.encode());
                                } else {
                                    jsonObject.put("code", 1).put("msg", res.cause().getMessage());
                                    response.end(jsonObject.encode());
                                }
                            });

                        } else {
                            //有，修改
                            String sql = "UPDATE invest_log SET price=?,rate=?,comment=? WHERE id=?";
                            JsonArray params = new JsonArray();
                            params.add(finalVo.getPrice());
                            params.add(finalVo.getRate());
                            params.add(finalVo.getComment());
                            params.add(list.get(0).getInteger("id"));
                            conn.updateWithParams(sql, params, r -> {
                                if (r.succeeded()) {
                                    jsonObject.put("code", 0).put("msg", "");
                                    response.end(jsonObject.encode());
                                } else {
                                    jsonObject.put("code", 1).put("msg", res.cause().getMessage());
                                    response.end(jsonObject.encode());
                                }
                            });
                        }
                    } else {
                        jsonObject.put("code", 1).put("msg", res.cause().getMessage());
                        response.end(jsonObject.encode());
                    }
                });


            } else {
                connection.cause().printStackTrace();
                System.err.println(connection.cause().getMessage());
                jsonObject.put("code", 1).put("msg", connection.cause().getMessage());
                response.end(jsonObject.encode());
            }
        });
    }

    public void handleInvestLogList(RoutingContext routingContext) {
        Integer investId = 0;
        String id = routingContext.request().getParam("id");
        if (StringUtil.isNullOrEmpty(id) == false) {
            try {
                investId = Integer.parseInt(id);
            } catch (Exception e) {
            }
        }
        String sql = "SELECT * FROM invest_log WHERE invest_id =  " + investId + " ORDER BY id DESC";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();

            conn.query(sql, query -> {
                List<JsonObject> list = query.result().getRows();

                List<InvestLogEntity> investList = new ArrayList<>();

                for (JsonObject child : list) {
                    InvestLogEntity entity = new InvestLogEntity();

                    entity.setId(child.getInteger("id"));
                    entity.setInvest_id(child.getInteger("invest_id"));
                    entity.setCreate_on(child.getInteger("create_on"));
                    entity.setDate(child.getString("date"));
                    entity.setType(child.getInteger("type"));
                    entity.setPrice(child.getDouble("price"));
                    entity.setRate(child.getDouble("rate"));
                    entity.setComment(child.getString("comment"));
                    investList.add(entity);
                }
                conn.close();
                routingContext.put("invest_log_list", investList);

                render(routingContext, "/invest/log_list");
            });

        });

    }


}
