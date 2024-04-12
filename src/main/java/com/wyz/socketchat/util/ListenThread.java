package com.wyz.socketchat.util;

import com.wyz.socketchat.controller.MainFrameController;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Yun
 * @description: 监听服务端广播转发消息的线程
 */
public class ListenThread extends Thread {
    public volatile boolean flag = true;

    private final Socket socket;
    private final TextArea textArea;

    public ListenThread(Socket socket, TextArea textArea) {
        this.socket = socket;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        MessageUtil messageUtil = new MessageUtil();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            while (flag) {
                //解析数据
                String str = br.readLine();
                if (str != null) {
                    char code = str.charAt(0);
                    String data = str.substring(1);
                    switch (code) {
                        case '3':
                            //失败消息，服务端查验用户名失败，发送失败确认消息，客户端被迫断连
                            messageUtil.sendMessage(socket, 6, data);
                            new MainFrameController().quitConnect();
                            break;
                        case '4':
                            //成功消息，服务端查验用户名成功，发送成功确认消息
                            messageUtil.sendMessage(socket, 7, data);
                            break;
                        case '5':
                            //广播消息，填充文本域
                            textArea.appendText(data+"\n");
                            break;
                    }
                }
            }
        } catch (IOException ignored) {
            //忽视这个异常
        }
    }
}
