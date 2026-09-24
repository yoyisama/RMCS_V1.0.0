package com.rmcs.controller;

import com.rmcs.App;
import com.rmcs.model.AuthUser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/** 登录页：账号密码校验（admin / 123456），成功后交由 App 切换主控台。 */
public class LoginController {

    /** 默认操作员凭证。 */
    private static final String DEFAULT_ACCOUNT = "admin";
    private static final String DEFAULT_PASSWORD = "123456";

    @FXML private HBox root;
    @FXML private TextField accountField;
    @FXML private PasswordField passwordField;
    @FXML private TextField tfPwdVisible;
    @FXML private Button btnShowPwd;

    private App app;
    private boolean passwordVisible;

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        accountField.setText(DEFAULT_ACCOUNT);
        passwordField.setText(DEFAULT_PASSWORD);
        tfPwdVisible.setText(DEFAULT_PASSWORD);
        // 回车快捷登录
        accountField.setOnAction(e -> onLogin());
        passwordField.setOnAction(e -> onLogin());
        tfPwdVisible.setOnAction(e -> onLogin());
    }

    /** 密码可见切换（眼睛按钮）。 */
    @FXML
    public void togglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            tfPwdVisible.setText(passwordField.getText());
            tfPwdVisible.setVisible(true);
            tfPwdVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            btnShowPwd.setText("🙈");
        } else {
            passwordField.setText(tfPwdVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            tfPwdVisible.setVisible(false);
            tfPwdVisible.setManaged(false);
            btnShowPwd.setText("👁");
        }
    }

    /** 一键填入默认凭证。 */
    @FXML
    public void fillDefault() {
        accountField.setText(DEFAULT_ACCOUNT);
        passwordField.setText(DEFAULT_PASSWORD);
        tfPwdVisible.setText(DEFAULT_PASSWORD);
    }

    /** 重置输入框并聚焦账号。 */
    @FXML
    public void onReset() {
        accountField.clear();
        passwordField.clear();
        tfPwdVisible.clear();
        accountField.requestFocus();
    }

    @FXML
    public void onLogin() {
        String account = accountField.getText() == null ? "" : accountField.getText().trim();
        String password = passwordVisible ? tfPwdVisible.getText() : passwordField.getText();
        if (password == null) password = "";

        if (account.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "登录提示", "请输入账号和密码");
            return;
        }
        if (DEFAULT_ACCOUNT.equals(account) && DEFAULT_PASSWORD.equals(password)) {
            if (app != null) {
                AuthUser user = new AuthUser(account, "管理员", "超级管理员", true);
                // 稍等当前按钮事件处理完成后再切换场景，避免 FX 事件线程中重排
                Platform.runLater(() -> app.showMain(user));
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "登录失败", "账号或密码错误，请重新输入");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.initOwner(root == null || root.getScene() == null ? null : root.getScene().getWindow());
        alert.showAndWait();
    }
}
