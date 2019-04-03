package com.licairiji.web.entity;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-04-03 17:58
 */
public class UserAccountEntity {
    private Integer id;
    private String userName;
    private String mobile;
    private String pwd;
    private Float capitalTotal;
    private Float investAmount;
    private Float incomeAmount;
    private Integer createOn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Float getCapitalTotal() {
        return capitalTotal;
    }

    public void setCapitalTotal(Float capitalTotal) {
        this.capitalTotal = capitalTotal;
    }

    public Float getInvestAmount() {
        return investAmount;
    }

    public void setInvestAmount(Float investAmount) {
        this.investAmount = investAmount;
    }

    public Float getIncomeAmount() {
        return incomeAmount;
    }

    public void setIncomeAmount(Float incomeAmount) {
        this.incomeAmount = incomeAmount;
    }

    public Integer getCreateOn() {
        return createOn;
    }

    public void setCreateOn(Integer createOn) {
        this.createOn = createOn;
    }
}
