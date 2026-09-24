package com.rmcs.controller;

import com.rmcs.model.InspectionRecord;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/** 数据履历面板：滚动展示最近 100 条 InspectionRecord。 */
public class DataHistoryController implements Initializable {

    @FXML private TableView<InspectionRecord> historyTable;
    @FXML private TableColumn<InspectionRecord, String> colRow, colPos, colMeasured, colStandard, colResult, colTime;
    @FXML private Button btnOpenHistory;
    @FXML private Label lblTotal, lblPassCount, lblFailCount, lblRate;
    @FXML private Label lblAutoBadge;

    private final ObservableList<InspectionRecord> records = FXCollections.observableArrayList();
    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colRow.setCellValueFactory(d -> new SimpleStringProperty("第" + d.getValue().getRow() + "行"));
        colPos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPosition() + "区"));
        colMeasured.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedMeasured()));
        colStandard.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedStandard()));
        colResult.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isResult() ? "OK" : "NG"));
        colResult.setCellFactory(c -> new javafx.scene.control.cell.TextFieldTableCell<InspectionRecord, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("OK".equals(item)
                        ? "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-background-color: #10b98122; -fx-background-radius: 8; -fx-alignment: center;"
                        : "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-color: #ef444422; -fx-background-radius: 8; -fx-alignment: center;");
            }
        });
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));

        historyTable.setItems(records);
        historyTable.setPlaceholder(new Label("暂无检测数据，等待首次测量"));
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void add(InspectionRecord r) {
        if (r == null) return;
        Platform.runLater(() -> {
            records.add(0, r);
            if (records.size() > 100) records.remove(100, records.size());
            refreshStats();
        });
    }

    public void clearAll() {
        records.clear();
        refreshStats();
    }

    /** 刷新底部统计条：总计 / 合格 / 超差 / 检出率。 */
    private void refreshStats() {
        if (lblTotal == null) return;
        int total = records.size();
        long pass = records.stream().filter(InspectionRecord::isResult).count();
        lblTotal.setText("总计: " + total);
        lblPassCount.setText("合格: " + pass);
        lblFailCount.setText("超差: " + (total - pass));
        lblRate.setText(total == 0 ? "良率: -" : String.format("良率: %.1f%%", pass * 100.0 / total));
        if (btnOpenHistory != null) {
            btnOpenHistory.setText("实时记录 (" + total + "条)");
        }
    }

    /** 由主控制台同步采集模式与间隔，刷新标题徽章（原型：自动获取 (1.4s) / 手动获取模式）。 */
    public void setAutoMode(boolean auto, long intervalMs) {
        if (lblAutoBadge == null) return;
        lblAutoBadge.setText(auto
                ? String.format("● 自动获取 (%.1fs)", intervalMs / 1000.0)
                : "● 手动获取模式");
        lblAutoBadge.getStyleClass().removeAll("badge-success", "badge-warning");
        lblAutoBadge.getStyleClass().add(auto ? "badge-success" : "badge-warning");
    }

    public ObservableList<InspectionRecord> getRecords() {
        return records;
    }

    public void setOnOpen(Runnable r) {
        this.onOpen = r;
    }

    @FXML
    public void openHistoryQuery() {
        if (onOpen != null) onOpen.run();
    }
}