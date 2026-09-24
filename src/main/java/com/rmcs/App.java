package com.rmcs;

import com.rmcs.controller.LoginController;
import com.rmcs.controller.MainController;
import com.rmcs.model.AuthUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    /** 设计基准尺寸：屏幕足够大时按此尺寸呈现 */
    private static final double BASE_WIDTH = 1280;
    private static final double BASE_HEIGHT = 800;

    /** 构建标记（区分新旧实例窗口） */
    private static final String BUILD_TAG = "20260922-1364";

    private Stage stage;
    private Runnable cleanup;
    /** 主题状态：true=黑夜 root-dark，false=白天 root-light（用于登录后恢复） */
    private boolean darkTheme = false;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        // 标题带构建标记：用于区分新旧实例（确认测试的是最新构建）
        stage.setTitle("RMCS 测阻机控制系统 · build " + BUILD_TAG);
        // 关闭窗口时释放串口与后台线程
        stage.setOnCloseRequest(e -> runCleanup());
        showLogin();
        stage.show();
    }

    public void showLogin() {
        runCleanup();
        try {
            FXMLLoader loader = new FXMLLoader(resolve("/fxml/login.fxml"));
            Parent root = loader.load();
            LoginController ctrl = loader.getController();
            if (ctrl != null) ctrl.setApp(this);
            Rectangle2D b = Screen.getPrimary().getVisualBounds();
            switchScene(root, "/css/login.css",
                    Math.min(820, b.getWidth() - 8),
                    Math.min(600, b.getHeight() - 8));
        } catch (Exception e) {
            throw new RuntimeException("无法加载登录页", e);
        }
    }

    public void showMain(AuthUser user) {
        runCleanup();
        try {
            FXMLLoader loader = new FXMLLoader(resolve("/fxml/main.fxml"));
            Parent root = loader.load();
            MainController ctrl = loader.getController();
            if (ctrl != null) {
                ctrl.initContext(this, user);
                cleanup = ctrl::releaseResources;
            }
            // 恢复当前主题，避免退出登录后重置为默认白天模式
            applyTheme(root);
            // 主控台最小尺寸：舒适值 1200×680，但绝不超过屏幕可视区（否则窗口被最小值撑出屏幕）
            Rectangle2D b = Screen.getPrimary().getVisualBounds();
            switchScene(root, "/css/main.css",
                    Math.min(1200, b.getWidth() - 8),
                    Math.min(680, b.getHeight() - 8));
            // 内联主题着色：等场景挂到窗口后跨两帧执行，确保所有控件就位；
            // 再延迟 600ms 补一轮，捕获异步加载/动态创建的节点
            Platform.runLater(() -> Platform.runLater(() ->
                    com.rmcs.util.ThemeStyler.apply(stage.getScene(), darkTheme)));
            javafx.animation.Timeline lateApply = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), e ->
                            com.rmcs.util.ThemeStyler.apply(stage.getScene(), darkTheme)));
            lateApply.play();
        } catch (Exception e) {
            throw new RuntimeException("无法加载主面板", e);
        }
    }

    /** 当前是否黑夜模式（供弹窗等场景同步内联主题）。 */
    public boolean isDarkTheme() {
        return darkTheme;
    }

    /** 将主题样式类设置到指定根节点，保证只存在 root-dark/root-light 之一。 */
    private void applyTheme(Parent root) {
        if (root == null) return;
        root.getStyleClass().removeAll("root-dark", "root-light");
        root.getStyleClass().add(darkTheme ? "root-dark" : "root-light");
        refreshCss();
        dumpTheme("applyTheme");
    }

    /**
     * 强制重新解析样式表：个别环境下仅翻转 root-dark/root-light 样式类不会刷新
     * 子控件的最终样式（表现为切换到黑夜模式后按钮/输入框仍为浅色底）。
     */
    private void refreshCss() {
        if (stage == null || stage.getScene() == null) return;
        try {
            java.util.List<String> sheets = new java.util.ArrayList<>(stage.getScene().getStylesheets());
            stage.getScene().getStylesheets().clear();
            stage.getScene().getStylesheets().addAll(sheets);
        } catch (Exception ignored) {
            // 刷新失败不影响正常使用
        }
    }

    /** 主题诊断：写入 target/theme_debug.txt，便于定位样式类是否真正切换。 */
    private void dumpTheme(String where) {
        try {
            Parent root = (stage != null && stage.getScene() != null) ? stage.getScene().getRoot() : null;
            String line = where + " darkTheme=" + darkTheme
                    + " root=" + (root == null ? "null" : root.getClass().getSimpleName())
                    + " classes=" + (root == null ? "-" : root.getStyleClass()) + "\n";
            java.nio.file.Files.write(java.nio.file.Paths.get("d:/work/yoyisama/RMCS_V1.0.0/target/theme_debug.txt"),
                    line.getBytes("UTF-8"),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
            // 诊断写文件失败不影响使用
        }
    }

    /**
     * 统一切换场景（确定性尺寸，杜绝「Scene 与窗口不等」导致的裁剪）：
     * 1. 先设最小尺寸、再取消最大化——最大化状态下 setWidth/Height 会被忽略，
     *    这正是「退出登录后显示不全」的直接原因；
     * 2. 窗口尺寸固定为「屏幕可视区 × 92% 与基准取小」，再夹紧到 [最小尺寸, 屏幕] 内；
     *    Scene 使用完全相同的尺寸，窗口与内容严格一致；
     * 3. 显示前强制 applyCss + layout 预布局，首帧渲染脉冲后再校正一次。
     */
    /** 窗口当前所在屏幕的可视区（多显示器环境按窗口中心定位所在屏，避免跨屏尺寸错配）。 */
    private Rectangle2D currentScreen() {
        if (stage != null && stage.getScene() != null
                && !Double.isNaN(stage.getX()) && stage.getWidth() > 0) {
            double cx = stage.getX() + stage.getWidth() / 2;
            double cy = stage.getY() + stage.getHeight() / 2;
            for (Screen s : Screen.getScreens()) {
                Rectangle2D b = s.getVisualBounds();
                if (cx >= b.getMinX() && cx <= b.getMaxX() && cy >= b.getMinY() && cy <= b.getMaxY()) {
                    return b;
                }
            }
        }
        return Screen.getPrimary().getVisualBounds();
    }

    private void switchScene(Parent root, String cssPath, double minW, double minH) {
        // 尺寸基准 = 窗口当前所在屏幕（多显示器下 getPrimary() 可能是另一块更大的屏，
        // 按 primary 计算会导致窗口比所在屏幕宽，右侧被屏幕边缘截断）
        Rectangle2D bounds = currentScreen();
        // 最小尺寸同样不得超过屏幕可视区，防止窗口被 min 撑出屏幕
        final double finalMinW = Math.min(minW, Math.max(320, bounds.getWidth() - 8));
        final double finalMinH = Math.min(minH, Math.max(240, bounds.getHeight() - 8));
        stage.setMinWidth(finalMinW);
        stage.setMinHeight(finalMinH);

        // 窗口目标尺寸由屏幕可视区确定性推导（与窗口历史状态无关），
        // 再夹紧到 [最小尺寸, 屏幕可视区 - 8]
        double w = Math.max(finalMinW, Math.min(BASE_WIDTH, bounds.getWidth() * 0.92));
        double h = Math.max(finalMinH, Math.min(BASE_HEIGHT, bounds.getHeight() * 0.92));
        w = Math.min(w, Math.max(finalMinW, bounds.getWidth() - 8));
        h = Math.min(h, Math.max(finalMinH, bounds.getHeight() - 8));

        // 先切内容（Scene 携带确定性尺寸）
        Scene scene = new Scene(root, w, h);
        URL css = resolve(cssPath);
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        bindFullScreen(scene);
        stage.setScene(scene);

        // 关键：窗口尺寸设置延迟到下一帧——全屏/最大化退出存在「还原到旧尺寸」的
        // 内部流程，同步调用 setWidth/Height 会被其覆盖（正是首帧错、拖动后对的成因）
        final double fw = w;
        final double fh = h;
        Platform.runLater(() -> {
            stage.setMaximized(false);
            stage.setFullScreen(false);
            stage.setWidth(fw);
            stage.setHeight(fh);
            stage.centerOnScreen();
            // 窗口尺寸落定后强制完整布局 + 首帧脉冲二次校正
            root.applyCss();
            root.layout();
            // 真实触发一次整树重排（与手工「拖动窗口」等价）：必须跨脉冲改变窗口宽度，
            // 让 Stage 产生真实的 resize 事件并重排。若在同一脉冲内先 set(fw-1) 再 set(fw)，
            // JavaFX 会合并为一次赋值而不触发 resize，这正是首帧错位、拖动后才正常的根因。
            Platform.runLater(() -> {
                stage.setWidth(fw - 1);
                Platform.runLater(() -> {
                    stage.setWidth(fw);
                    stage.centerOnScreen();
                    root.applyCss();
                    root.layout();
                    // 兜底：若窗口实际显示区域撑出屏幕（多显示器/DPI 差异），再下一帧缩回并居中
                    Platform.runLater(() -> fitToCurrentScreen(finalMinW, finalMinH));
                });
            });
        });
    }

    private void fitToCurrentScreen(double minW, double minH) {
        if (stage == null || stage.getScene() == null) return;
        Rectangle2D sb = currentScreen();
        double overflowX = (stage.getX() + stage.getWidth()) - sb.getMaxX();
        double overflowY = (stage.getY() + stage.getHeight()) - sb.getMaxY();
        if (overflowX > 0) {
            double nw = Math.max(minW, stage.getWidth() - overflowX - 4);
            stage.setWidth(nw);
            stage.setX(sb.getMaxX() - nw - 4);
        }
        if (overflowY > 0) {
            double nh = Math.max(minH, stage.getHeight() - overflowY - 4);
            stage.setHeight(nh);
            stage.setY(sb.getMaxY() - nh - 4);
        }
    }

    private double sceneWidth() {
        return Math.min(BASE_WIDTH, currentScreen().getWidth() * 0.92);
    }

    private double sceneHeight() {
        return Math.min(BASE_HEIGHT, currentScreen().getHeight() * 0.92);
    }

    /** F11 切换全屏（与顶部全屏按钮等价）。 */
    private void bindFullScreen(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                e.consume();
            }
        });
    }

    private void runCleanup() {
        if (cleanup != null) {
            try {
                cleanup.run();
            } catch (Exception ignored) {
                // 清理失败不应阻断页面切换
            }
            cleanup = null;
        }
    }

    /** 白天 / 黑夜模式切换：翻转场景根节点的 root-dark / root-light 样式类。 */
    public boolean toggleTheme() {
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) return false;
        Parent root = stage.getScene().getRoot();
        boolean toLight = root.getStyleClass().contains("root-dark");
        root.getStyleClass().removeAll("root-dark", "root-light");
        // 二次 remove 确保 FXML 初始类不会残留
        root.getStyleClass().removeAll("root-dark", "root-light");
        String target = toLight ? "root-light" : "root-dark";
        root.getStyleClass().add(target);
        darkTheme = !toLight;
        refreshCss();
        // 内联样式兜底：强制每个主题相关控件按当前主题着色（规避级联失效）
        com.rmcs.util.ThemeStyler.apply(stage.getScene(), darkTheme);
        System.out.println("[RMCS-THEME] toggle to " + target + " classes=" + root.getStyleClass() + " darkTheme=" + darkTheme);
        dumpTheme("toggleTheme->" + target);
        return toLight;
    }

    public Stage getStage() {
        return stage;
    }

    private static URL resolve(String path) {
        return App.class.getResource(path);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
