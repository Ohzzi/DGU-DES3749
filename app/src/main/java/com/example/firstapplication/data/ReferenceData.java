package com.example.firstapplication.data;

public class ReferenceData {

    private final String SNP;
    private final int CHR;
    private final String phenotype;
    private final double BETAOR;
    private final double PVAL;
    private final long BP;
    private final String minor;
    private final String major;

    public ReferenceData(String[] data) {
        this.SNP = data[0];
        this.CHR = Integer.parseInt(data[1]);
        this.phenotype = data[2];
        this.BETAOR = Double.parseDouble(data[3]);
        this.PVAL = Double.parseDouble(data[4]);
        this.BP = Long.parseLong(data[5]);
        this.minor = data[6];
        this.major = data[7];
    }

    public String getSNP() {
        return SNP;
    }

    public int getCHR() {
        return CHR;
    }

    public String getPhenotype() {
        return phenotype;
    }

    public double getBETAOR() {
        return BETAOR;
    }

    public double getPVAL() {
        return PVAL;
    }

    public long getBP() {
        return BP;
    }

    public String getMinor() {
        return minor;
    }

    public String getMajor() {
        return major;
    }
}