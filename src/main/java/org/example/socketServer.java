package org.example;

import org.example.thread.serverThread;
import org.example.thread.sparkThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yun
 * @version 1.0
 * @description: 用于socket通信的服务器
 * @date 2024/4/4
 */
public class socketServer {
    public static Map<Socket, String> map = new ConcurrentHashMap<>();//保存<客户端,用户名>的map

    public static void main(String[] args) {
        ServerSocket server;
        //打开服务器端口,连接讯飞星火接口
        try {
            server = new ServerSocket(8080);
            new Thread(new sparkThread()).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //监听客户端连接
        while (true) {
            try {
                Socket client = server.accept();
                System.out.println(client.getInetAddress() + ":" + client.getPort() + "上线");
                //启动多线程
                new Thread(new serverThread(client)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
