package com.wyz.socketchat.util;

import com.wyz.socketchat.bean.Message;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

    public boolean login(Socket socket, String name) {
        boolean flag = false;
        //发送登录消息
        Message message = new Message('0', name.length(), name, 0, "", "");//客户=>服务
        messageUtil.sendMessage(socket, message);
        //监听登录反馈
        while (true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                String str = br.readLine();
                if (str != null) {
                    Message logMsg = new Message().stringToMessage(str);
                    if (logMsg.getCode() == '4') {
                        //成功消息，服务端查验用户名成功，发送成功确认消息
                        message.setCode('7');
                        messageUtil.sendMessage(socket, message);
                        flag = true;
                        break;
                    } else if (logMsg.getCode() == '3') {
                        //失败消息，服务端查验用户名失败，发送失败确认消息，客户端被迫断连
                        message.setCode('6');
                        messageUtil.sendMessage(socket, message);
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return flag;
    }

    /**
     * @description: 在消息区域画上一个消息框
     * @param: vbox容器，消息区域，消息类型，消息内容，消息发送者，是否为私聊消息
     * @return:
     */
    public void drawMessage(VBox textBox, ScrollPane chatArea, int type,Message message) {
        Text messageText = new Text(message.getData());//消息内容
        double textLen = messageText.getLayoutBounds().getWidth();//消息文本长度
        TextFlow textFlow = new TextFlow();//装备消息内容的消息盒子
        HBox messageBox = new HBox();//装备消息盒子的外容器

        Text nameText = new Text(message.getFromName());//发送者姓名
        HBox nameBox = new HBox(nameText);//装备发送者姓名的外容器

        switch (type) {
            case 0:
                //系统消息
                messageText.setFill(Paint.valueOf("#ffffff"));
                messageText.setFont(Font.font("System", FontWeight.BOLD, 15));
                textFlow.getStyleClass().add("text-flow1");
                messageBox.setStyle("-fx-alignment: center");
                nameBox.setStyle("-fx-alignment: center");
                break;
            case 1:
                //他人消息
                textFlow.getStyleClass().add("text-flow3");
                messageText.setFont(new Font(20));
                messageBox.setStyle("-fx-alignment: top-left");
                nameBox.setStyle("-fx-alignment: top-left");
                break;
            case 2:
                //自己消息
                textFlow.getStyleClass().add("text-flow3");
                messageText.setFont(new Font(20));
                messageBox.setStyle("-fx-alignment: top-right");
                nameBox.setStyle("-fx-alignment: top-right");
                break;
        }
        //私聊消息再染白色
        if (message.getCode()== '8'){
            if(type==2){
                nameText.setText("(私)"+nameText.getText()+"->"+message.getToName());
            }
            nameText.setFill(Paint.valueOf("#ffffff"));
            messageText.setFill(Paint.valueOf("#ffffff"));
        }
        //消息盒子长宽包装
        textFlow.setMaxWidth(260);
        //textFlow.setMinHeight(((int) textLen/175)*30+35);
        textBox.setStyle("-fx-padding: 5 15 0 15");

        //容器包装 message->textFlow->messageBox->vbox->scroll-pane
        //       name->nameBox->Vbox->scroll-pane
        textFlow.getChildren().add(messageText);
        messageBox.getChildren().add(textFlow);
        textBox.getChildren().addAll(nameBox,messageBox);
        chatArea.setContent(textBox);
        chatArea.vvalueProperty().bind(textBox.heightProperty());//自动滚动
    }
}
