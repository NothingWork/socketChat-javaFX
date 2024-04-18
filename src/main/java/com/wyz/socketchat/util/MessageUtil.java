package com.wyz.socketchat.util;

import com.wyz.socketchat.bean.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * @description: 获取在线用户列表
     * @param: 包含用户列表的字符串
     * @return: javafx.collections.ObservableList<java.lang.String>
     */
    public ObservableList<String> getListByStr(String str){
        ObservableList<String> list = FXCollections.observableArrayList();
        int begin = 0;//其实分割位置
        while(true){
            int nameLen = Integer.parseInt(str.substring(begin,begin+1));
            if(nameLen == 0) break;//用户名长为0，说明到结束标志了
            String name = str.substring(begin+1,begin+1+nameLen);
            list.add(name);
            begin = begin+1+nameLen;
        }
        return list;
    }


    public String packString(int length,String str){
        str = " "+str+" ";
        StringBuilder sb = new StringBuilder(str);
        if(length>230){

        }

return null;
    }
}
