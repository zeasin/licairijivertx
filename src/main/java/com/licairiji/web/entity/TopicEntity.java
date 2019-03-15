package com.licairiji.web.entity;

/**
 * @program: licairiji-vertx
 * @description:
 * @author: Mr.Qi
 * @create: 2019-03-15 14:29
 **/
public class TopicEntity {
    private int id;
    private String title;
    private String content;
    private int createOn;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCreateOn() {
        return createOn;
    }

    public void setCreateOn(int createOn) {
        this.createOn = createOn;
    }
}
