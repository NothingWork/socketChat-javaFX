package com.wyz.socketchat.util;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Yun
 * @description: 和消息手法相关的工具类
 */
public class MessageUtil {
    public volatile Boolean  flag = true;
    private BufferedReader br;
    /**
     * @description: 消息发送
     * @param:发送消息的客户端，消息类型，消息内容
     * @return: void
     */
    public void sendMessage(Socket client,Integer code,String str) {
        //向服务器发送消息
        PrintWriter out;
        try {
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
            out.println(code+str);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
