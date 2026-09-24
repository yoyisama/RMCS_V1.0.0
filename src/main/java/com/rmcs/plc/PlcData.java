package com.rmcs.plc;

/**
 * PLC → 上位机 的单帧作业数据（对应图中 DB36 映射）。
 * 由 PlcService 解析后通过 PlcListener.onData 回调。
 */
public class PlcData {

    /** DB36.DBW0 通信心跳 */
    private int heartbeat;
    /** DB36.DBB2（S7 STRING）当前产品名称 */
    private String productName = "";
    /** DB36.DBW258 数据更新标志（自增 1，满 1000 归 1） */
    private int updateFlag;
    /** DB36.DBW266 1=Y1 / 2=Y2 */
    private String side = "Y1";
    /** DB36.DBW264 列号（1/2） */
    private int col;
    /** DB36.DBW260 1=左 / 2=右 */
    private String lr = "左";
    /** DB36.DBW262 行号（1~15） */
    private int row;

    public int getHeartbeat() { return heartbeat; }
    public void setHeartbeat(int heartbeat) { this.heartbeat = heartbeat; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getUpdateFlag() { return updateFlag; }
    public void setUpdateFlag(int updateFlag) { this.updateFlag = updateFlag; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public String getLr() { return lr; }
    public void setLr(String lr) { this.lr = lr; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
}
