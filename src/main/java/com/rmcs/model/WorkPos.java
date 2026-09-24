package com.rmcs.model;

/**
 * 作业位置（原型：Y1/Y2 两侧 × 第一列/第二列 × 左/右 × 第一行~第十五行）。
 * 作业矩阵与数据履历共用该定位，保证两个模块结构一致。
 */
public class WorkPos {

    /** 每侧行数。 */
    public static final int ROWS = 15;
    /** 单侧测点总数 = 60（2 列 × 左右 × 15 行）。 */
    public static final int PER_SIDE = 2 * 2 * ROWS;
    /** 全部位置总数 = Y1/Y2 两侧。 */
    public static final int TOTAL = PER_SIDE * 2;

    private static final String[] CN_ROWS = {
            "第一行", "第二行", "第三行", "第四行", "第五行", "第六行", "第七行", "第八行",
            "第九行", "第十行", "第十一行", "第十二行", "第十三行", "第十四行", "第十五行"
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

    /** 列中文名。 */
    public String cnCol() { return col == 1 ? "一" : "二"; }

    /** 行中文名，形如 "第一行"。 */
    public String rowName() { return CN_ROWS[row - 1]; }

    public static String rowName(int row) { return CN_ROWS[row - 1]; }

    /**
     * 按测量循环顺序取位置：Y1 整侧（行优先，列内先左后右）→ Y2 整侧。
     *
     * @param seq 循环序号（0..119 循环）
     */
    public static WorkPos ofSequence(int seq) {
        int total = ((seq % TOTAL) + TOTAL) % TOTAL;
        int sideIdx = total / PER_SIDE;            // 0=Y1, 1=Y2
        int within = total % PER_SIDE;             // 0..59
        int rowIdx = within / 4;                   // 0..14
        int rem = within % 4;                      // 0=列1左 1=列1右 2=列2左 3=列2右
        int colIdx = rem / 2;                      // 0..1
        int lrIdx = rem % 2;                       // 0=左 1=右
        return new WorkPos(sideIdx == 0 ? "Y1" : "Y2", colIdx + 1, lrIdx == 0 ? "左" : "右", rowIdx + 1);
    }
}
