package com.licairiji.web.handler;

import com.licairiji.web.DateUtil;
import com.licairiji.web.entity.StockEntity;
import io.vertx.core.Context;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 股票分析
 *
 * @author qlp
 * @date 2019-03-06 17:13
 */
public class StockAnalyseHandler extends AbstractHandler {
    public StockAnalyseHandler(Context context, ThymeleafTemplateEngine templateEngine, SQLClient mySQLClient) {
        super(context, templateEngine, mySQLClient);
    }

    public void handleStockAnalyse(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String code = request.getParam("code");
        routingContext.put("code", code);

        /*******************************我的炒股手册***********************************/
        /**1、热点行业板块（政策支持、未来方向）**/
        /**2、行业龙头股**/
        /**3、市值中等**/
        /**4、价位合适（市净率不要过高）**/
        /**5、最近20个交易日涨幅不超20% **/
        /**6、处于上涨趋势中（利用九转系列分析）**/


        /********************************一、技术分析***********************************/

        //10日涨幅分析
        //http://data.gtimg.cn/flashdata/hushen/daily/19/sh600519.js

        //20日涨幅分析

        //九转系列买入、卖出分析

        /********************************二、基本面分析***********************************/
        //市盈率市净率分析

        //市值分析（大盘中盘小盘）

        //板块分析（是否热点板块-行业板块、概念板块）

        //龙头股分析（1、从板块中进入个股；2、查看F10资料；3、查看行业对比；5、查看具体数据（如：净利润））

        render(routingContext, "/stock/analyse");
    }

}
