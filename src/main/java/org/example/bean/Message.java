package org.example.bean;

/**
 * @author Yun
 * @description: 消息类
 */
public class Message {
    private char code;//消息类型
    private int fromLen;//发送者姓名长度，<10
    private String fromName;//发送者姓名
    private int toLen;//接收者姓名长度，<10
    private String toName;//接收者姓名
    private String data;//消息详细内容

    public Message(char code, int fromLen, String fromName, int toLen, String toName, String data) {
        this.code = code;
        this.fromLen = fromLen;
        this.fromName = fromName;
        this.toLen = toLen;
        this.toName = toName;
        this.data = data;
    }

    public Message() {}

    public char getCode() {
        return code;
    }

    public void setCode(char code) {
        this.code = code;
    }

    public int getFromLen() {
        return fromLen;
    }

    public void setFromLen(int fromLen) {
        this.fromLen = fromLen;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public int getToLen() {
        return toLen;
    }

    public void setToLen(int toLen) {
        this.toLen = toLen;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String messageToString(){
        return ""+code+fromLen+fromName+toLen+toName+data;
    }

    /**
     * @description: 消息报文切割
     * @param: 消息报文拼接的字符串
     * @return: org.example.bean.Message
     */
    public Message stringToMessage(String str){
        //对字符串进行切割
        char code = str.charAt(0);
        int fromLen = Integer.parseInt(str.substring(1,2));
        String fromName = str.substring(2,fromLen+2);
        int toLen = Integer.parseInt(str.substring(2+fromLen,3+fromLen));
        String toName = str.substring(3+fromLen,3+fromLen+toLen);
        String data = str.substring(3+fromLen+toLen);

        return new Message(code,fromLen,fromName,toLen,toName,data);
    }

    /**
     * @description: 调换发送者和接收者
     * @param: 无
     * @return: message
     */
    public Message reverse(){
        int fromLen = this.fromLen;
        String fromName = this.fromName;

        this.fromLen = this.toLen;
        this.fromName = this.toName;

        this.toLen = fromLen;
        this.toName = fromName;

        return this;
    }
}
