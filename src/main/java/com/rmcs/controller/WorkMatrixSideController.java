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
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 单侧作业矩阵（Y1 或 Y2）：2 列左右分布、共 15 行。
 * 行高全部按百分比划分（fitToHeight），15 行无滚动完整显示。
 */
public class WorkMatrixSideController implements Initializable {

    @FXML private GridPane grid;
    @FXML private Label lblTitle, lblLiveStatus, lblProduct;

    private final Map<String, Label> cells = new HashMap<>();
    private String side;
    private boolean built;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 网格在 setSide 后构建（需要知道侧别）
    }

    /** 设置本卡片所属侧别（"Y1" / "Y2"）并构建 15 行网格。 */
    public void setSide(String side) {
        if (side == null || side.equals(this.side) && built) return;
        this.side = side;
        if (lblTitle != null) {
            lblTitle.setText("执行作业 · " + side);
        }
        build();
    }

    private void build() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        cells.clear();

        ColumnConstraints c0 = new ColumnConstraints(); c0.setPercentWidth(18);
        grid.getColumnConstraints().add(c0);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(20.5);
            grid.getColumnConstraints().add(c);
        }

        // 行高全部按百分比划分（fitToHeight 下网格高度恒等于视口），17 行无滚动完整显示
        RowConstraints h1 = new RowConstraints(); h1.setPercentHeight(100.0 / 17 * 1.2);
        RowConstraints h2 = new RowConstraints(); h2.setPercentHeight(100.0 / 17 * 0.9);
        grid.getRowConstraints().addAll(h1, h2);
        for (int i = 0; i < WorkPos.ROWS; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 17);
            grid.getRowConstraints().add(rc);
        }

        // 表头左上角：Y1 → 左列，Y2 → 右列
        addHead("Y1".equals(side) ? "左列" : "右列", 0, 0, 1);
        addHead("第一列", 1, 0, 2);
        addHead("第二列", 3, 0, 2);
        addHead("行", 0, 1, 1);
        addHead("左", 1, 1, 1);
        addHead("右", 2, 1, 1);
        addHead("左", 3, 1, 1);
        addHead("右", 4, 1, 1);

        for (int row = 1; row <= WorkPos.ROWS; row++) {
            Label rowLbl = new Label(WorkPos.rowName(row));
            rowLbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            rowLbl.getStyleClass().add("matrix-head");
            grid.add(rowLbl, 0, row + 1);

            for (int col = 1; col <= 2; col++) {
                for (String lr : new String[]{"左", "右"}) {
                    WorkPos pos = new WorkPos(side, col, lr, row);
                    Label cell = new Label("---");
                    cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    cell.getStyleClass().addAll("matrix-cell", "matrix-cell-dash");
                    grid.add(cell, (col - 1) * 2 + ("左".equals(lr) ? 1 : 2), row + 1);
                    cells.put(pos.key(), cell);
                }
            }
        }
        built = true;
    }

    private void addHead(String text, int col, int row, int span) {
        Label l = new Label(text);
        l.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        l.getStyleClass().add("matrix-col-head");
        GridPane.setColumnSpan(l, span);
        grid.add(l, col, row);
    }

    /** 更新本侧指定位置的单元格（非本侧的位置直接忽略）。 */
    public void setCell(WorkPos pos, MatrixCell cell) {
        if (pos == null || cell == null || side == null || !built) return;
        if (!side.equals(pos.getSide())) return;
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
        if (side == null) return;
        for (int col = 1; col <= 2; col++) {
            for (String lr : new String[]{"左", "右"}) {
                for (int row = 1; row <= WorkPos.ROWS; row++) {
                    setCell(new WorkPos(side, col, lr, row), new MatrixCell(null, null, false));
                }
            }
        }
    }

    /** 更新当前产品名称（来自 PLC DB36 产品名）。 */
    public void setProductName(String name) {
        if (lblProduct == null) return;
        lblProduct.setText(name == null || name.isBlank() ? "" : ("产品: " + name));
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
