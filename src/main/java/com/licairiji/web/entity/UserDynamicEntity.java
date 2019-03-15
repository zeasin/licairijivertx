package com.licairiji.web.entity;

/**
 * @program: licairiji-vertx
 * @description: 用户动态
 * @author: Mr.Qi
 * @create: 2019-03-15 13:48
 **/
public class UserDynamicEntity {
    private Integer id;
    private String title;
    private String imgs;
    private String content;
    private String tags;
    private Integer numSc;
    private Integer numPing;
    private Integer numZan;
    private Integer type;
    private String dataId;
    private String url;
    private Integer createOn;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getNumSc() {
        return numSc;
    }

    public void setNumSc(Integer numSc) {
        this.numSc = numSc;
    }

    public Integer getNumPing() {
        return numPing;
    }

    public void setNumPing(Integer numPing) {
        this.numPing = numPing;
    }

    public Integer getNumZan() {
        return numZan;
    }

    public void setNumZan(Integer numZan) {
        this.numZan = numZan;
    }

    public Integer getCreateOn() {
        return createOn;
    }

    public void setCreateOn(Integer createOn) {
        this.createOn = createOn;
    }
}
