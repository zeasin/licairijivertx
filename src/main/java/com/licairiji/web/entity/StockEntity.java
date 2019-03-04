package com.licairiji.web.entity;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-01-23 18:02
 */
public class StockEntity {
    private Integer id;
    private String code;
    private String ecode;
    private String name;
    private double joinPrice;
    private String plate;
    private String createOn;
    private String comment;

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEcode() {
        return ecode;
    }

    public void setEcode(String ecode) {
        this.ecode = ecode;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

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

    public String getCreateOn() {
        return createOn;
    }

    public void setCreateOn(String createOn) {
        this.createOn = createOn;
    }
}
