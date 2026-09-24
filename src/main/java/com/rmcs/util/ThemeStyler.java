package com.rmcs.util;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 主题内联着色器（保底方案）。
 *
 * 背景：该环境下 CSS 级联对部分控件不可靠（黑夜模式按钮白底），因此用
 * Node.setStyle 内联样式（优先级最高）直接着色。
 *
 * 特性：
 * 1. 所有 ButtonBase / TextInputControl 无条件兜底着色，黑夜模式无白色控件；
 * 2. 档位/筛选 ToggleButton 挂 selected 监听，点击立即切换高亮；
 * 3. 圆形图标按钮（主题切换/全屏/注销）、模式按钮保留项目原生配色；
 * 4. 悬浮反馈用监听补回，且动态读取当前主题（切换主题后不会回退旧配色）。
 */
public final class ThemeStyler {

    private static final String DEBUG_FILE = "d:/work/yoyisama/RMCS_V1.0.0/target/theme_debug.txt";
    private static final String STYLED_KEY = "rmcs-theme-styled";

    /** 当前主题（apply 时更新；监听器闭包动态读取，避免主题切换后回退旧配色）。 */
    private static volatile boolean currentDark = false;

    private ThemeStyler() {
    }

    /** 对整个场景应用内联主题色。单个节点失败不影响其余节点。 */
    public static void apply(Scene scene, boolean dark) {
        if (scene == null || scene.getRoot() == null) return;
        currentDark = dark;
        int buttons = 0, inputs = 0, headers = 0;
        for (Node n : scene.getRoot().lookupAll("*")) {
            try {
                int[] r = applyNode(n, dark);
                buttons += r[0];
                inputs += r[1];
                headers += r[2];
            } catch (Exception ignored) {
                // 单节点着色失败不影响整体
            }
        }
        dbg("ThemeStyler dark=" + dark + " buttons=" + buttons + " inputs=" + inputs + " headers=" + headers);
    }

    /** 返回 {按钮数, 输入控件数, 表头节点数}。 */
    private static int[] applyNode(Node n, boolean dark) {
        List<String> c = n.getStyleClass();
        int[] r = new int[3];
        boolean already = n.getProperties().containsKey(STYLED_KEY);

        // ---- 输入控件（无条件兜底） ----
        if (n instanceof TextInputControl || n instanceof ComboBox) {
            n.setStyle(dark
                    ? "-fx-background-color:#12141a;-fx-border-color:#2c3342;-fx-text-fill:#e2e8f0;-fx-prompt-text-fill:#64748b;"
                    : "-fx-background-color:#ffffff;-fx-border-color:#cbd5e1;-fx-text-fill:#1e293b;-fx-prompt-text-fill:#94a3b8;");
            r[1] = 1;
            n.getProperties().put(STYLED_KEY, true);
            return r;
        }

        // ---- 表格表头 ----
        if (c.contains("column-header-background") || c.contains("column-header")
                || c.contains("filler") || c.contains("nested-column-header")) {
            n.setStyle(dark ? "-fx-background-color:#11131a;" : "-fx-background-color:#f1f5f9;");
            r[2] = 1;
            n.getProperties().put(STYLED_KEY, true);
            return r;
        }
        if (c.contains("label") && n.getParent() != null
                && n.getParent().getStyleClass().contains("column-header")) {
            n.setStyle(dark ? "-fx-text-fill:#94a3b8;" : "-fx-text-fill:#475569;");
            r[2] = 1;
            n.getProperties().put(STYLED_KEY, true);
            return r;
        }

        // ---- 项目专属色块：上/下偏差（原黄色底，改为项目徽章风格） ----
        if (c.contains("dev-chip")) {
            n.setStyle("-fx-background-color:#f59e0b22;-fx-border-color:#f59e0b3d;-fx-border-radius:8;");
            return r;
        }
        if (c.contains("dev-value")) {
            n.setStyle(dark ? "-fx-text-fill:#f59e0b;" : "-fx-text-fill:#b45309;");
            return r;
        }
        if (c.contains("dev-label")) {
            n.setStyle(dark ? "-fx-text-fill:#94a3b8;" : "-fx-text-fill:#64748b;");
            return r;
        }

        // ---- 档位 / 分段切换按钮：挂选中监听（点击立即切换高亮）与悬停监听 ----
        if (n instanceof ToggleButton && (c.contains("interval-chip") || c.contains("seg-btn"))) {
            ToggleButton tb = (ToggleButton) n;
            tb.setStyle(chipStyle(tb, dark, false));
            r[0] = 1;
            if (!already) {
                tb.selectedProperty().addListener((o, was, is) ->
                        tb.setStyle(chipStyle(tb, currentDark, false)));
                // 内联样式会吃掉 CSS :hover，这里用监听补回悬停变色
                tb.hoverProperty().addListener((o, was, is) ->
                        tb.setStyle(chipStyle(tb, currentDark, is)));
            }
            n.getProperties().put(STYLED_KEY, true);
            return r;
        }

        // ---- 其它按钮 ----
        if (n instanceof ButtonBase) {
            List<String> cc = c;
            if (buttonBase(n, dark).equals(buttonFallback(n, dark))) {
                // 命中兜底分支：记录真实类名，便于排查类名匹配失效的控件
                dbg("fallback-button id=" + n.getId() + " classes=" + cc);
            }
            n.setStyle(buttonBase(n, dark));
            r[0] = 1;
            if (!already) {
                ButtonBase b = (ButtonBase) n;
                b.hoverProperty().addListener((o, was, is) ->
                        b.setStyle(is ? buttonHover(n, currentDark) : buttonBase(n, currentDark)));
            }
            n.getProperties().put(STYLED_KEY, true);
            return r;
        }
        return r;
    }

    /** 档位 / 分段按钮样式（含选中态与悬停态）。 */
    private static String chipStyle(ToggleButton tb, boolean dark, boolean hover) {
        List<String> c = tb.getStyleClass();
        if (c.contains("seg-btn")) {
            if (tb.isSelected()) return "-fx-background-color:#0078d4;-fx-text-fill:white;";
            if (hover) return dark
                    ? "-fx-background-color:#1f2635;-fx-text-fill:#e2e8f0;"
                    : "-fx-background-color:#e2e8f0;-fx-text-fill:#1e293b;";
            return dark ? "-fx-background-color:transparent;-fx-text-fill:#94a3b8;"
                    : "-fx-background-color:transparent;-fx-text-fill:#64748b;";
        }
        // interval-chip
        if (tb.isSelected()) return "-fx-background-color:#0078d4;-fx-border-color:#0078d4;-fx-text-fill:white;";
        if (hover) return dark
                ? "-fx-background-color:#1f2635;-fx-border-color:#38bdf8;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#e2e8f0;-fx-border-color:#cbd5e1;-fx-text-fill:#1e293b;";
        return dark
                ? "-fx-background-color:#11131a;-fx-border-color:#2c3342;-fx-text-fill:#cbd5e1;"
                : "-fx-background-color:#f1f5f9;-fx-border-color:#e2e8f0;-fx-text-fill:#475569;";
    }

    /** 按钮常态样式（项目原生配色）。 */
    private static String buttonBase(Node n, boolean dark) {
        List<String> c = n.getStyleClass();
        // fx:id 兜底：核心机组控制三键（类名匹配在此环境下偶发失效，直接按 id 定位）
        String id = n.getId();
        if ("btnStart".equals(id)) return "-fx-background-color:#22c55e;-fx-border-color:#22c55e;-fx-text-fill:white;";
        if ("btnStop".equals(id)) return "-fx-background-color:#ef4444;-fx-border-color:#ef4444;-fx-text-fill:white;";
        if ("btnReset".equals(id)) return dark
                ? "-fx-background-color:#181e2b;-fx-border-color:#29364d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#eef2f7;-fx-border-color:#cbd5e1;-fx-text-fill:#1e293b;";
        // 圆形图标按钮组：主题切换（浅灰）、全屏（蓝）、注销（红）——保留原型配色
        if (c.contains("btn-power")) return "-fx-background-color:#ef4444;-fx-text-fill:white;";
        if (c.contains("btn-circle-blue")) return "-fx-background-color:#0078d4;-fx-text-fill:white;";
        if (c.contains("btn-circle-ghost")) return dark
                ? "-fx-background-color:#252c3b;-fx-border-color:#384357;-fx-text-fill:#fcd34d;"
                : "-fx-background-color:#f1f5f9;-fx-border-color:#cbd5e1;-fx-text-fill:#334155;";
        if (c.contains("mode-btn")) {
            // 透明底 + 绿/橙文字
            String fill = c.contains("mode-auto")
                    ? (dark ? "#4ade80" : "#2e7d32")
                    : (c.contains("mode-manual") ? (dark ? "#fbbf24" : "#f57c00") : null);
            return "-fx-background-color:transparent;-fx-border-color:transparent;"
                    + (fill == null ? "" : "-fx-text-fill:" + fill + ";");
        }
        if (c.contains("btn-outline-primary")) return dark
                ? "-fx-background-color:#12141a;-fx-border-color:#0078d4;-fx-text-fill:#38bdf8;"
                : "-fx-background-color:#eef4fb;-fx-border-color:#0078d4;-fx-text-fill:#0078d4;";
        // 操作按钮：整个按钮底色即彩色区域（蓝/黄/紫 半透明着色，与图标圆片同色系）
        if (c.contains("op-btn-sky")) return dark
                ? "-fx-background-color:#38bdf826;-fx-border-color:#38bdf84d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#38bdf81f;-fx-border-color:#38bdf859;-fx-text-fill:#1e293b;";
        if (c.contains("op-btn-amber")) return dark
                ? "-fx-background-color:#fbbf2426;-fx-border-color:#fbbf244d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#fbbf241f;-fx-border-color:#fbbf2459;-fx-text-fill:#1e293b;";
        if (c.contains("op-btn-purple")) return dark
                ? "-fx-background-color:#a78bfa26;-fx-border-color:#a78bfa4d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#a78bfa1f;-fx-border-color:#a78bfa59;-fx-text-fill:#1e293b;";
        if (c.contains("btn-primary")) return "-fx-background-color:#0078d4;-fx-border-color:#0078d4;-fx-text-fill:white;";
        if (c.contains("btn-success")) return "-fx-background-color:#22c55e;-fx-border-color:#22c55e;-fx-text-fill:white;";
        if (c.contains("btn-danger")) return "-fx-background-color:#ef4444;-fx-border-color:#ef4444;-fx-text-fill:white;";
        if (c.contains("btn-warning")) return "-fx-background-color:#f59e0b;-fx-border-color:#f59e0b;-fx-text-fill:white;";
        if (c.contains("btn-ghost") || c.contains("op-btn")) return dark
                ? "-fx-background-color:#181e2b;-fx-border-color:#29364d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#eef2f7;-fx-border-color:#cbd5e1;-fx-text-fill:#1e293b;";
        if (c.contains("btn-icon")) return dark
                ? "-fx-background-color:#181e2b;-fx-border-color:#29364d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#eef2f7;-fx-border-color:#cbd5e1;-fx-text-fill:#1e293b;";
        return buttonFallback(n, dark);
    }

    /** 兜底样式：主题底色，绝不留白。 */
    private static String buttonFallback(Node n, boolean dark) {
        return dark
                ? "-fx-background-color:#181e2b;-fx-border-color:#29364d;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#eef2f7;-fx-border-color:#cbd5e1;-fx-text-fill:#1e293b;";
    }

    /** 按钮悬浮样式。 */
    private static String buttonHover(Node n, boolean dark) {
        List<String> c = n.getStyleClass();
        String id = n.getId();
        if ("btnStart".equals(id)) return "-fx-background-color:#16a34a;-fx-border-color:#16a34a;-fx-text-fill:white;";
        if ("btnStop".equals(id)) return "-fx-background-color:#dc2626;-fx-border-color:#dc2626;-fx-text-fill:white;";
        if ("btnReset".equals(id)) return dark
                ? "-fx-background-color:#1f2635;-fx-border-color:#38bdf8;-fx-text-fill:#38bdf8;"
                : "-fx-background-color:#dbe7f5;-fx-border-color:#0078d4;-fx-text-fill:#005a9e;";
        if (c.contains("btn-power")) return "-fx-background-color:#b71c1c;-fx-text-fill:white;";
        // 操作按钮悬停：同色系加深
        if (c.contains("op-btn-sky")) return dark
                ? "-fx-background-color:#38bdf840;-fx-border-color:#38bdf88c;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#38bdf833;-fx-border-color:#38bdf87a;-fx-text-fill:#1e293b;";
        if (c.contains("op-btn-amber")) return dark
                ? "-fx-background-color:#fbbf2440;-fx-border-color:#fbbf248c;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#fbbf2433;-fx-border-color:#fbbf247a;-fx-text-fill:#1e293b;";
        if (c.contains("op-btn-purple")) return dark
                ? "-fx-background-color:#a78bfa40;-fx-border-color:#a78bfa8c;-fx-text-fill:#e2e8f0;"
                : "-fx-background-color:#a78bfa33;-fx-border-color:#a78bfa7a;-fx-text-fill:#1e293b;";
        if (c.contains("btn-circle-blue")) return "-fx-background-color:#0063b1;-fx-text-fill:white;";
        if (c.contains("btn-circle-ghost")) return dark
                ? "-fx-background-color:#31394c;-fx-border-color:#384357;-fx-text-fill:#fcd34d;"
                : "-fx-background-color:#e2e8f0;-fx-border-color:#cbd5e1;-fx-text-fill:#334155;";
        if (c.contains("mode-btn")) return buttonBase(n, dark);
        if (c.contains("btn-primary")) return "-fx-background-color:#0063b1;-fx-border-color:#0063b1;-fx-text-fill:white;";
        if (c.contains("btn-success")) return "-fx-background-color:#16a34a;-fx-border-color:#16a34a;-fx-text-fill:white;";
        if (c.contains("btn-danger")) return "-fx-background-color:#dc2626;-fx-border-color:#dc2626;-fx-text-fill:white;";
        if (c.contains("btn-warning")) return "-fx-background-color:#d97706;-fx-border-color:#d97706;-fx-text-fill:white;";
        return dark
                ? "-fx-background-color:#1f2635;-fx-border-color:#38bdf8;-fx-text-fill:#38bdf8;"
                : "-fx-background-color:#dbe7f5;-fx-border-color:#0078d4;-fx-text-fill:#005a9e;";
    }

    private static void dbg(String line) {
        try {
            Files.write(Paths.get(DEBUG_FILE),
                    (line + "\n").getBytes("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
}
