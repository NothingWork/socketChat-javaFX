package com.wyz.socketchat.controller;

import com.wyz.socketchat.bean.Message;
import com.wyz.socketchat.util.JavaFXUtil;
import com.wyz.socketchat.util.ListenThread;
import com.wyz.socketchat.util.MessageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Yun
 * @description: 主窗口类
 */
public class MainFrameController {
    private Socket client;//连接的socket
    ListenThread listenThread;//监听消息的进程
    MessageUtil messageUtil = new MessageUtil();//发送消息工具类
    Message message = new Message();//消息类包装
    JavaFXUtil javaFXUtil = new JavaFXUtil();
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
    public ScrollPane chatArea;//聊天信息区域
    @FXML
    private Button sendMessageBtn;//发送消息按钮
    @FXML
    public VBox textBox;//包裹消息的内容器
    @FXML
    public ListView<String> userList;//包含在线用户的列表


    /**
     * @description: 连接聊天服务器，点击连接按钮启用
     * @param:
     * @return: void
     */
    @FXML
    void connect() {

        if (ipField.getText().trim().equals("") || portField.getText().trim().equals("")
                || nameField.getText().trim().equals("")) {
            javaFXUtil.addMessage(textBox, chatArea,javaFXUtil.getText(0,"请填写必要内容",false));
        } else {
            try {
                //检查端口合法性
                int port = Integer.parseInt(portField.getText());
                if (port < 1 || port > 65535) {
                    ((VBox) chatArea.getContent()).getChildren().add(javaFXUtil.getText(0, "端口非法",false));
                    portField.clear();
                }
                //进行登录，判断结果
                client = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
                boolean bool = javaFXUtil.login(client, nameField.getText());
                //登录成功
                if (bool) {
                    //组件状态改变
                    setDisable(true);
                    //开启监听线程
                    listenThread = new ListenThread(client, chatArea, textBox,userList,nameField.getText());
                    listenThread.start();
                    //消息类封装（用于统一的消息发送）
                    message.setFromLen(nameField.getText().length());
                    message.setFromName(nameField.getText());
                }
                //登录失败
                else {
                    javaFXUtil.addMessage(textBox, chatArea,javaFXUtil.getText(0,"该用户名已被占用",false));
                }
            } catch (IOException e) {
                javaFXUtil.addMessage(textBox, chatArea,javaFXUtil.getText(0,"服务器连接失败",false));
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
            //消息类封装
            message.setCode('1');
            message.setToLen(0);
            message.setToName("");
            message.setData(str);
            //发送出去消息
            messageUtil.sendMessage(client,message);
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
    void quitConnect() {
        try {
            //关闭监听线程
            listenThread.flag = false;
            //发送断连消息
            message.setCode('2');
            message.setToLen(0);
            message.setToName("");
            message.setData("");
            messageUtil.sendMessage(client,message);
            //messageUtil.sendMessage(client, 2, nameField.getText());
            //释放资源与组件状态调整
            client.close();
            setDisable(false);
            javaFXUtil.addMessage(textBox, chatArea,javaFXUtil.getText(0,"已断开与服务器的连接",false));
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

    public void setDisable(Boolean bool) {
        //连接状态bool为false，未连接为true
        ipField.setDisable(bool);
        portField.setDisable(bool);
        nameField.setDisable(bool);

        quitBtn.setDisable(!bool);
        sendMessageBtn.setDisable(!bool);
        connectBtn.setDisable(bool);

    }

    public void sendByKeyboard(KeyEvent event) {
        if (event.getCode() == KeyCode.getKeyCode("Enter")) {
            event.consume();
            sendMessageBtn.fire();
        }
    }
}
