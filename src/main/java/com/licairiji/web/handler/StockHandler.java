package com.licairiji.web.handler;

import com.csvreader.CsvReader;
import com.licairiji.web.DateUtil;
import com.licairiji.web.entity.StockDataEntity;
import com.licairiji.web.entity.StockEntity;
import com.licairiji.web.utils.HTMLSpirit;
import com.licairiji.web.utils.UrlStreamUtils;
import com.licairiji.web.vo.MessagesResult;
import com.licairiji.web.vo.Stock163;
import com.licairiji.web.vo.StockListAjaxVo;
import com.qiniu.util.Json;
import com.qiniu.util.StringUtils;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
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
     * @param routingContext
     */
    public void handleStockListAjax(RoutingContext routingContext) {
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");

        String sql = "SELECT code,name FROM  stock order by id desc";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();
            conn.query(sql, query -> {
//                List<JsonObject> list = query.result().getRows();

                List<StockListAjaxVo> stocks = new ArrayList<>();

                for (JsonObject child : query.result().getRows()) {
                    StockListAjaxVo entity = new StockListAjaxVo();
                    entity.setCode(child.getString("code"));
                    entity.setName(child.getString("name"));
                    stocks.add(entity);
                }
                conn.close();
                jsonObject.put("code", 0).put("msg", "SUCCESS");
                jsonObject.put("data",stocks);
                response.end(jsonObject.encode());
            });

        });
    }
    /**
     * 自选股列表
     *
     * @param routingContext
     */
    public void handleStockList(RoutingContext routingContext) {

        String sql = "SELECT * FROM  stock order by last_trade_time desc";
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.query(sql, query -> {
                String prod_code="";

                List<JsonObject> list = query.result().getRows();

                List<StockEntity> investList = new ArrayList<>();

                for (JsonObject child : list) {
                    StockEntity entity = new StockEntity();
                    entity.setCode(child.getString("code"));
                    entity.setName(child.getString("name"));
                    entity.setId(child.getInteger("id"));
                    entity.setCreateOn(DateUtil.stampToDate(child.getLong("create_on"), "yyyy-MM-dd"));
                    entity.setJoinPrice(child.getDouble("join_price"));
                    entity.setComment(child.getString("comment"));
                    entity.setEcode(child.getString("ecode"));
                    entity.setPlate(child.getString("plate"));
                    entity.setBourse(child.getString("bourse"));
                    entity.setQuantity(child.getInteger("quantity"));
                    if (child.getString("board").equalsIgnoreCase("ZB"))
                        entity.setBoard("主板");
                    else if (child.getString("board").equalsIgnoreCase("ZSB"))
                        entity.setBoard("中小板");
                    else if (child.getString("board").equalsIgnoreCase("CYB"))
                        entity.setBoard("创业板");
                    entity.setQuantity(child.getInteger("quantity"));
                    entity.setAvgPrice(child.getDouble("avg_price"));

                    prod_code += child.getString("code")+"."+child.getString("bourse")+",";

                    investList.add(entity);
                }
                if(StringUtils.isNullOrEmpty(prod_code)==false) prod_code = prod_code.substring(0,prod_code.length()-1);

                conn.close();
                routingContext.put("stock_list", investList);
                routingContext.put("prod_code", prod_code);

                render(routingContext, "/stock/list22222");
            });

        });

    }

    public void handleStockAddGet(RoutingContext routingContext) {
//        routingContext.put("stock_list", investList);

        render(routingContext, "/stock/add");
    }

    /**
     * 添加自选股
     *
     * @param routingContext
     */
    public void handleStockAddPost(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String code = request.getParam("code");
        String name = "";
        float price = 0.0f;//request.getParam("price");
        String plate = request.getParam("plate");
        String comment = request.getParam("comment");
        JsonObject jsonObject = new JsonObject();
        HttpServerResponse response = routingContext.response();
        response.setStatusCode(200);
        response.putHeader("Content-Type", "application/json");
        if (org.thymeleaf.util.StringUtils.isEmpty(code) || code.length() != 6) {
            jsonObject.put("code", 1).put("msg", "股票代码不正确");
            response.end(jsonObject.encode());
        }

        String board = "";
        String ecode = "";
        String bourse = "";
        if (code.startsWith("600") || code.startsWith("601") || code.startsWith("603")) {
            ecode = "sh" + code;
            board = "ZB";
            bourse = "SS";
        } else if (code.startsWith("000") || code.startsWith("002") || code.startsWith("300")) {
            ecode = "sz" + code;
            bourse = "SZ";
            if (code.startsWith("000")) board = "ZB";
            else if (code.startsWith("002")) board = "ZSB";
            else if (code.startsWith("300")) board = "CYB";
        } else {
            jsonObject.put("code", 1).put("msg", "股票代码不正确");
            response.end(jsonObject.encode());
        }

//        List<StockDataEntity> stockDataList = new ArrayList<>();//股票数据
        List<JsonArray> batch = new ArrayList<>();//股票数据insert mysql params


        //读取网易股票接口，下载股票历史数据
        String url163 = "http://quotes.money.163.com/service/chddata.html?code=";
        if (bourse.equalsIgnoreCase("SZ")) url163 += "1" + code;
        else if (bourse.equalsIgnoreCase("SS")) url163 += "0" + code;
        try {
            Long time = System.currentTimeMillis() / 1000;
            URL url = new URL(url163);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            httpConn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
//            conn.setRequestProperty("lfwywxqyh_token",toekn);

            //得到输入流
            InputStream inputStream = httpConn.getInputStream();
            //获取自己数组
//            byte[] getData = UrlStreamUtils.readInputStream(inputStream);
            CsvReader csvReader = new CsvReader(inputStream, ',', Charset.forName("GBK"));
            if (csvReader.readRecord()) {
                while (csvReader.readRecord()) {
                    String line = csvReader.getRawRecord();
//                    String[] lineArr = line.split(",");

//                    StockDataEntity d = new StockDataEntity();
                    JsonArray dataParams = new JsonArray();
                    dataParams.add(code);


                    String dataStr = line.split(",")[0];//时间
//                    Date da = DateUtil.stringtoDate(dataStr, "yyyy-MM-dd");
//                    d.setDate(dataStr + " 09:00:00");
                    dataParams.add(dataStr + " 09:00:00");
                    dataParams.add(DateUtil.date2TimeStamp(dataStr + " 09:00:00", "yyyy-MM-dd HH:mm:ss"));
//                    d.setDatetime(DateUtil.date2TimeStamp(dataStr + " 09:00:00", "yyyy-MM-dd HH:mm:ss"));
//                    d.setCode(code);
                    name = line.split(",")[2];//股票名称
//                    d.setName(name);
                    dataParams.add(name);

                    String price_sp = line.split(",")[3];//收盘价
                    if (price_sp.equals("None")) {
//                        d.setPrice_end(0);
                        dataParams.add(0);
                    } else {
                        if (price <= 0) price = Float.valueOf(price_sp);
//                        d.setPrice_end(Float.valueOf(price_sp));
                        dataParams.add(Float.valueOf(price_sp));
                    }

                    String price_max = line.split(",")[4];//最高价
//                    d.setPrice_max(price_max.equals("None") ? 0 : Float.valueOf(price_max));
                    dataParams.add(price_max.equals("None") ? 0 : Float.valueOf(price_max));

                    String price_min = line.split(",")[5];//最低价
//                    d.setPrice_min(price_min.equals("None") ? 0 : Float.valueOf(price_min));
                    dataParams.add(price_min.equals("None") ? 0 : Float.valueOf(price_min));

                    String price_kp = line.split(",")[6];//开盘价
//                    d.setPrice_start(price_kp.equals("None") ? 0 : Float.valueOf(price_kp));
                    dataParams.add(price_kp.equals("None") ? 0 : Float.valueOf(price_kp));

                    String price_qsp = line.split(",")[7];//前收盘
//                    d.setPrice_end_yesterday(price_qsp.equals("None") ? 0 : Float.valueOf(price_qsp));
                    dataParams.add(price_qsp.equals("None") ? 0 : Float.valueOf(price_qsp));

                    String zd_money = line.split(",")[8];//涨跌额
//                    d.setUp_down_money(zd_money.equals("None") ? 0 : Float.valueOf(zd_money));
                    dataParams.add(zd_money.equals("None") ? 0 : Float.valueOf(zd_money));

                    String zd_rate = line.split(",")[9];//涨跌幅
//                    d.setUp_down_rate(zd_rate.equals("None") ? 0 : Float.valueOf(zd_rate));
                    dataParams.add(zd_rate.equals("None") ? 0 : Float.valueOf(zd_rate));

                    String huan_rate = line.split(",")[10];//换手率
//                    d.setTurnover_rate(huan_rate.equals("None") ? 0 : Float.valueOf(huan_rate));
                    dataParams.add(huan_rate.equals("None") ? 0 : Float.valueOf(huan_rate));

                    String cjl = line.split(",")[11];//成交量
//                    d.setTransaction_volume(cjl.equals("None") ? 0 : Double.valueOf(cjl));
                    dataParams.add(cjl.equals("None") ? 0 : Double.valueOf(cjl));

                    String cje = line.split(",")[12];//成交金额
//                    d.setTransaction_money(cje.equals("None") ? 0 : Double.valueOf(cje));
                    dataParams.add(cje.equals("None") ? 0 : Double.valueOf(cje));

                    String cjb = line.split(",")[15];//成交笔数
//                    d.setTransaction_count(cjb.equals("None") ? 0 : Double.valueOf(cjb));
                    dataParams.add(cjb.equals("None") ? 0 : Double.valueOf(cjb));

                    String zsz = line.split(",")[13];//总市值
//                    d.setTotal_value(zsz.equals("None") ? 0 : Double.valueOf(zsz));
                    dataParams.add(zsz.equals("None") ? 0 : Double.valueOf(zsz));

                    String ltsz = line.split(",")[14];//流通市值
//                    d.setMarket_value(ltsz.equals("None") ? 0 : Double.valueOf(ltsz));
                    dataParams.add(ltsz.equals("None") ? 0 : Double.valueOf(ltsz));

                    dataParams.add(time);
                    batch.add(dataParams);

//                    // 读一整行
                    System.out.println(csvReader.getRawRecord());
//                    // 读这行的某一列
//                    System.out.println(csvReader.get("名称"));
//                    stockDataList.add(d);
                }
            }


//            if (batch != null && batch.size() > 0) {
            //开始写入数据库
            String finalEcode = ecode;
            String finalBoard = board;
            String finalBourse = bourse;
            String finalName = name;
            float finalPrice = price;

            mySQLClient.getConnection(connection -> {
                if (connection.failed()) {
                    throw new RuntimeException(connection.cause());
                }
                SQLConnection conn = connection.result();
                //没有，新增
                String sql = "INSERT INTO stock (code,ecode,name,board,bourse,plate,join_price,comment,create_on) VALUE (?,?,?,?,?,?,?,?,?)";
                JsonArray params = new JsonArray();
                params.add(code);
                params.add(finalEcode);
                params.add(finalName);
                params.add(finalBoard);
                params.add(finalBourse);

                params.add(plate);
                params.add(finalPrice);
                params.add(comment);
                params.add(time);

                conn.updateWithParams(sql, params, r -> {
                    if (r.succeeded()) {

//                            String dataSQL = "INSERT INTO stock_data (code,name,date,datetime,price_end,price_max,price_min,price_start,price_end_yesterday,up_down_money,up_down_rate,turnover_rate,transaction_volume,transaction_money,transaction_count,total_value,market_value,create_on)" +
//                                    " VALUE (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//
//                            conn.batchWithParams(dataSQL, batch, r2 -> {
//                                if (r2.succeeded()) {
//                                    System.out.println("OK");
//                                } else {
//                                    connection.cause().printStackTrace();
//                                    System.err.println("失败：" + connection.cause().getMessage());
//                                }
//                            });


//                            加入动态
                        String dSql = "INSERT INTO user_dynamic (user_id,title,content,imgs,tags,type,data_id,url,create_on) VALUE (?,?,?,?,?,?,?,?,?)";

                        JsonArray dParams = new JsonArray();
                        dParams.add(1);
                        dParams.add("添加[" + code + finalName + "]到自选股");
                        dParams.add(code + finalName + "现价：￥" + finalPrice + "，所属行业：" + plate + "，备注：" + comment);
                        dParams.add("");
                        dParams.add("自选股");
                        dParams.add(2);
                        dParams.add(code);
                        dParams.add("/stock/detail/" + code);
                        dParams.add(System.currentTimeMillis() / 1000);
                        conn.updateWithParams(dSql, dParams, r1 -> {
//                                if (r1.succeeded()) {
//                                    jsonObject.put("code", 0).put("msg", "SUCCESS");
//                                    response.end(jsonObject.encode());
//                                }
                        });


                        jsonObject.put("code", 0).put("msg", "SUCCESS");
                        response.end(jsonObject.encode());

//                            HttpServerResponse response = routingContext.response();
//                            response.setStatusCode(302);
//                            response.headers().add("location", "/");
//                            response.end();
                    } else {
                        connection.cause().printStackTrace();
                        System.err.println(connection.cause().getMessage());
//                    HttpServerResponse response = routingContext.response();
//                    response.setStatusCode(302);
//                    response.headers().add("location", "/stock/add");
//                    response.end();
                    }
                });
            });


//            } else {
//                jsonObject.put("code", 1).put("msg", "没找到该股票");
//                response.end(jsonObject.encode());
//            }


        } catch (Exception e) {


            System.out.println(e.getMessage());
            jsonObject.put("code", 1).put("msg", e.getMessage());
            response.end(jsonObject.encode());


        }


    }

    /**
     * 股票详情
     *
     * @param routingContext
     */
    public void handleStockDetail(RoutingContext routingContext) {
        String code = routingContext.request().getParam("code");
        String sql = "SELECT * FROM  stock WHERE code=? LIMIT 1";
        JsonArray params = new JsonArray();
        params.add(code);

        StockEntity entity = new StockEntity();
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.querySingleWithParams(sql, params, query -> {
                JsonArray child = query.result();
                entity.setId(child.getInteger(0));
                entity.setCode(child.getString(1));
                entity.setEcode(child.getString(2));
                entity.setBoard(child.getString(3));
                entity.setBourse(child.getString(4));
                entity.setName(child.getString(5));
                entity.setPlate(child.getString(6));
                entity.setJoinPrice(child.getDouble(7));
                entity.setQuantity(child.getInteger(8));
                entity.setComment(child.getString(10));

                //读取https://api.xuangubao.cn/资讯
                try {
//
                    String url = "https://api.xuangubao.cn/api/stocks/messages?Symbol=" + entity.getCode() + "." + entity.getBourse() + "&Limit=20";
                    //开始读取数据
                    OkHttpClient okHttpClient = new OkHttpClient(); // 创建OkHttpClient对象
                    Request request = new Request.Builder().url(url).build(); // 创建一个请求
                    Response response = okHttpClient.newCall(request).execute(); // 返回实体

                    if (response.isSuccessful()) { // 判断是否成功
                        /**获取返回的数据，可通过response.body().string()获取，默认返回的是utf-8格式；
                         * string()适用于获取小数据信息，如果返回的数据超过1M，建议使用stream()获取返回的数据，
                         * 因为string() 方法会将整个文档加载到内存中。*/
                        String result = response.body().string();
                        MessagesResult messagesResult = Json.decode(result,MessagesResult.class);
                        System.out.println(result);

                        routingContext.put("news", messagesResult.getMessages());
                    }
                } catch (Exception e) {

                }
                routingContext.put("stock", entity);
                routingContext.put("code", code);
                if (entity.getBourse().equalsIgnoreCase("SS")) {
                    routingContext.put("ecode", "0" + code);
                } else {
                    routingContext.put("ecode", "1" + code);
                }
                routingContext.put("name", entity.getName());
                render(routingContext, "/stock/detail");

            });
        });


    }

    /**
     * 股票K线（网易接口，今年）
     *
     * @param routingContext
     */
    public void handleStockKData(RoutingContext routingContext) {
        HttpServerRequest req = routingContext.request();
//        String code = req.getParam("code");

        HttpServerResponse resp = routingContext.response();
//返回内容
        JsonObject jsonObject = new JsonObject();

        String code = routingContext.request().getParam("code");
        String sql = "SELECT * FROM  stock WHERE code=? LIMIT 1";
        JsonArray params = new JsonArray();
        params.add(code);

        StockEntity entity = new StockEntity();
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.querySingleWithParams(sql, params, query -> {
                JsonArray child = query.result();
//                entity.setId(child.getInteger(0));
                entity.setCode(child.getString(1));
//                entity.setEcode(child.getString(2));
//                entity.setBoard(child.getString(3));
                entity.setBourse(child.getString(4));
//                entity.setName(child.getString(5));
                //查询日K数据
                try {
//                    OkHttpClient client = new OkHttpClient();
//                    String urlBaidu = "http://finance.sina.com.cn/realstock/company/sz300059/qianfuquan.js?d=";
                    String url163 = "http://img1.money.126.net/data/hs/kline/day/history/2019/";//1300059.json
                    if (entity.getBourse().equalsIgnoreCase("SS")) {
                        url163 += "0" + entity.getCode() + ".json";
                    } else if (entity.getBourse().equalsIgnoreCase("SZ")) {
                        url163 += "1" + entity.getCode() + ".json";
                    }
                    //开始读取数据
                    OkHttpClient okHttpClient = new OkHttpClient(); // 创建OkHttpClient对象
                    Request request = new Request.Builder().url(url163).build(); // 创建一个请求
                    Response response = okHttpClient.newCall(request).execute(); // 返回实体

                    if (response.isSuccessful()) { // 判断是否成功
                        /**获取返回的数据，可通过response.body().string()获取，默认返回的是utf-8格式；
                         * string()适用于获取小数据信息，如果返回的数据超过1M，建议使用stream()获取返回的数据，
                         * 因为string() 方法会将整个文档加载到内存中。*/
                        String result = response.body().string();
                        System.out.println(result); // 打印数据
                        Stock163 stock163 = Json.decode(result, Stock163.class);
                        System.out.println(stock163.getName());
//                        System.out.println(stock163.getData());

                        ArrayList<List<BigDecimal>> arrayList = new ArrayList<>();

                        for (List<String> item : stock163.getData()) {
                            List<BigDecimal> it = new ArrayList<>();
                            it.add(BigDecimal.valueOf((DateUtil.date2TimeStamp(item.get(0) + "08:00:00", "yyyyMMddHH:mm:ss"))));
                            it.add(new BigDecimal(item.get(1)));//item 1 开盘价
                            it.add(new BigDecimal(item.get(3)));//item 3 最高价
                            it.add(new BigDecimal(item.get(4)));//item 4 最低价
                            it.add(new BigDecimal(item.get(2)));//item 2 收盘价
                            it.add(new BigDecimal(item.get(5)));//item 5 成交量


//                    it.add(new BigDecimal(item.get(5)));
                            arrayList.add(it);
//                    Long s = DateUtil.date2TimeStamp(item.get(0).toString() + "08:00:00", "yyyyMMddHH:mm:ss");
//                    item.set(0,Float.valueOf(s.toString()));
//                    item.remove(6);
                        }

                        jsonObject.put("code", 1).put("msg", "操作成功！");


                        jsonObject.put("data", arrayList);

//                StringBuilder sb = new StringBuilder();
//                sb.append(?)


                        resp.end(jsonObject.encode());

                    } else {
                        jsonObject.put("code", 0).put("msg", "失败！");
                        resp.end(jsonObject.encode());
                        System.out.println("失败"); // 链接失败
                    }
                } catch (Exception e) {

                    System.out.println(e.getMessage());
                }
            });
        });
    }

    /**
     * 股票K线（自有数据）
     *
     * @param routingContext
     */
    public void handleStockKData2(RoutingContext routingContext) {
//        HttpServerRequest req = routingContext.request();
        HttpServerResponse resp = routingContext.response();
        resp.setStatusCode(200);
        resp.putHeader("Content-Type", "application/json");


        //返回内容
        JsonObject jsonObject = new JsonObject();

        String code = routingContext.request().getParam("code");
        if (org.thymeleaf.util.StringUtils.isEmpty(code)) {
            jsonObject.put("code", 0).put("msg", "失败！");
            resp.end(jsonObject.encode());
            return;
        }

        String sql = "SELECT * FROM  stock_data WHERE code=? order by datetime desc limit 55";
        JsonArray params = new JsonArray();
        params.add(code);

//        StockEntity entity = new StockEntity();
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                throw new RuntimeException(res.cause());
            }
            SQLConnection conn = res.result();


            conn.queryWithParams(sql, params, query -> {
                if (query.succeeded()) {
                    ArrayList<List<Object>> arrayList = new ArrayList<>();
                    List<JsonObject> list = query.result().getRows();
                    for (JsonObject child : list) {
                        List<Object> it = new ArrayList<>();
                        it.add(child.getLong("datetime"));
                        it.add(child.getFloat("price_start"));//item 1 开盘价
                        it.add(child.getFloat("price_max"));//item 3 最高价
                        it.add(child.getFloat("price_min"));//item 4 最低价
                        it.add(child.getFloat("price_end"));//item 2 收盘价
                        it.add(new BigDecimal(child.getDouble("transaction_volume")));//item 5 成交量
                        arrayList.add(it);
                    }

                    jsonObject.put("code", 1).put("msg", "操作成功！");
                    jsonObject.put("data", arrayList);
                    resp.end(jsonObject.encode());
                } else {
                    jsonObject.put("code", 0).put("msg", "失败！");
                    resp.end(jsonObject.encode());
                    System.out.println("失败"); // 链接失败
                }
            });
        });
    }


}
