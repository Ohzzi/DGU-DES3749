package com.example.firstapplication.data;

public class InputData {

    private final int CHR;
    private final String SNP;
    private final int geno;
    private Double Pval;


    public InputData(int CHR, String SNP, int geno) {
        this.CHR = CHR;
        this.SNP = SNP;
        this.geno = geno;
    }

    public int getCHR() {
        return CHR;
    }

    public String getSNP() {
        return SNP;
    }

    public int getGeno() {
        return geno;
    }

    public Double getPval() {
        return Pval;
    }

    public void setPval(Double pval) {
        Pval = pval;
    }
}
