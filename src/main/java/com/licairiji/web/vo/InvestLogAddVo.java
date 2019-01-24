package com.licairiji.web.vo;

/**
 * 描述：
 * 投资日志
 *
 * @author qlp
 * @date 2019-01-24 09:52
 */
public class InvestLogAddVo {
    private int investId;
    private double price;
    private double rate;
    private String comment;

    public int getInvestId() {
        return investId;
    }

    public void setInvestId(int investId) {
        this.investId = investId;
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
}
