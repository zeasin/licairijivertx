package com.licairiji.web.entity;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-04-04 14:07
 */
public class PlateEntity {
    private Integer id;
    private String title;
    private String content;
    private Integer createOn;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCreateOn() {
        return createOn;
    }

    public void setCreateOn(Integer createOn) {
        this.createOn = createOn;
    }
}
