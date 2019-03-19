package com.licairiji.web.vo;

import java.util.List;

/**
 * @program: licairiji-vertx
 * @description:
 * @author: Mr.Qi
 * @create: 2019-03-19 17:40
 **/
public class MessagesResult {
    private List<MessagesVo> Messages;

    public List<MessagesVo> getMessages() {
        return Messages;
    }

    public void setMessages(List<MessagesVo> messages) {
        Messages = messages;
    }
}
