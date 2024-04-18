package org.example.thread;

import org.example.bean.Message;
import org.example.socketServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Yun
 * @description: 为每个客户端连接建立对应的线程
 */
public class serverThread extends Thread {
    public Socket client;
    private BufferedReader br;

    public serverThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            br = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            while (true) {
                String str = br.readLine();
                if (str != null) {
                    Message message = new Message().stringToMessage(str);
                    switch (message.getCode()) {
                        case '0':
                            //连接消息,查验通过广播成功消息，查验失败广播失败消息
                            socketServer.map.put(client, "");
                            if (checkName(message.getFromName())) {
                                //告诉客户端上线成功
                                message.setCode('4');
                                socketServer.map.put(client, message.getFromName());//添加登录用户的到map中
                                sendMessage(message.reverse(), client);
                            } else {
                                //告诉客户端上线失败
                                message.setCode('3');
                                sendMessage(message.reverse(), client);
                            }
                            break;
                        case '1':
                            //群发消息，client为空
                            sendMessage(message, null);
                            break;
                        case '8':
                            //私聊消息，进行转发
                            //1.对自己转发以表明发送成功
                            sendMessage(message, getSocketByName(message.getFromName()));
                            //2.向目标发送一份
                            sendMessage(message, getSocketByName(message.getToName()));
                            break;
                        case '2':
                            //断连消息，广播下线消息，更新在线用户列表
                            message.setCode('5');
                            message.setData(message.getFromName() + "离开了");
                            sendMessage(message.reverse(), null);
                            socketServer.map.remove(client);
                            sendNames();
                            break;
                        case '6':
                            //失败确认，后续直接结束线程
                            socketServer.map.remove(client);
                            break;
                        case '7':
                            //成功确认，广播上线消息，更新在线用户列表
                            message.setCode('5');
                            message.setData(message.getFromName() + "上线了");
                            sendMessage(message.reverse(), null);
                            sendNames();
                            break;
                    }
                    //客户端发来断连消息或者失败确认，结束线程
                    if (message.getCode() == '2' || message.getCode() == '6') {
                        br.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            //客户端下线，释放资源
            System.out.println(client.getInetAddress() + ":" + client.getPort() + "下线了");
            try {
                br.close();
                socketServer.map.remove(client);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * @description: 检查用户名是否重复
     * @param: 要检查的用户名
     * @return: boolean
     */
    public boolean checkName(String username) {
        for (Map.Entry<Socket, String> entry : socketServer.map.entrySet()) {
            if (username.equals(entry.getValue())) return false;
        }
        return true;
    }

    /**
     * @description: 向所有客户端广播消息
     * @param: 要发送的消息，要发送至的socket
     * @return: void
     */
    public void sendMessage(Message message, Socket socket) {
        //聊天信息分为群发和私聊消息，群发消息传入的socket为空
        try {
            //私聊消息
            if (socket != null) {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                pw.println(message.messageToString());
                pw.flush();
            } else {
                //群发消息
                for (Map.Entry<Socket, String> entry : socketServer.map.entrySet()
                ) {
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(entry.getKey().getOutputStream(), StandardCharsets.UTF_8));
                    pw.println(message.messageToString());
                    pw.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: 根据用户名找到对应的socket
     * @param: 用户名
     * @return: java.net.Socket
     */
    public Socket getSocketByName(String name) {
        Socket socket = null;
        for (Map.Entry<Socket, String> entry : socketServer.map.entrySet()
        ) {
            if (entry.getValue().equals(name)) {
                socket = entry.getKey();
                break;
            }
        }
        return socket;
    }

    /**
     * @description: 向所哟用户发送一条包含当前所有在线用户的用户名的消息
     * @param:
     * @return:
     */
    public void sendNames() {
        //拼接用户名
        StringBuilder list = new StringBuilder();
        for (Map.Entry<Socket, String> entry : socketServer.map.entrySet()
        ) {
            //拼接携带用户名的字符串(用户名长度/用户名/用户名长度/用户名......)
            String name = entry.getValue();
            String nameLen = String.valueOf(name.length());
            list.append(nameLen).append(name);
        }
        list.append("0");//结束标志
        //发送消息
        sendMessage(new Message('9', 0, "", 0, "", list.toString()), null);
    }
}
