package com.rmcs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

/**
 * 冒烟验证：不打开主窗口，逐个加载全部 FXML，
 * 用于确认 @FXML 字段注入与 onAction 方法绑定均正确（缺失会在加载阶段抛异常）。
 */
public class SmokeTest extends Application {

    private static final String[] FXMLS = {
            "/fxml/login.fxml",
            "/fxml/main.fxml",
            "/fxml/header.fxml",
            "/fxml/footer_bar.fxml",
            "/fxml/components/work_matrix.fxml",
            "/fxml/components/data_history.fxml",
            "/fxml/components/action_log.fxml",
            "/fxml/components/right_panel.fxml",
            "/fxml/modals/standard_config.fxml",
            "/fxml/modals/history_query.fxml",
            "/fxml/modals/log_query.fxml"
    };

    @Override
    public void start(Stage stage) {
        boolean ok = true;
        for (String fxml : FXMLS) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                loader.load();
                Object ctrl = loader.getController();
                System.out.println("OK   " + fxml + "  ->  "
                        + (ctrl == null ? "(无控制器)" : ctrl.getClass().getSimpleName()));
            } catch (Throwable t) {
                ok = false;
                System.out.println("FAIL " + fxml + "  ->  " + t);
            }
        }
        System.out.println(ok ? "SMOKE_TEST_OK" : "SMOKE_TEST_FAILED");
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(SmokeTest.class, args);
    }
}
