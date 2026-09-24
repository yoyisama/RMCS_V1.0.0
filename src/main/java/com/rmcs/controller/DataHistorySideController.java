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
import java.util.ResourceBundle;

/**
 * 单侧数据履历（Y1 或 Y2）：
 * 7 列明细表（列号 | 行号 | 左/右 | 测量值 | 标准值 | 结果 | 时间）+ 本侧统计条。
 * 本侧只接收与自身 side 相同的记录。
 */
public class DataHistorySideController implements Initializable {

    private static final int MAX_RECORDS = 100;

    @FXML private TableView<InspectionRecord> table;
    @FXML private TableColumn<InspectionRecord, String> colCol, colRow, colLr, colProduct,
            colMeasured, colStandard, colResult, colTime;
    @FXML private Button btnOpenHistory;
    @FXML private Label lblTitle, lblTotal, lblPassCount, lblFailCount, lblRate;

    private final ObservableList<InspectionRecord> records = FXCollections.observableArrayList();
    private String side;
    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colCol.setCellValueFactory(d -> new SimpleStringProperty("第" + d.getValue().getCol() + "列"));
        colRow.setCellValueFactory(d -> new SimpleStringProperty(WorkPos.rowName(d.getValue().getRow())));
        colLr.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLr()));
        colProduct.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProductName()));
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

        table.setItems(records);
        table.setPlaceholder(new Label("暂无检测数据"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /** 设置本卡片所属侧别（"Y1" / "Y2"）。 */
    public void setSide(String side) {
        this.side = side;
        if (lblTitle != null) {
            lblTitle.setText("数据履历 · " + side + ("Y1".equals(side) ? "（左侧）" : "（右侧）"));
        }
    }

    /** 追加记录；非本侧记录直接忽略。 */
    public void add(InspectionRecord r) {
        if (r == null || side == null || !side.equals(r.getSide())) return;
        Platform.runLater(() -> {
            records.add(0, r);
            if (records.size() > MAX_RECORDS) records.remove(MAX_RECORDS, records.size());
            refreshStats();
        });
    }

    public void clearAll() {
        records.clear();
        refreshStats();
    }

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

    public ObservableList<InspectionRecord> getRecords() { return records; }

    public void setOnOpen(Runnable r) { this.onOpen = r; }

    @FXML
    public void openHistoryQuery() {
        if (onOpen != null) onOpen.run();
    }
}
