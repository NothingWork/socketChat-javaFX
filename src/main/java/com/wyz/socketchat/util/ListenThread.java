package com.wyz.socketchat.util;

import com.wyz.socketchat.bean.Message;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    private final Text receiver;
    private final String name;//当前线程所属用户名
    JavaFXUtil javaFXUtil = new JavaFXUtil();
    MessageUtil messageUtil = new MessageUtil();

    //构造函数
    public ListenThread(Socket socket, ScrollPane chatArea, VBox textBox,ListView<String> listView,Text receiver,String name) {
        this.socket = socket;
        this.chatArea = chatArea;
        this.textBox = textBox;
        this.name = name;
        this.listView = listView;
        this.receiver = receiver;
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
                    if (message.getFromName().equals(name)) {
                        //是自己发出的消息
                        type = 2;
                    } else if(!message.getFromName().equals("")) {
                        //是别人发出的消息
                        type = 1;
                    }
                    if(message.getCode()=='9'){
                        //更新消息，更新用户列表
                        Platform.runLater(() -> updateList(listView,message.getData()));
                    }
                    else{
                        //消息代码为 '9' 不用在聊天区域渲染消息,其余消息进行渲染
                        int finalType = type;
                        Platform.runLater(() ->
                                javaFXUtil.drawMessage(textBox, chatArea, finalType,message));
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
        //防止线程冲突，先复制一份
        List<String> copyList = new ArrayList<>(listView.getItems());

        for (String name:copyList
             ) {
            if(!list.contains(name)){
                //如果移除的是当前选中的，则改选中为0,接受者提示文本修改
                if(name.equals(listView.getSelectionModel().getSelectedItem())){
                    listView.getSelectionModel().select(0);
                    receiver.setFill(Paint.valueOf("#000000"));
                    receiver.setText("群发");
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
    }

}
