package com.licairiji.web.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @program: licairiji-vertx
 * @description:
 * @author: Mr.Qi
 * @create: 2019-03-17 23:33
 **/
public class StockDataEntity {
    private String code;//股票代码',
    private Long datetime;
    private String name;// varchar(10) not null comment '股票名称',
    private String date;// DATE not null comment '日期',
    private float price_end;// float not null comment '收盘价',
    private float price_max;// FLOAT not null comment '最高价',
    private float price_min;// Float not null comment '最低价',
    private float price_start;// float not null comment '开盘价',
    private float price_end_yesterday;// float not null comment '前收盘',
    private float up_down_money;// float not null comment '涨跌额',
    private float up_down_rate;// float not null comment '涨跌幅',
    private float turnover_rate;// float not null comment '换手率',
    private Double transaction_volume;// DECIMAL(15,2)  not null comment '成交量',
    private Double transaction_money;// DECIMAL(15,2)  not null comment '成交金额',
    private Double transaction_count;// DECIMAL(15,2)  not null comment '成交笔数',
    private Double total_value;// DECIMAL(16,2) not null comment '总市值',
    private Double market_value;// DECIMAL(16,2)  not null comment '流通市值',
    private Integer create_on;//int not null comment '创建时间'

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getDatetime() {
        return datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getPrice_end() {
        return price_end;
    }

    public void setPrice_end(float price_end) {
        this.price_end = price_end;
    }

    public float getPrice_max() {
        return price_max;
    }

    public void setPrice_max(float price_max) {
        this.price_max = price_max;
    }

    public float getPrice_min() {
        return price_min;
    }

    public void setPrice_min(float price_min) {
        this.price_min = price_min;
    }

    public float getPrice_start() {
        return price_start;
    }

    public void setPrice_start(float price_start) {
        this.price_start = price_start;
    }

    public float getPrice_end_yesterday() {
        return price_end_yesterday;
    }

    public void setPrice_end_yesterday(float price_end_yesterday) {
        this.price_end_yesterday = price_end_yesterday;
    }

    public float getUp_down_money() {
        return up_down_money;
    }

    public void setUp_down_money(float up_down_money) {
        this.up_down_money = up_down_money;
    }

    public float getUp_down_rate() {
        return up_down_rate;
    }

    public void setUp_down_rate(float up_down_rate) {
        this.up_down_rate = up_down_rate;
    }

    public float getTurnover_rate() {
        return turnover_rate;
    }

    public void setTurnover_rate(float turnover_rate) {
        this.turnover_rate = turnover_rate;
    }

    public Double getTransaction_volume() {
        return transaction_volume;
    }

    public void setTransaction_volume(Double transaction_volume) {
        this.transaction_volume = transaction_volume;
    }

    public Double getTransaction_money() {
        return transaction_money;
    }

    public void setTransaction_money(Double transaction_money) {
        this.transaction_money = transaction_money;
    }

    public Double getTransaction_count() {
        return transaction_count;
    }

    public void setTransaction_count(Double transaction_count) {
        this.transaction_count = transaction_count;
    }

    public Double getTotal_value() {
        return total_value;
    }

    public void setTotal_value(Double total_value) {
        this.total_value = total_value;
    }

    public Double getMarket_value() {
        return market_value;
    }

    public void setMarket_value(Double market_value) {
        this.market_value = market_value;
    }

    public Integer getCreate_on() {
        return create_on;
    }

    public void setCreate_on(Integer create_on) {
        this.create_on = create_on;
    }
}
