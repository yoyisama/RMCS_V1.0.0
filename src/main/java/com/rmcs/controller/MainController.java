package com.rmcs.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss");

    @FXML private BorderPane appHeader;
    @FXML private Button btnFullScreen;
    @FXML private GridPane jobGrid;
    @FXML private TableView<MeasureRecord> dataTable;
    @FXML private TableColumn<MeasureRecord, String> colRow;
    @FXML private TableColumn<MeasureRecord, String> colPos;
    @FXML private TableColumn<MeasureRecord, String> colMeasured;
    @FXML private TableColumn<MeasureRecord, String> colStandard;
    @FXML private TableColumn<MeasureRecord, String> colResult;
    @FXML private TableColumn<MeasureRecord, String> colTime;
    @FXML private TextArea actionLog;
    @FXML private Label lblDateTime;
    @FXML private Label lblCount;
    @FXML private Label lblRunState;
    @FXML private Label lblDuration;

    private final ObservableList<MeasureRecord> records = FXCollections.observableArrayList();
    private long completedCount = 8921;
    private Timeline clock;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildJobGrid();
        setupTable();
        initSampleData();

        appendLog("电阻计通讯串口打开成功!");
        appendLog("PLC通讯串口打开成功!");
        appendLog("系统初始化完成，等待启动指令");

        setupDateTime();
        setupFullScreen();
        setupWindowDrag();
    }

    /* ================= 执行作业 7 行 x 3 区 ================= */
    private void buildJobGrid() {
        // 列宽按比例分配，保证 4 列填满面板
        var c0 = new javafx.scene.layout.ColumnConstraints();
        c0.setPercentWidth(22);
        var c1 = new javafx.scene.layout.ColumnConstraints();
        c1.setPercentWidth(26);
        var c2 = new javafx.scene.layout.ColumnConstraints();
        c2.setPercentWidth(26);
        var c3 = new javafx.scene.layout.ColumnConstraints();
        c3.setPercentWidth(26);
        jobGrid.getColumnConstraints().addAll(c0, c1, c2, c3);
        // 8 行（表头 + 7 数据行）均分面板高度，让格子填满
        for (int i = 0; i < 8; i++) {
            var rc = new javafx.scene.layout.RowConstraints();
            rc.setPercentHeight(100.0 / 8);
            jobGrid.getRowConstraints().add(rc);
        }

        String[] heads = {"", "左区（L）", "中区（M）", "右区（R）"};
        for (int c = 0; c < heads.length; c++) {
            Label head = new Label(heads[c]);
            head.getStyleClass().add(c == 0 ? "cell-blank" : "cell-head");
            head.setMinSize(0, 32);
            head.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            jobGrid.add(head, c, 0);
        }

        String[] rowNames = {"第一行", "第二行", "第三行", "第四行", "第五行", "第六行", "第七行"};
        for (int r = 0; r < rowNames.length; r++) {
            Label rowHead = new Label(rowNames[r]);
            rowHead.getStyleClass().add("row-head");
            rowHead.setMinSize(0, 30);
            rowHead.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            jobGrid.add(rowHead, 0, r + 1);

            for (int c = 0; c < 3; c++) {
                boolean hasValue = (r == 1 && c == 0);  // 示例：第二行/左区
                Label cell = new Label(hasValue ? "3,412" : "---");
                cell.getStyleClass().add("cell");
                if (!hasValue) {
                    cell.getStyleClass().add("cell-empty");
                }
                cell.setMinSize(0, 30);
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                jobGrid.add(cell, c + 1, r + 1);
            }
        }
    }

    /* ================= 数据履历表格 ================= */
    private void setupTable() {
        colRow.setCellValueFactory(new PropertyValueFactory<>("rowNo"));
        colPos.setCellValueFactory(new PropertyValueFactory<>("position"));
        colMeasured.setCellValueFactory(new PropertyValueFactory<>("measured"));
        colStandard.setCellValueFactory(new PropertyValueFactory<>("standard"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        // 固定行高，杜绝行与行重叠错位
        dataTable.setFixedCellSize(28);
        dataTable.setItems(records);
    }

    private void initSampleData() {
        records.addAll(
                new MeasureRecord("1", "L", "3.167", "3.200", "False", "2024/4/25 12:22"),
                new MeasureRecord("1", "M", "2.241", "2.241", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49"),
                new MeasureRecord("1", "L", "3.412", "6.532", "True",  "2024/4/25 19:49")
        );
    }

    /* ================= 动作履历日志 ================= */
    private void appendLog(String message) {
        String ts = LocalDateTime.now().format(TIME_FMT);
        actionLog.appendText(ts + " " + message + "\n");
    }

    /* ================= 时钟 ================= */
    private void setupDateTime() {
        updateDateTime();
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateDateTime() {
        lblDateTime.setText("当前日期: " + LocalDateTime.now().format(DATE_TIME_FMT));
    }

    /* ================= 全屏显示 ================= */
    @FXML
    private void onToggleFullScreen() {
        if (jobGrid.getScene() == null || jobGrid.getScene().getWindow() == null) {
            return;
        }
        Stage stage = (Stage) jobGrid.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    private void setupFullScreen() {
        if (jobGrid.getScene() != null) {
            bindFullScreen(jobGrid.getScene());
        }
        // FXML 加载阶段 scene 尚未创建，监听 scene 挂载后再绑定
        jobGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                bindFullScreen(newScene);
            }
        });
    }

    private void bindFullScreen(Scene scene) {
        // F11 切换全屏
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F11) {
                onToggleFullScreen();
                e.consume();
            }
        });
        if (scene.getWindow() != null) {
            bindStage((Stage) scene.getWindow());
        } else {
            // Scene 已创建但还没挂到 Stage 上
            scene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
                if (newWindow != null) {
                    bindStage((Stage) newWindow);
                }
            });
        }
    }

    private void bindStage(Stage stage) {
        stage.setFullScreenExitHint("按 ESC 或 F11 退出全屏");
        stage.fullScreenProperty().addListener((obs, oldVal, isFull) -> {
            if (btnFullScreen != null) {
                btnFullScreen.setText(isFull ? "⤡" : "⛶");
                btnFullScreen.setTooltip(new Tooltip(isFull ? "退出全屏（F11）" : "全屏显示（F11）"));
            }
            appendLog(isFull ? "已切换为全屏显示" : "已退出全屏显示");
        });
    }

    /* ================= 拖动标题栏移动窗口 / 双击标题栏全屏 ================= */
    private void setupWindowDrag() {
        final double[] offset = new double[2];
        appHeader.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (isInteractive(e.getTarget())) {
                return;
            }
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });
        appHeader.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (isInteractive(e.getTarget()) || appHeader.getScene() == null) {
                return;
            }
            Window window = appHeader.getScene().getWindow();
            if (window == null || (window instanceof Stage stage && stage.isFullScreen())) {
                return;
            }
            window.setX(e.getScreenX() - offset[0]);
            window.setY(e.getScreenY() - offset[1]);
        });
        appHeader.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !isInteractive(e.getTarget())) {
                onToggleFullScreen();
            }
        });
    }

    /** 事件目标是否落在按钮或输入控件上（这些控件上的操作不参与窗口拖动/双击） */
    private boolean isInteractive(Object target) {
        Node node = target instanceof Node ? (Node) target : null;
        while (node != null) {
            if (node instanceof Button || node instanceof TextInputControl) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    /* ================= 人员操作 ================= */
    @FXML
    private void onDeviceStop() {
        lblRunState.setText("已停止");
        appendLog("设备停止指令已发送");
    }

    @FXML
    private void onDeviceStart() {
        lblRunState.setText("运行中");
        appendLog("设备启动指令已发送");
    }

    @FXML
    private void onDeviceReset() {
        appendLog("设备复位完成");
    }

    @FXML
    private void onHistoryQuery() {
        appendLog("履历查询完成，共 " + records.size() + " 条记录");
    }

    @FXML
    private void onLogQuery() {
        appendLog("日志查询完成");
    }

    @FXML
    private void onExportData() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("导出数据履历");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV 文件 (*.csv)", "*.csv"));
        chooser.setInitialFileName("data_history_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        Window owner = dataTable.getScene() != null ? dataTable.getScene().getWindow() : null;
        File file = chooser.showSaveDialog(owner);
        if (file == null) {
            appendLog("导出数据已取消");
            return;
        }
        try (OutputStreamWriter w = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            // 写入 UTF-8 BOM 以便 Excel 正确识别中文
            w.write('\uFEFF');
            w.write("行号,位置,测量值,标准值,结果,检测时间\n");
            for (MeasureRecord r : records) {
                w.write(String.join(",",
                        r.rowNoProperty().get(), r.positionProperty().get(),
                        r.measuredProperty().get(), r.standardProperty().get(),
                        r.resultProperty().get(), r.timeProperty().get()));
                w.write("\n");
            }
            w.flush();
            appendLog("数据已导出：" + file.getAbsolutePath() + "（共 " + records.size() + " 条）");
            new Alert(AlertType.INFORMATION,
                    "数据已成功导出至：\n" + file.getAbsolutePath()
                            + "\n\n共 " + records.size() + " 条记录").showAndWait();
        } catch (IOException ex) {
            appendLog("导出失败：" + ex.getMessage());
            new Alert(AlertType.ERROR, "导出失败：" + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onParam() {
        appendLog("打开参数设定窗口（TODO：接入参数配置界面）");
    }

    @FXML
    private void onClearRun() {
        completedCount = 0;
        lblCount.setText("0");
        appendLog("运行计数已清零");
    }

    @FXML
    private void onPower() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要退出系统吗？", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("退出确认");
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                if (clock != null) {
                    clock.stop();
                }
                javafx.application.Platform.exit();
            }
        });
    }

    /* ================= 表格数据模型 ================= */
    public static class MeasureRecord {
        private final StringProperty rowNo;
        private final StringProperty position;
        private final StringProperty measured;
        private final StringProperty standard;
        private final StringProperty result;
        private final StringProperty time;

        public MeasureRecord(String rowNo, String position, String measured,
                             String standard, String result, String time) {
            this.rowNo = new SimpleStringProperty(rowNo);
            this.position = new SimpleStringProperty(position);
            this.measured = new SimpleStringProperty(measured);
            this.standard = new SimpleStringProperty(standard);
            this.result = new SimpleStringProperty(result);
            this.time = new SimpleStringProperty(time);
        }

        public StringProperty rowNoProperty() { return rowNo; }
        public StringProperty positionProperty() { return position; }
        public StringProperty measuredProperty() { return measured; }
        public StringProperty standardProperty() { return standard; }
        public StringProperty resultProperty() { return result; }
        public StringProperty timeProperty() { return time; }
    }
}
