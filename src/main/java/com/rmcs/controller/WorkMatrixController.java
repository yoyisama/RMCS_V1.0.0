package com.rmcs.controller;

import com.rmcs.model.MatrixCell;
import com.rmcs.model.WorkPos;
import com.rmcs.model.ProductionStatus.MachineStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 执行作业（方案①）：单一总标题 + 单一共享列头；Y1 | Y2 并排对照，竖线分隔。
 * 列结构（每侧）：行 | 第一列·左 | 第一列·右 | 第二列·左 | 第二列·右。
 * 行高按百分比划分（fitToHeight），15 行无滚动完整显示。
 */
public class WorkMatrixController implements Initializable {

    @FXML private GridPane grid;
    @FXML private Label lblLiveStatus;

    private final Map<String, Label> cells = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        build();
    }

    private void build() {
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        grid.getChildren().clear();
        cells.clear();

        // 列：Y1行标签(8%) | Y1 4数据(10.3%×4) | 分隔(1.6%) | Y2行标签(8%) | Y2 4数据(10.3%×4)
        double[] cw = {8, 10.3, 10.3, 10.3, 10.3, 1.6, 8, 10.3, 10.3, 10.3, 10.3};
        for (double w : cw) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(w);
            grid.getColumnConstraints().add(c);
        }

        // 行：表头2行固定 + 15数据行固定高度，超出后可滚动
        grid.getRowConstraints().addAll(new RowConstraints(28, 28, 28),
                                        new RowConstraints(28, 28, 28));
        for (int i = 0; i < WorkPos.getRows(); i++) {
            grid.getRowConstraints().add(new RowConstraints(34, 34, 34));
        }

        // ---- 单一共享列头（只画一次，横跨 Y1/Y2 两块） ----
        addHead("Y1", 0, 0, 1);              // 左侧块标识
        addHead("第一列", 1, 0, 2);
        addHead("第二列", 3, 0, 2);
        addHead("Y2", 6, 0, 1);              // 右侧块标识
        addHead("第一列", 7, 0, 2);
        addHead("第二列", 9, 0, 2);
        addHead("行", 0, 1, 1);
        addHead("L", 1, 1, 1); addHead("R", 2, 1, 1);
        addHead("L", 3, 1, 1); addHead("R", 4, 1, 1);
        addHead("行", 6, 1, 1);
        addHead("L", 7, 1, 1); addHead("R", 8, 1, 1);
        addHead("L", 9, 1, 1); addHead("R", 10, 1, 1);

        // 竖线分隔（跨所有行）
        Region divider = new Region();
        divider.getStyleClass().add("band-divider");
        grid.add(divider, 5, 0, 1, WorkPos.getRows() + 2);

        // ---- 数据：Y1 块(col0-4) / Y2 块(col6-10) ----
        for (int row = 1; row <= WorkPos.getRows(); row++) {
            int gRow = row + 1;
            Label y1Lbl = new Label(WorkPos.rowName(row));
            y1Lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            y1Lbl.getStyleClass().add("matrix-head");
            grid.add(y1Lbl, 0, gRow);

            Label y2Lbl = new Label(WorkPos.rowName(row));
            y2Lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            y2Lbl.getStyleClass().add("matrix-head");
            grid.add(y2Lbl, 6, gRow);

            for (String side : new String[]{"Y1", "Y2"}) {
                int base = "Y1".equals(side) ? 0 : 6;
                for (int col = 1; col <= WorkPos.getCols(); col++) {
                    for (String lr : new String[]{"左", "右"}) {
                        WorkPos pos = new WorkPos(side, col, lr, row);
                        Label cell = new Label("---");
                        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        cell.getStyleClass().addAll("matrix-cell", "matrix-cell-dash");
                        int colIdx = base + (col - 1) * 2 + ("左".equals(lr) ? 1 : 2);
                        grid.add(cell, colIdx, gRow);
                        cells.put(pos.key(), cell);
                    }
                }
            }
        }
    }

    private void addHead(String text, int col, int row, int span) {
        Label l = new Label(text);
        l.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        l.getStyleClass().add("matrix-col-head");
        GridPane.setColumnSpan(l, span);
        grid.add(l, col, row);
    }

    public void setCell(WorkPos pos, MatrixCell cell) {
        if (pos == null || cell == null) return;
        Label lbl = cells.get(pos.key());
        if (lbl == null) return;
        Platform.runLater(() -> {
            lbl.getStyleClass().removeAll("matrix-cell-pass", "matrix-cell-fail", "matrix-cell-active", "matrix-cell-dash");
            if (cell.isTesting()) {
                lbl.getStyleClass().add("matrix-cell-active");
                lbl.setText("· · ·");
            } else if (cell.getValue() == null) {
                lbl.getStyleClass().add("matrix-cell-dash");
                lbl.setText("---");
            } else {
                boolean pass = Boolean.TRUE.equals(cell.getResult());
                lbl.getStyleClass().add(pass ? "matrix-cell-pass" : "matrix-cell-fail");
                lbl.setText(String.format("%.3f", cell.getValue()));
            }
        });
    }

    public void clearAll() {
        for (String side : new String[]{"Y1", "Y2"}) {
            for (int col = 1; col <= WorkPos.getCols(); col++) {
                for (String lr : new String[]{"左", "右"}) {
                    for (int row = 1; row <= WorkPos.getRows(); row++) {
                        setCell(new WorkPos(side, col, lr, row), new MatrixCell(null, null, false));
                    }
                }
            }
        }
    }

    public void setLiveStatus(MachineStatus status) {
        if (lblLiveStatus == null || status == null) return;
        String text; String cls;
        switch (status) {
            case RUNNING:    text = "● 测量中"; cls = "badge-running"; break;
            case STANDYBY:   text = "● 待命中"; cls = "badge-warning"; break;
            case STOPPED:    text = "● 已停止"; cls = "badge-danger";  break;
            case RESETTING:  text = "● 复位中"; cls = "badge-info";    break;
            default:         text = "—";         cls = "badge-info";
        }
        lblLiveStatus.setText(text);
        lblLiveStatus.getStyleClass().removeAll("badge-running", "badge-warning", "badge-danger", "badge-info");
        lblLiveStatus.getStyleClass().add(cls);
    }
}
