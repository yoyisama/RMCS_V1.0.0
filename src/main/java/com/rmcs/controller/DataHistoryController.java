package com.rmcs.controller;

import com.rmcs.model.InspectionRecord;
import com.rmcs.model.WorkPos;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 数据履历（方案①）：单一总标题 + 单一共享列头；Y1 | Y2 两张表并排，
 * Y2 表头隐藏以共用 Y1 表头，避免双表头重复。
 */
public class DataHistoryController implements Initializable {

    private static final int MAX_RECORDS = 100;

    @FXML private TableView<InspectionRecord> y1Table, y2Table;
    @FXML private TableColumn<InspectionRecord, String> y1ColCol, y1ColRow, y1ColLr,
            y1ColMeasured, y1ColStandard, y1ColResult, y1ColTime;
    @FXML private TableColumn<InspectionRecord, String> y2ColCol, y2ColRow, y2ColLr,
            y2ColMeasured, y2ColStandard, y2ColResult, y2ColTime;
    @FXML private Button btnOpenHistory;
    @FXML private Label lblAutoBadge, lblTotal, lblPassCount, lblFailCount, lblRate;

    private final ObservableList<InspectionRecord> y1Records = FXCollections.observableArrayList();
    private final ObservableList<InspectionRecord> y2Records = FXCollections.observableArrayList();
    private final Map<String, ObservableList<InspectionRecord>> bySide = new HashMap<>();
    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bySide.put("Y1", y1Records);
        bySide.put("Y2", y2Records);
        setupTable(y1Table, y1ColCol, y1ColRow, y1ColLr, y1ColMeasured, y1ColStandard, y1ColResult, y1ColTime);
        setupTable(y2Table, y2ColCol, y2ColRow, y2ColLr, y2ColMeasured, y2ColStandard, y2ColResult, y2ColTime);
        // Y2 表头隐藏，共用 Y1 表头（单一共享列头）
        y2Table.getStyleClass().add("no-header");
    }

    private void setupTable(TableView<InspectionRecord> table,
                            TableColumn<InspectionRecord, String> colCol, TableColumn<InspectionRecord, String> colRow,
                            TableColumn<InspectionRecord, String> colLr, TableColumn<InspectionRecord, String> colMeasured,
                            TableColumn<InspectionRecord, String> colStandard, TableColumn<InspectionRecord, String> colResult,
                            TableColumn<InspectionRecord, String> colTime) {
        colCol.setCellValueFactory(d -> new SimpleStringProperty("第" + d.getValue().getCol() + "列"));
        colRow.setCellValueFactory(d -> new SimpleStringProperty(WorkPos.rowName(d.getValue().getRow())));
        colLr.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLr()));
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
                        ? "-fx-text-fill:#10b981;-fx-font-weight:bold;-fx-background-color:#10b98122;-fx-background-radius:8;-fx-alignment:center;"
                        : "-fx-text-fill:#ef4444;-fx-font-weight:bold;-fx-background-color:#ef444422;-fx-background-radius:8;-fx-alignment:center;");
            }
        });
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
        table.setPlaceholder(new Label("暂无检测数据"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void add(InspectionRecord r) {
        if (r == null) return;
        ObservableList<InspectionRecord> list = bySide.get(r.getSide());
        if (list == null) return;
        Platform.runLater(() -> {
            list.add(0, r);
            if (list.size() > MAX_RECORDS) list.remove(MAX_RECORDS, list.size());
            refreshStats();
        });
    }

    public void clearAll() {
        y1Records.clear();
        y2Records.clear();
        refreshStats();
    }

    private void refreshStats() {
        if (lblTotal == null) return;
        int total = y1Records.size() + y2Records.size();
        long pass = y1Records.stream().filter(InspectionRecord::isResult).count()
                  + y2Records.stream().filter(InspectionRecord::isResult).count();
        lblTotal.setText("总计: " + total);
        lblPassCount.setText("合格: " + pass);
        lblFailCount.setText("超差: " + (total - pass));
        lblRate.setText(total == 0 ? "良率: -" : String.format("良率: %.1f%%", pass * 100.0 / total));
        if (btnOpenHistory != null) btnOpenHistory.setText("实时记录 (" + total + "条)");
    }

    public void setAutoMode(boolean auto, long intervalMs) {
        if (lblAutoBadge == null) return;
        lblAutoBadge.setText(auto
                ? String.format("● 自动获取 (%.1fs)", intervalMs / 1000.0)
                : "● 手动获取模式");
        lblAutoBadge.getStyleClass().removeAll("badge-success", "badge-warning");
        lblAutoBadge.getStyleClass().add(auto ? "badge-success" : "badge-warning");
    }

    public ObservableList<InspectionRecord> getRecords() {
        ObservableList<InspectionRecord> all = FXCollections.observableArrayList();
        all.addAll(y1Records);
        all.addAll(y2Records);
        return all;
    }

    public void setOnOpen(Runnable r) { this.onOpen = r; }

    @FXML
    public void openHistoryQuery() {
        if (onOpen != null) onOpen.run();
    }
}
