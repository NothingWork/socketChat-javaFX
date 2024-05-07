package org.example.gpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import okhttp3.*;
import org.example.bean.Message;
import org.example.bean.RoleContent;
import org.example.util.MessageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Yun
 * @description: 建立与spark的连接和消息收发
 */
public class Spark extends WebSocketListener {
    private static final String hosturl = "https://spark-api.xf-yun.com/v3.5/chat";//地址
    //鉴权信息
    private static final String appid = "6ea1b830";
    private static final String apiSecret = "MDc5Njg0YmRjM2JjZjRiODJjODRkY2Iz";
    private static final String apiKey = "4a4016620f417284d035bd39be2405d8";
    public static List<RoleContent> historyList = new ArrayList<>();//对话历史
    public  StringBuilder totalAnswer = new StringBuilder();//gpt答案汇总
    private WebSocket webSocket;
    private Socket scoket;//连接聊天服务器
    private static String toName = "";//提问者的姓名
    private static final Gson gson = new Gson();
    private MessageUtil messageUtil = new MessageUtil();

    public Spark(Socket scoket) {
        this.scoket = scoket;
    }

    /**
     * @description:初始化
     * @param:
     * @return: void
     */
    public void sendMessage(String name,String question) throws Exception{
        //构造鉴权url
        String authUrl = getAuthUrl(hosturl,apiKey,apiSecret);
        //构建http请求体
        OkHttpClient client = new OkHttpClient.Builder().build();
        String url = authUrl.replace("http://","ws://").
                replace("https://","wss://");
        //构建请求体
        Request request = new Request.Builder().url(url).build();
        //开启websocket连接
        webSocket = client.newWebSocket(request,new Spark(scoket));
        //修改提问者
        toName = name;
        //发送消息
        webSocket.send(packMessage(name,question).toString());
    }


    /**
     * @description: 获得鉴权
     * @param: 连接地址，key，secret
     * @return: java.lang.String
     */
    public String getAuthUrl(String hosturl,String apiKey,String apiSecret) throws Exception {
        URL url = new URL(hosturl);
        //时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        //对apiSecret加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        //Base64编码生成signature
        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        return httpUrl.toString();
    }

    /**
     * @description: 包装用户发送的提问消息
     * @param:发出提问的用户名
     * @return: com.alibaba.fastjson.JSONObject
     */
    public JSONObject packMessage(String name,String question){
        JSONObject requestJson = new JSONObject();

        JSONObject header = new JSONObject();
        header.put("app_id",appid);
        header.put("uid",name);//传入用户名

        JSONObject parameter = new JSONObject();
        JSONObject chat = new JSONObject();
        chat.put("domain","generalv3.5");
        chat.put("temperature",0.5);
        chat.put("max_tokens",8192);
        parameter.put("chat",chat);

        JSONObject payload = new JSONObject();
        JSONObject message = new JSONObject();
        JSONArray text = new JSONArray();
        
        //历史消息填充
        for (RoleContent RC:historyList
             ) {
            text.add(JSON.toJSON(RC));
        }

        //问题
        RoleContent roleContent = new RoleContent("user",question);
        text.add(roleContent);
        historyList.add(roleContent);

        message.put("text",text);
        payload.put("message",message);

        requestJson.put("header",header);
        requestJson.put("parameter",parameter);
        requestJson.put("payload",payload);

        return requestJson;

    }

    /**
     * @description: 剪切历史消息记录
     * @param:
     * @return: void
     */
    public void cutHistory(){
        int historyLen = 0;
        for (RoleContent RC:historyList
             ) {
            historyLen+=RC.getContent().length();
        }
        if(historyLen>12000){
            historyList.remove(0);
            historyList.remove(1);
            historyList.remove(2);
            historyList.remove(3);
            historyList.remove(4);
        }
    }
    //websocket打开
    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
    }

    //收到消息，对消息解析
    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);
        Answer answer = gson.fromJson(text,Answer.class);
        //System.out.println(text);
        if(answer.header.code != 0){
            System.out.println("回答出错");
            webSocket.close(1000,"");
        }
        //AI回答部分
        List<RoleContent> textList = answer.payload.choices.text;
        for (RoleContent RC:textList
             ) {
            totalAnswer.append(RC.getContent().replaceAll("\n",""));
        }
        //接收到最后一个结果
        if(answer.header.status == 2){
            //裁剪历史消息队列
            cutHistory();
            //将此次对话添加进历史消息
            historyList.add( new RoleContent("assistant",totalAnswer.toString()));
            //发送答案至聊天服务器
            Message message = new Message('8',4,"讯飞星火",toName.length(),
                    toName,totalAnswer.toString());
            messageUtil.sendMessage(scoket,message);
        }

    }

    //对话失败
    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        //提问包含非法信息
        //发送答案至聊天服务器
        Message message = new Message('8',4,"讯飞星火",toName.length(),toName,"听不懂捏~");
        messageUtil.sendMessage(scoket,message);

        System.out.println("对话失败");
        if (response != null) {
            System.out.println("错误码："+ response.code());
            System.out.println("错误响应"+response.body().toString());
        }
        else{
            System.out.println("null");
        }

    }

    //消息解析类
    class Answer{
        Header header;
        Payload payload;
    }
     class Header{
        int code;
        int status;
        String sid;
    }
     class Payload{
        Choices choices;
    }
     class Choices{
        List<RoleContent> text;
    }

}


































