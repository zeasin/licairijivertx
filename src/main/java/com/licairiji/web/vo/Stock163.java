package com.licairiji.web.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: licairiji-vertx
 * @description:
 * @author: Mr.Qi
 * @create: 2019-03-16 20:58
 **/
public class Stock163 {
    private String symbol;
    private String name;
    private ArrayList<List<String>> data;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<List<String>> getData() {
        return data;
    }

    public void setData(ArrayList<List<String>> data) {
        this.data = data;
    }
}
