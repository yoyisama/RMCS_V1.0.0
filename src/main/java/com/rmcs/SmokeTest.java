package com.rmcs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
public class SmokeTest extends Application {
    @Override public void start(Stage stage) throws Exception {
        new FXMLLoader(getClass().getResource("/fxml/login.fxml")).load();
        new FXMLLoader(getClass().getResource("/fxml/main.fxml")).load();
        System.out.println("SMOKE_TEST_OK");
        Platform.exit();
    }
    public static void main(String[] args) { Application.launch(SmokeTest.class, args); }
}