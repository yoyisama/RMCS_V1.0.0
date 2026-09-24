package com.rmcs.controller;

import com.rmcs.model.LogEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** 日志查询模态：按类型/关键字检索动作履历，统计告警数量并导出 TXT。 */
public class LogQueryController {

    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfKeyword;
    @FXML private TableView<LogEntry> table;
    @FXML private TableColumn<LogEntry, String> colTime;
    @FXML private TableColumn<LogEntry, String> colType;
    @FXML private TableColumn<LogEntry, String> colMessage;
    @FXML private Label lblTotal;
    @FXML private Label lblInfo;
    @FXML private Label lblWarn;
    @FXML private Label lblErrorCount;

    private ObservableList<LogEntry> source = FXCollections.observableArrayList();
    private FilteredList<LogEntry> filtered;
    private Runnable onCleared;

    @FXML
    public void initialize() {
        cbType.getItems().addAll("全部", "信息 INFO", "PLC", "串口 COM", "成功 SUCCESS", "警告 WARNING", "错误 ERROR");
        cbType.getSelectionModel().selectFirst();

        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().name()));
        colMessage.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMessage()));
        colType.setCellFactory(c -> new TextFieldTableCell<LogEntry, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(colorOf(item));
            }
        });

        filtered = new FilteredList<>(source, e -> true);
        table.setItems(filtered);
        table.setPlaceholder(new Label("暂无日志记录"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshStats();
    }

    public void bind(ObservableList<LogEntry> logs) {
        this.source = logs == null ? FXCollections.observableArrayList() : logs;
        filtered = new FilteredList<>(source, e -> true);
        table.setItems(filtered);
        source.addListener((javafx.collections.ListChangeListener<LogEntry>) c -> refreshStats());
        applyFilter();
    }

    public void setOnCleared(Runnable onCleared) { this.onCleared = onCleared; }

    @FXML
    public void applyFilter() {
        String mode = cbType.getValue() == null ? "全部" : cbType.getValue();
        String kw = tfKeyword.getText() == null ? "" : tfKeyword.getText().trim().toLowerCase();
        filtered.setPredicate(e -> {
            if (!"全部".equals(mode)) {
                String name = mode.substring(mode.indexOf(' ') + 1);
                if (!name.equals(e.getType().name())) return false;
            }
            if (!kw.isEmpty() && !e.getMessage().toLowerCase().contains(kw)) return false;
            return true;
        });
        refreshStats();
    }

    @FXML
    public void resetFilter() {
        cbType.getSelectionModel().selectFirst();
        tfKeyword.clear();
        applyFilter();
    }

    @FXML
    public void clearAll() {
        source.clear();
        if (onCleared != null) onCleared.run();
        refreshStats();
    }

    @FXML
    public void exportTxt() {
        FileChooser fc = new FileChooser();
        fc.setTitle("导出动作日志");
        fc.setInitialFileName("action_log.txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("文本文件", "*.txt"));
        File file = fc.showSaveDialog(stage());
        if (file == null) return;
        try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
            for (LogEntry e : filtered) {
                w.printf("[%s][%s] %s%n", e.getFormattedTime(), e.getType().name(), e.getMessage());
            }
            w.flush();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "导出失败: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void close() {
        Stage s = stage();
        if (s != null) s.close();
    }

    private void refreshStats() {
        if (filtered == null) return;
        lblTotal.setText("查询结果: " + filtered.size() + " 条");
        lblInfo.setText("信息 " + count("INFO") + " · 成功 " + count("SUCCESS"));
        lblWarn.setText("警告 " + count("WARNING"));
        lblErrorCount.setText("错误 " + count("ERROR"));
    }

    private long count(String type) {
        return filtered.stream().filter(e -> e.getType().name().equals(type)).count();
    }

    private static String colorOf(String type) {
        switch (type) {
            case "INFO":    return "-fx-text-fill: #38bdf8;";
            case "PLC":     return "-fx-text-fill: #a855f7;";
            case "COM":     return "-fx-text-fill: #22d3ee;";
            case "SUCCESS": return "-fx-text-fill: #22c55e;";
            case "WARNING": return "-fx-text-fill: #fbbf24;";
            case "ERROR":   return "-fx-text-fill: #ef4444;";
            default:        return "";
        }
    }

    private Stage stage() {
        if (table == null || table.getScene() == null) return null;
        return (Stage) table.getScene().getWindow();
    }
}
