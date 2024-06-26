package com.wyz.socketchat.controller;

import com.alibaba.fastjson.JSONObject;
import com.wyz.socketchat.bean.Message;
import com.wyz.socketchat.util.JavaFXUtil;
import com.wyz.socketchat.util.ListenThread;
import com.wyz.socketchat.util.MessageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
    private AnchorPane root;//根节点场景
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
    private Button sendFileBtn;//发送文件按钮
    @FXML
    public VBox textBox;//包裹消息的内容器
    @FXML
    public ListView<String> userList;//包含在线用户的列表
    @FXML
    private Text receiver;//消息接收者


    /**
     * @description: 连接聊天服务器，点击连接按钮启用
     * @param:
     * @return: void
     */
    @FXML
    void connect() {

        if (ipField.getText().trim().equals("") || portField.getText().trim().equals("")
                || nameField.getText().trim().equals("")) {
            message.setData("请完善必要信息");
            javaFXUtil.drawMessage(textBox, chatArea,0,message);
        } else {
            try {
                //检查端口合法性
                int port = Integer.parseInt(portField.getText());
                if (port < 1 || port > 65535) {
                    message.setData("端口非法");
                    javaFXUtil.drawMessage(textBox, chatArea,0,message);
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
                    listenThread = new ListenThread(client, chatArea, textBox,userList,receiver,nameField.getText());
                    listenThread.start();
                    //消息类封装（用于统一的消息发送）
                    message.setFromLen(nameField.getText().length());
                    message.setFromName(nameField.getText());
                    //填充在线列表,默认选中群发
                    userList.getItems().add("（选中以群发）");
                    userList.getSelectionModel().select(0);
                    receiver.setText("群发");
                    receiver.setFill(Paint.valueOf("#000000"));
                }
                //登录失败
                else {
                    message.setData("该用户名已被占用");
                    javaFXUtil.drawMessage(textBox, chatArea,0,message);
                }
            } catch (IOException e) {
                message.setData("服务器连接失败");
                javaFXUtil.drawMessage(textBox, chatArea,0,message);
            }
        }
    }

    /**
     * @description: 发送消息按钮，点击发送按调用
     * @param:
     * @return: void
     */
    void sendMessage(char code,String data) {
        if (!data.equals("")) {
            //消息类封装
            String toName = userList.getSelectionModel().getSelectedItem();//选中的发送人
            message.setToLen(toName.length());
            message.setToName(toName);
            message.setData(data);
            //判断是否为私发或者为文件消息
            if (userList.getSelectionModel().getSelectedIndex() == 0 && code!='I') message.setCode('1');
            else message.setCode(code);
            //发送出去消息
            messageUtil.sendMessage(client, message);
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
            //释放资源与组件状态调整
            client.close();
            setDisable(false);
            message.setData("已断开与服务器的连接");
            javaFXUtil.drawMessage(textBox, chatArea,0,message);
            userList.getItems().clear();
            receiver.setText("");
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
        nameCheck();//启用用户名检查
        //禁用部分组件
        quitBtn.disableProperty().set(true);
        sendFileBtn.disableProperty().set(true);
        //消息基本信息编写
        message.setCode('5');
        message.setFromName("");

        //按下ctrl+enter换行
        typeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                int caretPosition = typeArea.getCaretPosition();
                String text = typeArea.getText();
                String newText = text.substring(0, caretPosition) + "\n" + text.substring(caretPosition);
                typeArea.setText(newText);
                typeArea.positionCaret(typeArea.getLength()); // 将光标放在最后
                event.consume();
            }
        });
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

    /**
     * @description: 对用户名输入窗口的输入检查
     * @param:
     * @return: void
     */
    public void nameCheck() {
        nameField.setTextFormatter(new TextFormatter<>(change -> {
            //如果用户名长度<10可以change,否则不改变
            if (nameField.getText().length()<9 || change.getText().equals("")) return change;
            return null;
        }));
    }

    /**
     * @description: 改变组件是否被禁用的状态
     * @param: 要改变的状态
     * @return: void
     */

    public void setDisable(Boolean bool) {
        //连接状态bool为false，未连接为true
        ipField.setDisable(bool);
        portField.setDisable(bool);
        nameField.setDisable(bool);

        quitBtn.setDisable(!bool);
        sendFileBtn.setDisable(!bool);
        connectBtn.setDisable(bool);

    }

    /**
     * @description: 在文本输入区域按下回车键发送消息
     * @param: event
     * @return: void
     */
    public void sendByKeyboard(KeyEvent event) {
        if (event.getCode() == KeyCode.getKeyCode("Enter")) {
            event.consume();
            //发送消息
            sendMessage('8',typeArea.getText());
            //清空发送区域
            typeArea.setText("");
        }
    }

    /**
     * @description: 监听列表点击事件。将当前选中的接收者渲染到提示区域
     * @param: event
     * @return: void
     */
    public void listClick(MouseEvent mouseEvent) {
        //1.新选中群发
        if(userList.getSelectionModel().getSelectedIndex()==0){
            receiver.setFill(Paint.valueOf("#000000"));
            receiver.setText("群发");
        }
        //2.选中私聊
        else{
            receiver.setFill(Paint.valueOf("#05ad1f"));
            receiver.setText(userList.getSelectionModel().getSelectedItem());
        }
    }

    /**
     * @description: 发送文件
     * @param:
     * @return: void
     */
    @FXML
     void sendFile(ActionEvent actionEvent) {
        //发送消息
        sendMessage('8',typeArea.getText());
        //清空发送区域
        typeArea.setText("");

        //传输文件系统，待完善
    }

    public void writeFile(){
        try {
            //1.选择文件
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            Stage stage = (Stage) root.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                //2.写文件
                FileInputStream fis = new FileInputStream(selectedFile);
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                dos.writeUTF(selectedFile.getName());//文件名
                dos.writeLong(selectedFile.length());//文件长度
                dos.flush();
                System.out.println("开始传输文件");
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes,0,bytes.length)) != -1){
                    dos.write(length);
                    dos.flush();
                }
                System.out.println("传输完成");
                fis.close();
                dos.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
