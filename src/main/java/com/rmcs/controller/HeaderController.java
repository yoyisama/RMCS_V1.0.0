package com.rmcs.controller;

import com.rmcs.App;
import com.rmcs.model.AuthUser;
import com.rmcs.model.LogEntry.LogType;
import com.rmcs.model.ProductionStatus.MachineStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/** 顶部条：品牌/用户/模式切换/主题/全屏/注销。 */
public class HeaderController implements Initializable {

    @FXML private Label lblUser;
    @FXML private Button btnAuto, btnTheme, btnFullScreen, btnLogout;

    private App app;
    private AuthUser user;
    private boolean autoMode = true;
    private OnLogoutListener onLogout;
    private OnModeChangeListener onModeChange;
    private Runnable onThemeChange;

    public void setContext(App app, AuthUser user) {
        this.app = app;
        this.user = user;
        if (lblUser != null) {
            lblUser.setText(user == null ? "未登录" : (user.getName() + " (" + user.getUsername() + ")"));
        }
    }

    public void setOnLogout(OnLogoutListener onLogout) {
        this.onLogout = onLogout;
    }

    @Override public void initialize(URL location, ResourceBundle resources) {
        applyMode();
    }

    /** 原型样式：单个模式按钮，点击在自动 / 手动之间切换。 */
    private void applyMode() {
        if (btnAuto == null) return;
        btnAuto.setText(autoMode ? "● 自动模式" : "● 手动模式");
        btnAuto.getStyleClass().removeAll("mode-auto", "mode-manual");
        btnAuto.getStyleClass().add(autoMode ? "mode-auto" : "mode-manual");
    }

    @FXML
    public void toggleAuto() { autoMode = !autoMode; applyMode(); fireModeChange(); }

    private void fireModeChange() {
        if (onModeChange != null) onModeChange.accept(autoMode);
    }

    public void setOnModeChange(OnModeChangeListener onModeChange) {
        this.onModeChange = onModeChange;
    }

    public void setOnThemeChange(Runnable onThemeChange) {
        this.onThemeChange = onThemeChange;
    }

    public boolean isAutoMode() { return autoMode; }

    @FXML
    public void toggleTheme() {
        boolean toLight = app != null && app.toggleTheme();
        btnTheme.setText(toLight ? "🌙" : "☀");
        if (onThemeChange != null) onThemeChange.run();
    }

    @FXML
    public void toggleFullScreen() {
        if (app != null && app.getStage() != null) {
            app.getStage().setFullScreen(!app.getStage().isFullScreen());
        }
    }

    @FXML
    public void logout() {
        if (!confirmLogout()) {
            return;
        }
        if (onLogout != null) {
            Platform.runLater(onLogout::run);
        } else if (app != null) {
            Platform.runLater(app::showLogin);
        }
    }

    /**
     * 项目风格退出确认弹窗（替代默认 Alert）：
     * 透明窗口 + 圆角卡片 + 蓝色标题栏，与「参数设定」等弹窗风格一致。
     *
     * @return true = 用户确认退出
     */
    private boolean confirmLogout() {
        javafx.stage.Stage owner = (app != null) ? app.getStage() : null;
        boolean dark = owner != null && owner.getScene() != null && owner.getScene().getRoot() != null
                && owner.getScene().getRoot().getStyleClass().contains("root-dark");

        javafx.stage.Stage dlg = new javafx.stage.Stage();
        dlg.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        if (owner != null) dlg.initOwner(owner);
        dlg.setResizable(false);

        javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane();
        overlay.setStyle("-fx-background-color: transparent;");

        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox();
        card.getStyleClass().add("modal-card");
        card.setMaxWidth(420);
        // 内联实色背景：该环境个别 CSS 变量（-bg-panel）在独立弹窗场景不解析，
        // 会呈现透明底（透过卡片能看到主窗口），此处按主题显式着色
        card.setStyle(dark
                ? "-fx-background-color:#181c24;-fx-border-color:#2c3342;-fx-border-radius:12;"
                : "-fx-background-color:#ffffff;-fx-border-color:#e2e8f0;-fx-border-radius:12;");

        // ── 蓝色标题栏 ──
        javafx.scene.layout.HBox titleBar = new javafx.scene.layout.HBox(8);
        titleBar.getStyleClass().add("dlg-titlebar");
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.control.Label icon = new javafx.scene.control.Label("⏻");
        icon.getStyleClass().add("dlg-title-icon");
        javafx.scene.control.Label title = new javafx.scene.control.Label("电源 / 注销");
        title.getStyleClass().add("dlg-title");
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.control.Button close = new javafx.scene.control.Button("✕");
        close.getStyleClass().add("dlg-close");
        close.setOnAction(e -> dlg.close());
        titleBar.getChildren().addAll(icon, title, spacer, close);

        // ── 内容区：图标 + 提示语 / 按钮行 ──
        javafx.scene.layout.VBox body = new javafx.scene.layout.VBox(16);
        body.setStyle("-fx-padding: 20 20 18 20;");

        javafx.scene.layout.HBox msgRow = new javafx.scene.layout.HBox(14);
        msgRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.control.Label warn = new javafx.scene.control.Label("⏻");
        warn.setStyle("-fx-text-fill:#ef4444;-fx-font-size:28;-fx-font-weight:bold;");
        javafx.scene.control.Label msg = new javafx.scene.control.Label("确认退出当前操作员会话，并返回登录页？");
        msg.setStyle(dark
                ? "-fx-text-fill:#e2e8f0;-fx-font-size:13;-fx-wrap-text:true;"
                : "-fx-text-fill:#1e293b;-fx-font-size:13;-fx-wrap-text:true;");
        msgRow.getChildren().addAll(warn, msg);

        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(10);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        javafx.scene.control.Button cancel = new javafx.scene.control.Button("取  消");
        cancel.getStyleClass().addAll("btn", "btn-ghost");
        cancel.setPrefSize(96, 34);
        javafx.scene.control.Button ok = new javafx.scene.control.Button("确认退出");
        ok.getStyleClass().addAll("btn", "btn-danger");
        ok.setPrefSize(110, 34);
        ok.setDefaultButton(true);
        cancel.setOnAction(e -> dlg.close());
        ok.setOnAction(e -> { close.setDisable(true); dlg.close(); confirmResult = true; });
        btnRow.getChildren().addAll(cancel, ok);

        body.getChildren().addAll(msgRow, btnRow);
        card.getChildren().addAll(titleBar, body);
        overlay.getChildren().add(card);

        javafx.scene.Scene scene = new javafx.scene.Scene(overlay, 430, 190);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        java.net.URL css = getClass().getResource("/css/main.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        // 标题栏拖拽移动
        final double[] drag = new double[2];
        titleBar.setOnMousePressed(e -> { drag[0] = e.getSceneX(); drag[1] = e.getSceneY(); });
        titleBar.setOnMouseDragged(e -> {
            dlg.setX(e.getScreenX() - drag[0]);
            dlg.setY(e.getScreenY() - drag[1]);
        });

        dlg.setScene(scene);
        // 注意：不能在 showAndWait() 之前调用 show()（会抛异常导致退出流程中断），
        // 改为在窗口显示后（setOnShown）执行内联着色
        dlg.setOnShown(e -> com.rmcs.util.ThemeStyler.apply(scene, dark));
        try {
            dlg.showAndWait();
        } catch (Exception ignored) {
            // 弹窗异常不影响后续登出流程
        }
        boolean result = confirmResult;
        confirmResult = false;
        return result;
    }

    /** 退出确认结果（showAndWait 期间由按钮回写）。 */
    private boolean confirmResult;

    public interface OnLogoutListener { void run(); }

    /** 自动 / 手动模式切换通知（true=自动）。 */
    public interface OnModeChangeListener { void accept(boolean auto); }

    /** 占位方法：登录后将状态推送给 Header（供后续扩展）。 */
    public void setRunningMode(MachineStatus status) {
        if (status == null) return;
        switch (status) {
            case RUNNING, STANDYBY, STOPPED, RESETTING -> applyMode();
        }
    }

    /** 触发一条日志（用于 Header 内的提示，便于调试）。 */
    public void echo(LogType type, String msg) {
        // 实际日志由 ActionLogController 写，这里保留接口方便外部调用
    }
}