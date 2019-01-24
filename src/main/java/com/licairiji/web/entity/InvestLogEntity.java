package com.licairiji.web.entity;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-01-24 10:27
 */
public class InvestLogEntity {
    private int id;
    private int invest_id;
    private String date;
    private int type;
    private double price;
    private double rate;
    private String comment;
    private int create_on;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvest_id() {
        return invest_id;
    }

    public void setInvest_id(int invest_id) {
        this.invest_id = invest_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getCreate_on() {
        return create_on;
    }

    public void setCreate_on(int create_on) {
        this.create_on = create_on;
    }
}
