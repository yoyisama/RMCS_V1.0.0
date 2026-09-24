package com.rmcs.controller;

import com.rmcs.model.MatrixCell;
import com.rmcs.model.MatrixCell.Position;
import com.rmcs.model.ProductionStatus.MachineStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.ResourceBundle;

/** 执行作业 7 行 × 3 区矩阵。 */
public class WorkMatrixController implements Initializable {

    @FXML private GridPane matrixGrid;
    @FXML private Label lblLiveStatus;

    private final Label[][] cells = new Label[7][3];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 4 列：行号 + L/M/R；8 行：表头 + 7 数据
        ColumnConstraints c0 = new ColumnConstraints(); c0.setPercentWidth(13);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(29);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(29);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(29);
        matrixGrid.getColumnConstraints().addAll(c0, c1, c2, c3);
        for (int i = 0; i < 8; i++) {
            RowConstraints rc = new RowConstraints(); rc.setPercentHeight(100.0 / 8);
            matrixGrid.getRowConstraints().add(rc);
        }

        addHeader(0, 0, "行");
        addHeader(1, 0, "左区 (L)");
        addHeader(2, 0, "中区 (M)");
        addHeader(3, 0, "右区 (R)");

        String[] rowNames = {"第一行", "第二行", "第三行", "第四行", "第五行", "第六行", "第七行"};
        for (int row = 0; row < 7; row++) {
            Label rowLbl = new Label(rowNames[row]);
            rowLbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            rowLbl.getStyleClass().add("matrix-head");
            matrixGrid.add(rowLbl, 0, row + 1);

            for (int col = 0; col < 3; col++) {
                Label cell = new Label("—");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cell.getStyleClass().addAll("matrix-cell", "matrix-cell-dash");
                matrixGrid.add(cell, col + 1, row + 1);
                cells[row][col] = cell;
            }
        }
    }

    private void addHeader(int col, int row, String text) {
        Label l = new Label(text);
        l.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        l.getStyleClass().add("matrix-col-head");
        matrixGrid.add(l, col, row);
    }

    public void setCell(int row, Position p, MatrixCell cell) {
        if (row < 0 || row >= 7 || p == null || cell == null) return;
        int col = p.ordinal();
        Label lbl = cells[row][col];
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
                lbl.setText(String.format("%.3f Ω", cell.getValue()));
            }
        });
    }

    public void clearAll() {
        for (int r = 0; r < 7; r++) {
            for (Position p : Position.values()) {
            setCell(r, p, new MatrixCell(null, null, false));
            }
        }
    }

    public void setLiveStatus(MachineStatus status) {
        if (lblLiveStatus == null || status == null) return;
        String text;
        String cls;
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