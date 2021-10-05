package com.example.des3749.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class ReferenceData {

    private final String SNP;
    private final int CHR;
    private final String phenotype;
    private final double BETAOR;
    private final double PVAL;
    private final long BP;
    private final String minor;
    private final String major;

}