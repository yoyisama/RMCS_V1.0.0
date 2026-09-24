package com.rmcs.model;

/**
 * 作业位置（原型：Y1/Y2 两侧 × 第一列/第二列 × 左/右 × 第一行~第十五行）。
 * 作业矩阵与数据履历共用该定位，保证两个模块结构一致。
 */
public class WorkPos {

    /** 矩阵规模（按 PLC 对接图暂定 10 行 × 3 列，可在参数设定中调整）。 */
    private static int rows = 15;
    private static int cols = 2;

    /** 设置矩阵规模（行/列），参数设定保存后调用并重建矩阵。 */
    public static void setGrid(int r, int c) {
        rows = Math.max(1, Math.min(30, r));
        cols = Math.max(1, Math.min(10, c));
    }

    public static int getRows() { return rows; }
    public static int getCols() { return cols; }

    /** 单侧测点总数 = 列数 × 左右 × 行数。 */
    public static int perSide() { return cols * 2 * rows; }
    /** 全部测点总数 = Y1/Y2 两侧。 */
    public static int total() { return perSide() * 2; }

    private static final String[] CN_ROWS = {
            "第一行", "第二行", "第三行", "第四行", "第五行", "第六行", "第七行", "第八行",
            "第九行", "第十行", "第十一行", "第十二行", "第十三行", "第十四行", "第十五行"
    };

    private static final String[] CN_NUMS = {
            "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"
    };

    private final String side;   // Y1 / Y2
    private final int col;       // 1 / 2
    private final String lr;     // 左 / 右
    private final int row;       // 1..15

    public WorkPos(String side, int col, String lr, int row) {
        this.side = side;
        this.col = col;
        this.lr = lr;
        this.row = row;
    }

    public String getSide() { return side; }
    public int getCol() { return col; }
    public String getLr() { return lr; }
    public int getRow() { return row; }

    /** 矩阵键，形如 "Y1-1-L-5"。 */
    public String key() {
        return side + "-" + col + "-" + ("左".equals(lr) ? "L" : "R") + "-" + row;
    }

    /** 履历描述，形如 "Y1第一列左侧第一行"。 */
    public String describe() {
        return side + "第" + cnCol() + "列" + lr + "侧" + rowName();
    }

    /** 列中文名（实例）。 */
    public String cnCol() { return cnCol(col); }

    /** 列中文名（静态，支持任意列号）。 */
    public static String cnCol(int n) {
        return n >= 1 && n <= CN_NUMS.length ? CN_NUMS[n - 1] : String.valueOf(n);
    }

    /** 行中文名，形如 "第一行"；超过预置名时用 "第N行"。 */
    public String rowName() { return rowName(row); }

    public static String rowName(int row) {
        if (row >= 1 && row <= CN_ROWS.length) return CN_ROWS[row - 1];
        return "第" + row + "行";
    }

    /**
     * 按测量循环顺序取位置：Y1 整侧（行优先，列内先左后右）→ Y2 整侧。
     *
     * @param seq 循环序号（按当前矩阵规模循环）
     */
    public static WorkPos ofSequence(int seq) {
        int total = ((seq % total()) + total()) % total();
        int sideIdx = total / perSide();           // 0=Y1, 1=Y2
        int within = total % perSide();
        int perRow = cols * 2;                     // 每行测点数 = 列数 × 左右
        int rowIdx = within / perRow;
        int rem = within % perRow;                 // 0=列1左 1=列1右 2=列2左 ...
        int colIdx = rem / 2;                      // 0..cols-1
        int lrIdx = rem % 2;                       // 0=左 1=右
        return new WorkPos(sideIdx == 0 ? "Y1" : "Y2", colIdx + 1, lrIdx == 0 ? "左" : "右", rowIdx + 1);
    }
}
