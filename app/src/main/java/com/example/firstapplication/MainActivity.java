package com.example.firstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.firstapplication.data.ReferenceData;
import com.example.firstapplication.data.ResultData;
import com.example.firstapplication.handler.DataHandler;
import com.example.firstapplication.handler.TextHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /* reference data 들을 넣는 map 을 static 으로 선언하여 하나로 유지 */
    public static HashMap<String, ReferenceData> referenceDataHashMap = new HashMap<>();

    TextView textView;
    // TextHandler textHandler;
    DataHandler dataHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        // textHandler = new TextHandler(MainActivity.this);
        loadReferenceData(R.raw.itrc_snp_hypertension_sm);
        dataHandler = new DataHandler(MainActivity.this);
    }

    /* 사용 x
    public void onTextLoadButtonClicked(View view) {
        int resId = R.raw.sample;
        String text = textHandler.readTextResource(resId);
        //String fileName = "sample.txt";
        //String text = textHandler.readTextFileByAssets(fileName);
        textView.setText(text);
    }*/

    /* reference data 들을 읽어오는 메소드 */
    public void loadReferenceData(int resId) {
        try {
            InputStream is = this.getResources().openRawResource(resId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine(); // 데이터의 맨 윗줄(데이터의 이름 표시) 지워 줌
            while ((line = reader.readLine()) != null) {
                String[] strArray = line.split(",");
                referenceDataHashMap.put(strArray[0], new ReferenceData(strArray));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* test set 데이터 읽어오는 버튼을 눌렀을 때 데이터를 읽어오는 메소드 */
    public void onLoadButtonClicked(View view) {
        int buttonId = view.getId();
        final int button1 = R.id.button1;
        final int button2 = R.id.button2;
        final int button3 = R.id.button3;
        ResultData resultData;

        /* 클릭된 버튼과 버튼 1, 2, 3의 id를 비교해서 알맞는 데이터를 읽도록 설정 */
        switch (buttonId) {
            case button1:
                resultData = dataHandler.input(R.raw.test_set1);
                break;
            case button2:
                resultData = dataHandler.input(R.raw.test_set2);
                break;
            case button3:
                resultData = dataHandler.input(R.raw.test_set3);
                break;
            default:
                resultData = null;
                break;
        }

        /* resultData 가 null 값이 반환 가능하므로 null 인지 아닌지 체크 */
        if (resultData != null) {
            String resultText = "P.VALUE 최소: " + resultData.getMinPvalData().getSNP() + " " + resultData.getMinPvalData().getPval() + "\n"
                    + "P.VALUE 최대: " + resultData.getMaxPvalData().getSNP() + " " + resultData.getMaxPvalData().getPval() + "\n"
                    + "geno 0 개수: " + resultData.getCount0() + "\n"
                    + "geno 1 개수: " + resultData.getCount1() + "\n"
                    + "geno 2 개수: " + resultData.getCount2() + "\n";
            textView.setText(resultText);
        } else {
            String resultText = "오류가 발생했습니다.";
            textView.setText(resultText);
        }
    }
}