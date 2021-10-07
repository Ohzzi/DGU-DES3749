package com.example.des3749.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResultData {

    private final InputData minPvalData;
    private final InputData maxPvalData;
    private final int count0;
    private final int count1;
    private final int count2;

}
