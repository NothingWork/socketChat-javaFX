package org.example.thread;

import com.alibaba.fastjson.JSONObject;
import org.example.bean.Message;
import org.example.gpt.Spark;
import org.example.util.MessageUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;

/**
 * @author Yun
 * @description: 讯飞星火线程
 */
public class sparkThread extends Thread {
    private WebSocket webSocket;//发起请求的socket
    private JSONObject requestJSON;//请求消息
    MessageUtil messageUtil = new MessageUtil();

    private boolean flag = true;
    @Override
    public void run() {
        try {
        //连接聊天服务器
        Socket client = connect();
        //初始化spark
        Spark spark = new Spark(client);
        //监听消息
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            while (true) {
                String str = br.readLine();
                if (str != null) {
                    Message message = new Message().stringToMessage(str);
                    //ai只对私聊消息做出反应
                    if(message.getCode()=='8'){
                        if(message.getFromName().equals("讯飞星火")){
                            //如果是自己的小证明上次提问已经完成
                            flag = true;
                        }
                        else askThread(spark,message);//考虑到多人同时提问情况，另开提问线程
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: 连接至聊天服务器
     * @param:
     * @return: java.net.Socket
     */
    public Socket connect() {
        Socket client;
        try {
            client = new Socket("127.0.0.1", 8080);
            //登录服务器
            if (!login(client, "讯飞星火")) {
                //登陆失败
                System.out.println("讯飞星火连接服务器失败!");
            }
            //开启消息监听线程
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return client;

    }

    /**
     * @description: 登录至聊天服务器
     * @param: 连接的客户端和用户名
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
     * @description: 提问线程
     * @param: ai对象，问题消息
     * @return: void
     */
    public void askThread(Spark spark, Message message) throws Exception {

        Runnable runnable = () -> {

                try {
                    while(true){
                        System.out.println("排队");
                    Thread.sleep(2000);
                        if(flag)break;
                    }

                    //发送消息
                    spark.sendMessage(message.getFromName(),message.getData());
                    System.out.println("提问了"+message.getData());
                    flag = false;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        };
        Thread thread =new Thread(runnable);
        thread.start();

    }

}
