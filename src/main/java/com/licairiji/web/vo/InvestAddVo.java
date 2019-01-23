package com.licairiji.web.vo;

/**
 * 描述：
 * 添加投资记录vo
 *
 * @author qlp
 * @date 2019-01-23 18:26
 */
public class InvestAddVo {
    private String code;
    private int transaction_type;
    private double price_buy;
    private double price_cost;
    private String transaction_date;
    private double sell_drop_rate;
    private double sell_up_rate;
    private String strategy;
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(int transaction_type) {
        this.transaction_type = transaction_type;
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

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
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
}
