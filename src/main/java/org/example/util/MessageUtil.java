package org.example.util;

import org.example.bean.Message;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Yun
 * @description: 和消息收发相关的工具类
 */
public class MessageUtil {
    /**
     * @description: 消息发送
     * @param:发送消息的客户端，消息类型，消息内容
     * @return: void
     */
    public void sendMessage(Socket client, Message message) {
        //向服务器发送消息
        PrintWriter out;
        try {
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
            out.println(message.messageToString());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
