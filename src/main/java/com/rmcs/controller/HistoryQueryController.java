package com.rmcs.controller;

import com.rmcs.model.InspectionRecord;
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

/** 履历查询模态：按判定/位置筛选检测记录，统计合格率并导出 CSV。 */
public class HistoryQueryController {

    @FXML private ComboBox<String> cbResult;
    @FXML private TextField tfKeyword;
    @FXML private TableView<InspectionRecord> table;
    @FXML private TableColumn<InspectionRecord, String> colTime;
    @FXML private TableColumn<InspectionRecord, String> colRow;
    @FXML private TableColumn<InspectionRecord, String> colPos;
    @FXML private TableColumn<InspectionRecord, String> colProduct;
    @FXML private TableColumn<InspectionRecord, String> colMeasured;
    @FXML private TableColumn<InspectionRecord, String> colStandard;
    @FXML private TableColumn<InspectionRecord, String> colResult;
    @FXML private Label lblTotal;
    @FXML private Label lblPass;
    @FXML private Label lblFail;
    @FXML private Label lblRate;

    private ObservableList<InspectionRecord> source = FXCollections.observableArrayList();
    private FilteredList<InspectionRecord> filtered;
    private Runnable onCleared;

    @FXML
    public void initialize() {
        cbResult.getItems().addAll("全部", "仅合格", "仅不合格");
        cbResult.getSelectionModel().selectFirst();

        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
        colRow.setCellValueFactory(d -> new SimpleStringProperty("第" + d.getValue().getRow() + "行"));
        colPos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPosition() + "区"));
        colProduct.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProductName()));
        colMeasured.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedMeasured()));
        colStandard.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedStandard()));
        colResult.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isResult() ? "合格 ✓" : "不合格 ✗"));
        colResult.setCellFactory(c -> new TextFieldTableCell<InspectionRecord, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.startsWith("合格")
                        ? "-fx-text-fill: #22c55e;"
                        : "-fx-text-fill: #ef4444;");
            }
        });

        filtered = new FilteredList<>(source, r -> true);
        table.setItems(filtered);
        table.setPlaceholder(new Label("暂无检测记录"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshStats();
    }

    /** 绑定主控制台的履历数据源（同一 ObservableList，自动跟随新增）。 */
    public void bind(ObservableList<InspectionRecord> records) {
        this.source = records == null ? FXCollections.observableArrayList() : records;
        filtered = new FilteredList<>(source, r -> true);
        table.setItems(filtered);
        source.addListener((javafx.collections.ListChangeListener<InspectionRecord>) c -> refreshStats());
        applyFilter();
    }

    public void setOnCleared(Runnable onCleared) { this.onCleared = onCleared; }

    @FXML
    public void applyFilter() {
        String mode = cbResult.getValue();
        String kw = tfKeyword.getText() == null ? "" : tfKeyword.getText().trim().toLowerCase();
        filtered.setPredicate(r -> {
            if (mode != null) {
                if ("仅合格".equals(mode) && !r.isResult()) return false;
                if ("仅不合格".equals(mode) && r.isResult()) return false;
            }
            if (!kw.isEmpty()) {
                String pos = r.getPosition().toLowerCase();
                if (!pos.contains(kw) && !String.valueOf(r.getRow()).contains(kw)) return false;
            }
            return true;
        });
        refreshStats();
    }

    @FXML
    public void resetFilter() {
        cbResult.getSelectionModel().selectFirst();
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
    public void exportCsv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("导出检测履历");
        fc.setInitialFileName("inspection_history_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV 文件", "*.csv"));
        File file = fc.showSaveDialog(stage());
        if (file == null) return;
        try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
            w.write('\uFEFF');
            w.println("时间,行,位置,产品名称,实测值,标准值,判定");
            for (InspectionRecord r : filtered) {
                w.printf("%s,%d,%s,%s,%s,%s,%s%n",
                        r.getFormattedTime(), r.getRow(), r.getPosition(), r.getProductName(),
                        r.getFormattedMeasured(), r.getFormattedStandard(),
                        r.isResult() ? "合格" : "不合格");
            }
            w.flush();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "导出失败: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void close() {
        Stage s = stage();
        if (s != null) s.close();
    }

    private void refreshStats() {
        int total = filtered == null ? 0 : filtered.size();
        long pass = filtered == null ? 0 : filtered.stream().filter(InspectionRecord::isResult).count();
        long fail = total - pass;
        double rate = total == 0 ? 0 : pass * 100.0 / total;
        lblTotal.setText("查询结果: " + total + " 条");
        lblPass.setText("合格 " + pass);
        lblFail.setText("不合格 " + fail);
        lblRate.setText(String.format("合格率 %.1f%%", rate));
    }

    private Stage stage() {
        if (table == null || table.getScene() == null) return null;
        return (Stage) table.getScene().getWindow();
    }
}
