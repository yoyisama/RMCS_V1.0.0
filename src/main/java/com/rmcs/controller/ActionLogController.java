package com.rmcs.controller;

import com.rmcs.model.LogEntry;
import com.rmcs.model.LogEntry.LogType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 系统运行日志面板（原型 ActionLogPanel）：
 * 保留最近 200 条全量日志，支持 全部 / PLC / 串口 / 报警 四类筛选，并可展开完整日志窗口。
 */
public class ActionLogController implements Initializable {

    /** 面板最多保留条数（全量日志仍可供查询弹窗使用）。 */
    private static final int MAX_LOGS = 200;

    @FXML private ListView<LogEntry> logList;
    @FXML private Label lblCount;
    @FXML private ToggleButton tbAll, tbSystem, tbPlc, tbCom, tbAlarm;
    @FXML private ToggleGroup logFilterGroup;

    private ObservableList<LogEntry> logs = FXCollections.observableArrayList();
    private FilteredList<LogEntry> filtered;
    private Runnable onOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filtered = new FilteredList<>(logs, e -> true);
        logList.setItems(filtered);
        // 富文本单元格：时间戳（灰）/ 分类标签（按类型着色）/ 内容（按类型着色）
        logList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LogEntry item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("log-line", "log-info", "log-system", "log-plc", "log-com",
                        "log-success", "log-warning", "log-error");
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                getStyleClass().addAll("log-line", "log-" + item.getType().name().toLowerCase());

                javafx.scene.text.Text time = new javafx.scene.text.Text("[" + item.getFormattedTime() + "]  ");
                javafx.scene.text.Text tag = new javafx.scene.text.Text("[" + tagOf(item.getType()) + "]  ");
                javafx.scene.text.Text msg = new javafx.scene.text.Text(item.getMessage() == null ? "" : item.getMessage());
                time.getStyleClass().add("log-time");
                tag.getStyleClass().add("log-tag");
                msg.getStyleClass().add("log-msg");

                javafx.scene.text.TextFlow flow = new javafx.scene.text.TextFlow(time, tag, msg);
                setGraphic(flow);
                setText(null);
            }

            private String tagOf(LogEntry.LogType t) {
                switch (t) {
                    case SYSTEM:  return "系统";
                    case PLC:     return "PLC";
                    case COM:     return "串口";
                    case SUCCESS: return "成功";
                    case WARNING: return "报警";
                    case ERROR:   return "错误";
                    default:      return "信息";
                }
            }
        });

        tbAll.setUserData("all");
        tbSystem.setUserData("system");
        tbPlc.setUserData("plc");
        tbCom.setUserData("com");
        tbAlarm.setUserData("alarm");
        logFilterGroup.selectedToggleProperty().addListener((obs, old, t) -> {
            if (t == null) {
                tbAll.setSelected(true);
                return;
            }
            applyFilter(String.valueOf(t.getUserData()));
        });
        applyFilter("all");
    }

    /** 按原型的四类筛选规则过滤（PLC / 串口 / 报警），并刷新记录数。 */
    private void applyFilter(String key) {
        filtered.setPredicate(e -> {
            switch (key) {
                case "system":
                    return e.getType() == LogType.SYSTEM
                            || matches(e, "系统|控制台|参数|配置|操作员|登录|登出|退出|主题|间隔|模式");
                case "plc":
                    return e.getType() == LogType.PLC || matches(e, "plc|db|进料|出料|步进|伺服|气缸");
                case "com":
                    return e.getType() == LogType.COM || matches(e, "com|电阻|阻值|采样|数据采集|校准|串口");
                case "alarm":
                    return e.getType() == LogType.ERROR || e.getType() == LogType.WARNING
                            || matches(e, "报警|超差|异常|失败|预警|错误");
                default:
                    return true;
            }
        });
        updateCount();
    }

    private static boolean matches(LogEntry e, String regex) {
        return e.getMessage() != null
                && java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(e.getMessage()).find();
    }

    private void updateCount() {
        if (lblCount != null) {
            lblCount.setText(String.valueOf(filtered == null ? 0 : filtered.size()));
        }
    }

    public void append(LogEntry e) {
        if (e == null) return;
        Platform.runLater(() -> {
            logs.add(0, e);
            if (logs.size() > MAX_LOGS) logs.remove(MAX_LOGS, logs.size());
            updateCount();
        });
    }

    public void append(LogType type, String msg) {
        append(new LogEntry(String.valueOf(System.nanoTime()), type, msg));
    }

    /** 返回全量日志（供「日志查询」弹窗使用，不受面板筛选影响）。 */
    public ObservableList<LogEntry> getLogs() {
        return logs;
    }

    public void clearAll() {
        logs.clear();
        updateCount();
    }

    public void setOnOpen(Runnable r) {
        this.onOpen = r;
    }

    /** 使用外部共享日志列表（Y1/Y2 两个 Tab 共用同一份系统日志）。 */
    public void useList(ObservableList<LogEntry> list) {
        this.logs = list;
        this.filtered = new FilteredList<>(logs, e -> true);
        logList.setItems(filtered);
        logs.addListener((javafx.collections.ListChangeListener<LogEntry>) c -> updateCount());
        applyFilter("all");
    }

    @FXML
    public void openLogQuery() {
        if (onOpen != null) onOpen.run();
    }
}
