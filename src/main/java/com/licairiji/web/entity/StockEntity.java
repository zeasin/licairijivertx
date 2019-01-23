package com.licairiji.web.entity;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-01-23 18:02
 */
public class StockEntity {
    private String code;
    private String name;
    private double joinPrice;
    private Integer createOn;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getJoinPrice() {
        return joinPrice;
    }

    public void setJoinPrice(double joinPrice) {
        this.joinPrice = joinPrice;
    }

    public Integer getCreateOn() {
        return createOn;
    }

    public void setCreateOn(Integer createOn) {
        this.createOn = createOn;
    }
}
