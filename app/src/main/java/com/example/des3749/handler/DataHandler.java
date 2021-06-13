package com.example.des3749.handler;

import android.content.Context;

import com.example.des3749.data.InputData;
import com.example.des3749.data.ReferenceData;
import com.example.des3749.data.ResultData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.example.des3749.SecondActivity.referenceDataHashMap;

public class DataHandler {

    /* P.VALUE max 값, min 값, geno 별 snip 개수 */
    private double maxPVAL = 0;
    private double minPVAL = 1;
    private int[] count = { 0, 0, 0 };

    /* P.VALUE 최소, 최대를 가지는 snip 데이터들 */
    private InputData minPvalData = null;
    private InputData maxPvalData = null;

    private final Context context;

    public DataHandler(Context context) {
        this.context = context;
    }

    public ResultData input(int id) {
        try {
            InputStream is = context.getResources().openRawResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ResultData resultData;
            String line = reader.readLine(); // 데이터의 맨 윗줄(데이터의 이름 표시) 지워 줌
            while ((line = reader.readLine()) != null) {
                String[] strArray = line.split(",");
                InputData data = new InputData(Integer.parseInt(strArray[0]), strArray[1], Integer.parseInt(strArray[2]));

                /* 조건: reference 들이 들어 있는 map 에 input 된 데이터의 snip 이 존재 */
                if (referenceDataHashMap.containsKey(data.getSNP())) {
                    ReferenceData ref = referenceDataHashMap.get(data.getSNP());
                    count[data.getGeno()]++;
                    double pval = ref.getPVAL();
                    data.setPval(pval);

                    /* P.VALUE 최대, 최소 값 계산 */
                    if (pval < minPVAL) {
                        minPVAL = pval;
                        minPvalData = data;
                    } else if (pval > maxPVAL) {
                        maxPVAL = pval;
                        maxPvalData = data;
                    }
                }
            }
            if (minPvalData != null && maxPvalData != null) {
                resultData = new ResultData(minPvalData, maxPvalData, count[0], count[1], count[2]);
            } else {
                return new ResultData(null, null, 0, 0, 0);
            }
            clear();
            return resultData;
        } catch (IOException e) {
            e.printStackTrace();
            clear();
            return null;
        }
    }

    public void clear() {
        /* result 반환한 이후 데이터를 초기화 하는 메소드 */
        this.maxPVAL = 0;
        this.minPVAL = 1;
        this.count[0] = 0;
        this.count[1] = 0;
        this.count[2] = 0;
        this.minPvalData = null;
        this.maxPvalData = null;
    }
}
