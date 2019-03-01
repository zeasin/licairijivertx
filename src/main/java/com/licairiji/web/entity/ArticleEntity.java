package com.licairiji.web.entity;

/**
 * 描述：
 *
 * @author qlp
 * @date 2019-03-01 14:56
 */
public class ArticleEntity {
    private Integer id;
    private String title;
    private String image;
    private String content;
    private Integer createOn;


    public Integer getCreateOn() {
        return createOn;
    }

    public void setCreateOn(Integer createOn) {
        this.createOn = createOn;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
