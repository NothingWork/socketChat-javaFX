package com.wyz.socketchat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * @author Yun
 * @version 1.0
 * @description: 主窗口程序
 * @date 2024/4/3
 */
public class MainApp extends Application {
    private Stage primaryStage;//窗口舞台

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        //防止主界面关闭窗口操作和托盘操作冲突
        Platform.setImplicitExit(false);//设置图标
        // 设置标题
        primaryStage.setTitle("简易聊天室");
        //设置图标
        primaryStage.getIcons().add(new Image("images/socketChat.png"));
        //窗口关闭事件,生成确认弹窗
        primaryStage.setOnCloseRequest(event -> {
            //取消关闭窗口关闭的默认事件，由弹窗决定是否退出程序
            event.consume();
            showAlert();
        });

        // 在程序启动的时候加载主界面
        initMainFrame();

    }

    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认");
        alert.setHeaderText("确认要退出吗？");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        // 添加事件处理器
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                //确认关闭,关闭窗口
                    System.exit(0);
            }
        });
    }

    //创建主界面
    private void initMainFrame() {
        try {
            // 加载FXML界面文件
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainFrame.fxml"));
            Parent root = loader.load();
            // 实例化场景
            Scene scene = new Scene(root);
            // 将场景设置到舞台
            primaryStage.setScene(scene);
            // 展示舞台
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //启动主界面窗口
        launch(args);
    }
}
