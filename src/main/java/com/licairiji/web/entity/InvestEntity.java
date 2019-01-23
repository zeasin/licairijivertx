package com.licairiji.web.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-01-23 19:31
 */
public class InvestEntity {
    private int id;
    private String code;
    private String name;
    private int type;
    private double price_buy;
    private double price_cost;
    private int transaction_time;
    private double sell_drop_rate;
    private double sell_up_rate;
    private String strategy;
    private int count;
    private int create_on;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String transactionTime;

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getPrice_buy() {
        return price_buy;
    }

    public void setPrice_buy(double price_buy) {
        this.price_buy = price_buy;
    }

    public double getPrice_cost() {
        return price_cost;
    }

    public void setPrice_cost(double price_cost) {
        this.price_cost = price_cost;
    }

    public int getTransaction_time() {
        return transaction_time;
    }

    public void setTransaction_time(int transaction_time) {
        this.transaction_time = transaction_time;
    }

    public double getSell_drop_rate() {
        return sell_drop_rate;
    }

    public void setSell_drop_rate(double sell_drop_rate) {
        this.sell_drop_rate = sell_drop_rate;
    }

    public double getSell_up_rate() {
        return sell_up_rate;
    }

    public void setSell_up_rate(double sell_up_rate) {
        this.sell_up_rate = sell_up_rate;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCreate_on() {
        return create_on;
    }

    public void setCreate_on(int create_on) {
        this.create_on = create_on;
    }
}
