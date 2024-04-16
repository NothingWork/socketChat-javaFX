package com.wyz.socketchat.util;

import com.wyz.socketchat.bean.Message;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

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
    private final ScrollPane chatArea;
    private final VBox textBox;
    private final ListView<String> listView;
    private final String name;//当前线程所属用户名
    JavaFXUtil javaFXUtil = new JavaFXUtil();
    MessageUtil messageUtil = new MessageUtil();

    //构造函数
    public ListenThread(Socket socket, ScrollPane chatArea, VBox textBox,ListView<String> listView,String name) {
        this.socket = socket;
        this.chatArea = chatArea;
        this.textBox = textBox;
        this.name = name;
        this.listView = listView;
    }

    //监听消息线程
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
                    int type = 0;//消息类型，默认为系统广播消息
                    boolean isSolo = false;//是否为私聊消息，默认为非私聊
                    switch (message.getCode()) {
                        case '5':
                            //广播消息
                            break;
                        case '1':
                            //群发消息，
                            if (message.getFromName().equals(name)) {
                                //是自己发出的消息
                                type = 2;
                            } else {
                                //是别人发出的消息
                                type = 1;
                                message.setData(message.getData() + "说：");
                            }
                            break;
                        case '8':
                            //私聊消息，
                            isSolo = true;
                            if (message.getFromName().equals(name)) {
                                //是自己发出的消息
                                type = 2;
                                message.setData("你对" + message.getToName() + "说：" + message.getData());
                            } else {
                                //是别人发出的消息
                                type = 1;
                                message.setData(message.getFromName() + "对你说：" + message.getData());
                            }
                            break;
                        case '9':
                            //更新消息，更新用户列表
                            Platform.runLater(() -> updateList(listView,message.getData()));
                            break;
                    }
                    //消息代码为 '9' 不用在聊天区域渲染消息
                    if(message.getCode()!='9'){
                        int finalType = type;
                        boolean finalIsSolo = isSolo;
                        Platform.runLater(() ->
                                javaFXUtil.addMessage(textBox, chatArea,
                                        javaFXUtil.getText(finalType, message.getData(), finalIsSolo)));
                    }
                }
            }
        } catch (IOException ignored) {
            //忽视这个异常
        }
    }

    /**
     * @description: 更新主界面的在线用户列表
     * @param:
     * @return:
     */
    public void updateList(ListView<String> listView, String str){
        //从字符串中分割list
        ObservableList<String> list = messageUtil.getListByStr(str); //新的在线列表
        list.remove(name);//去除自己
        list.add(0,"（选中以群发）");//添加群发选项

        //为了不影响用户原有的操作，对列表进行逐步更新而不是整个list直接替换
        //删除离线的
        for (String name:listView.getItems()
             ) {
            if(!list.contains(name)){
                //如果移除的是当前选中的，则改选中为0
                if(name.equals(listView.getSelectionModel().getSelectedItem())){
                    listView.getSelectionModel().select(0);
                }
                listView.getItems().remove(name);
            }
        }
        //添加上线的
        for (String name:list
             ) {
            if(!listView.getItems().contains(name)){
                listView.getItems().add(name);
            }
        }
        listView.setItems(list);
    }

}
