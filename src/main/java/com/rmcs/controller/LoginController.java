package com.rmcs.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private AnchorPane root;
    @FXML private HBox titleBar;
    @FXML private TextField accountField;
    @FXML private PasswordField passwordField;

    private double offsetX;
    private double offsetY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 标题栏拖拽移动窗口
        titleBar.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setX(event.getScreenX() - offsetX);
            stage.setY(event.getScreenY() - offsetY);
        });

        // 回车快捷登录
        accountField.setOnAction(e -> onLogin());
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        String account = accountField.getText() == null ? "" : accountField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (account.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "登录提示", "请输入账号和密码");
            return;
        }

        // TODO: 接入真实账号校验逻辑
        if ("admin".equals(account) && "123456".equals(password)) {
            enterMainPage();
        } else {
            showAlert(Alert.AlertType.ERROR, "登录失败", "账号或密码错误，请重新输入");
        }
    }

    /** 登录成功，切换到主控页面 */
    private void enterMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent mainRoot = loader.load();
            Scene scene = new Scene(mainRoot, 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("自动测阻机控制系统");
            stage.setScene(scene);
            stage.setMinWidth(1120);
            stage.setMinHeight(680);

            // 按屏幕可视区域自适应，避免窗口被任务栏/屏幕边缘裁切
            javafx.geometry.Rectangle2D vb = Screen.getPrimary().getVisualBounds();
            double w = Math.min(1280, vb.getWidth() - 40);
            double h = Math.min(800, vb.getHeight() - 64);
            stage.setWidth(w);
            stage.setHeight(h);
            stage.setX(vb.getMinX() + (vb.getWidth() - w) / 2);
            stage.setY(vb.getMinY() + (vb.getHeight() - h) / 2);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "系统错误", "主页面加载失败：" + e.getMessage());
        }
    }

    @FXML
    private void onReset() {
        accountField.clear();
        passwordField.clear();
        accountField.requestFocus();
    }

    @FXML
    private void onMin() {
        ((Stage) root.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void onClose() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要退出系统吗？", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("退出确认");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
                .addAll(((Stage) root.getScene().getWindow()).getIcons());
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                ((Stage) root.getScene().getWindow()).close();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
