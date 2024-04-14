package com.wyz.socketchat.util;

import com.wyz.socketchat.bean.Message;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Yun
 * @description: 监听服务端广播转发消息的线程
 */
public class ListenThread extends Thread {
    public volatile boolean flag = true;
    private final Socket socket;
    private final ScrollPane chatArea;
    private final VBox textBox;
    private final String name;//当前线程所属线程
    JavaFXUtil javaFXUtil = new JavaFXUtil();

    public ListenThread(Socket socket, ScrollPane chatArea,VBox textBox,String name) {
        this.socket = socket;
        this.chatArea = chatArea;
        this.textBox = textBox;
        this.name = name;
    }

    @Override
    public void run() {
        //因为这个线程要修改主线程的内容，所以使用runLater方法
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            while (flag) {
                //解析数据
                String str = br.readLine();
                if (str != null) {
                    Message message = new Message().stringToMessage(str);
                    switch (message.getCode()) {
                        case '5':
                            //广播消息，填充文本
                            Platform.runLater(() ->
                                    javaFXUtil.addMessage(textBox,chatArea,
                                            javaFXUtil.getText(0,message.getData())));
                            break;
                        case '1':
                            //聊天消息，填充文本
                            int type;
                            if(message.getFromName().equals(name)){
                                //是自己发出的消息
                                Platform.runLater(() ->
                                        javaFXUtil.addMessage(textBox,chatArea,javaFXUtil.getText(
                                                2,message.getData())));
                            } else {
                                //是别人发出的消息
                                Platform.runLater(() ->
                                        javaFXUtil.addMessage(textBox,chatArea,javaFXUtil.getText(
                                                1,message.getFromName()+"说："+message.getData())));
                            }
                    }
                }
            }
        } catch (IOException ignored) {
            //忽视这个异常
        }
    }

}
