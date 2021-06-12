package com.example.des3749.data;

public class ResultData {

    private final InputData minPvalData;
    private final InputData maxPvalData;
    private final int count0;
    private final int count1;
    private final int count2;

    public ResultData(InputData minPvalData, InputData maxPvalData, int count0, int count1, int count2) {
        this.minPvalData = minPvalData;
        this.maxPvalData = maxPvalData;
        this.count0 = count0;
        this.count1 = count1;
        this.count2 = count2;
    }

    public InputData getMinPvalData() {
        return minPvalData;
    }

    public InputData getMaxPvalData() {
        return maxPvalData;
    }

    public int getCount0() {
        return count0;
    }

    public int getCount1() {
        return count1;
    }

    public int getCount2() {
        return count2;
    }
}
