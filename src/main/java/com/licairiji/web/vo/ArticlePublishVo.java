package com.licairiji.web.vo;

/**
 * 描述：
 * 文章发布vo
 *
 * @author qlp
 * @date 2019-01-23 14:16
 */
public class ArticlePublishVo {
    private String image;
    private String title;
    private String tags;
    private String plate;//板块
    private String stock;//股票代码
    private String content;

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
}
