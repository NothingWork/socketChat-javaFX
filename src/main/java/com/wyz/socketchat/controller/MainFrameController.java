package com.wyz.socketchat.controller;

import com.wyz.socketchat.util.ListenThread;
import com.wyz.socketchat.util.MessageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yun
 * @description: 主窗口类
 */
public class MainFrameController {
    private Socket client;//连接的socket
    ListenThread listenThread;//监听消息的进程
    MessageUtil messageUtil = new MessageUtil();
    @FXML
    private TextField ipField;//填写聊天服务器的ip
    @FXML
    private TextField portField;//填写聊天服务器的端口
    @FXML
    private TextField nameField;//填写用户名
    @FXML
    private Button connectBtn;//连接服务器按钮
    @FXML
    private Button quitBtn;//断开服务器连接按钮
    @FXML
    private TextArea typeArea;//打字区域
    @FXML
    private TextArea mainWindow;//主窗口
    @FXML
    private Button sendMessageBtn;//发送消息按钮



    /**
     * @description: 连接聊天服务器，点击连接按钮启用
     * @param:
     * @return: void
     */
    @FXML
    void connect() {
        if (ipField.getText().trim().equals("") || portField.getText().trim().equals("")
                || nameField.getText().trim().equals("")) {
            mainWindow.appendText("请检查必要信息的填写!\n");
        } else {
            try {
                //检查端口合法性
                int port = Integer.parseInt(portField.getText());
                if(port<1||port>65535){
                    mainWindow.appendText("端口非法!");
                    portField.clear();
                }
                //发送服务器连接消息
                client = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
                messageUtil.sendMessage(client,0,nameField.getText());

                //部分组件状态改变
                ipField.disableProperty().set(true);
                portField.disableProperty().set(true);
                nameField.disableProperty().set(true);
                connectBtn.disableProperty().set(true);
                quitBtn.disableProperty().set(false);
                sendMessageBtn.disableProperty().set(false);

                //开启监听线程
                listenThread = new ListenThread(client,mainWindow);
                listenThread.start();
            } catch (IOException e) {
                mainWindow.appendText("服务器连接失败!\n");
            }
        }
    }

    /**
     * @description: 发送消息事件，点击发送按调用
     * @param:
     * @return: void
     */
    @FXML
    void sendMessage() {
        if (typeArea.getText().equals("")) {
            System.out.println("没打字，处理一下");
        } else {
            //文本框消息
            String str = typeArea.getText();
            //发送出去消息
            messageUtil.sendMessage(client,1,nameField.getText()+": "+str);
            //清空发送区域
            typeArea.setText("");
        }
    }
    /**
     * @description: 断开与服务器的连接
     * @param:
     * @return: void
     */
    @FXML
    public void quitConnect() {
        try {
            //关闭监听线程
            listenThread.flag = false;
            //发送断连消息
            messageUtil.sendMessage(client,2,nameField.getText());
            //释放资源与组件状态调整
            client.close();
            ipField.disableProperty().set(false);
            portField.disableProperty().set(false);
            nameField.disableProperty().set(false);
            quitBtn.disableProperty().set(true);
            sendMessageBtn.disableProperty().set(true);
            connectBtn.disableProperty().set(false);
            mainWindow.appendText("已断开与服务器的连接!\n");
        } catch (IOException e) {
            System.out.println("断开连接失败");
        }
    }
    /**
     * @description: 初始化加载，程序默认调用
     * @param:
     * @return: void
     */
    @FXML
     void initialize() {
        portCheck();//启用端口号文本框输入检查
        //禁用断开部分组件
        quitBtn.disableProperty().set(true);
        sendMessageBtn.disableProperty().set(true);
    }


    /**
     * @description: 对端口号输入窗口的输入检查
     * @param:
     * @return: void
     */
    public void portCheck() {
        portField.setTextFormatter(new TextFormatter<>(change -> {
            //如果字符符合正则则返回可以change,否则不改变
            String str = change.getText();
            if (str.matches("[0-9]") || str.equals("")) return change;
            return null;
        }));
    }

}
