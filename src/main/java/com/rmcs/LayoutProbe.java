package com.rmcs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/** 临时布局探针 v3：实测 AnchorPane 版首帧/二帧坐标（交付前删除）。 */
public class LayoutProbe extends Application {

    private static void dump(String name, Node n) {
        if (n == null) {
            System.out.println("[PROBE] " + name + " = null !");
            return;
        }
        Bounds bp = n.getBoundsInParent();
        System.out.println("[PROBE] " + name + " parentMinX=" + Math.round(bp.getMinX())
                + " w=" + Math.round(bp.getWidth())
                + " computedMinW=" + Math.round(n.minWidth(-1))
                + " computedPrefW=" + Math.round(n.prefWidth(-1)));
    }

    private void snapshot(String tag, Stage stage, Parent root) {
        Node right = root.lookup("#rightScroll");
        System.out.println("[PROBE] === " + tag + " ===");
        System.out.println("[PROBE] stageW=" + Math.round(stage.getWidth())
                + " sceneW=" + Math.round(stage.getScene().getWidth())
                + " rootW=" + Math.round(root.getLayoutBounds().getWidth()));
        dump("rightScroll", right);
        dump("anchorPane", right == null ? null : right.getParent());
        dump("workspaceVBox", root.lookup(".bg-app"));
        dump("workMatrix", root.lookup("#workMatrix"));
        dump("dataHistory", root.lookup("#dataHistory"));
    }

    @Override
    public void start(Stage stage) {
        try {
            double w = Math.min(1280, Screen.getPrimary().getVisualBounds().getWidth() * 0.92);
            double h = Math.min(800, Screen.getPrimary().getVisualBounds().getHeight() * 0.92);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, w, h);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(w);
            stage.setHeight(h);
            stage.show();

            snapshot("first-frame", stage, root);
            Platform.runLater(() -> {
                root.applyCss();
                root.layout();
                snapshot("second-frame", stage, root);
                Platform.exit();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        Application.launch(LayoutProbe.class, args);
    }
}
