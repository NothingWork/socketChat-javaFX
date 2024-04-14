package com.wyz.socketchat.util;

import com.wyz.socketchat.bean.Message;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Yun
 * @description: 简化主界面函数提供的工具类
 */
public class JavaFXUtil {
    MessageUtil messageUtil = new MessageUtil();

    /**
     * @description: 登录操作
     * @param: 要登录的socket和用户名
     * @return: boolean
     */

    public boolean login(Socket socket,String name){
        boolean flag = false;
        //发送登录消息
        Message message = new Message('0',name.length(),name,0,"","");//客户=>服务
        messageUtil.sendMessage(socket,message);
        //监听登录反馈
        while (true){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                String str = br.readLine();
                if(str!=null){
                    Message logMsg = new Message().stringToMessage(str);
                    if(logMsg.getToName().equals(name)){
                        if(logMsg.getCode() == '4'){
                            //成功消息，服务端查验用户名成功，发送成功确认消息
                            message.setCode('7');
                            messageUtil.sendMessage(socket, message);
                            flag = true;
                            break;
                        }
                        else if(message.getCode() == '3'){
                            //失败消息，服务端查验用户名失败，发送失败确认消息，客户端被迫断连
                            message.setCode('6');
                            messageUtil.sendMessage(socket, message);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return flag;
    }

    /**
     * @description: 生成用于在聊天区域展示的文本
     * @param: 消息的类型代码,消息内容
     * @return: javafx.scene.text.Text
     */
    public TextFlow getText(int type, String message){
        Text text = new Text(message+"\n");
        TextFlow textFlow =new TextFlow();
        switch (type){
            case 0:
                //系统消息
                text.setFill(Paint.valueOf("#4e38e0"));
                text.setFont(Font.font("System", FontWeight.BOLD,15));
                textFlow.setTextAlignment(TextAlignment.CENTER);
                break;
            case 1:
                //他人消息
                textFlow.setTextAlignment(TextAlignment.LEFT);
                text.setFont(new Font(20));
                break;
            case 2:
                //自己消息
                textFlow.setTextAlignment(TextAlignment.RIGHT);
                text.setFont(new Font(20));
                break;
        }
        textFlow.getChildren().add(text);
        return textFlow;
    }

    /**
     * @description: 在聊天区域渲染新的消息
     * @param: 消息容器，渲染区域，消息内容
     * @return: void
     */
    public void addMessage(VBox textBox,ScrollPane chatArea,TextFlow tf){
        textBox.getChildren().add(tf);
        chatArea.setContent(textBox);
        chatArea.setVvalue(1.0);
    }
}
