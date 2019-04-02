package com.licairiji.web.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @program: licairiji-vertx
 * @description:
 * @author: Mr.Qi
 * @create: 2019-04-02 20:44
 **/
public class ClearBufferThread implements Runnable  {
    private InputStream inputStream;

    public ClearBufferThread(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public void run() {
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
            while(br.readLine() != null);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
