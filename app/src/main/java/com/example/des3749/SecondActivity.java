package com.example.des3749;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.des3749.data.ReferenceData;
import com.example.des3749.data.ResultData;
import com.example.des3749.handler.DataHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class SecondActivity extends AppCompatActivity {

    /* reference data 들을 넣는 map 을 static 으로 선언하여 하나로 유지 */
    public static HashMap<String, ReferenceData> referenceDataHashMap = new HashMap<>();

    Handler handler = new Handler();

    TextView textView;
    DataHandler dataHandler;
    ProgressDialog progressDialog;
    ResultData resultData;

    int buttonId;
    String buttonName;
    final int button1 = R.id.button1;
    final int button2 = R.id.button2;
    final int button3 = R.id.button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        textView = findViewById(R.id.textView);
        loadReferenceData(R.raw.itrc_snp_hypertension_sm);
        dataHandler = new DataHandler(SecondActivity.this);
        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

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
        progressDialog.show();
        buttonId = view.getId();
        buttonName = ((Button)view).getText().toString();
        textView.setText("");
        Thread thread = new Thread(null, loadData);
        thread.start();
    }

    private final Runnable loadData = new Runnable() {
        @Override
        public void run() {
            try {
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
                handler.post(updateResult);
            } catch (Exception e) {
                String resultText = "오류가 발생했습니다.";
                textView.setText(resultText);
            }
        }
    };

    private final Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            /* resultData 가 null 값이 반환 가능하므로 null 인지 아닌지 체크 */
            if (resultData != null) {
                String resultText = buttonName + " 분석 결과 \n"
                        + "P.VALUE 최소: " + resultData.getMinPvalData().getSNP() + " " + resultData.getMinPvalData().getPval() + "\n"
                        + "P.VALUE 최대: " + resultData.getMaxPvalData().getSNP() + " " + resultData.getMaxPvalData().getPval() + "\n"
                        + "geno 0 개수: " + resultData.getCount0() + "\n"
                        + "geno 1 개수: " + resultData.getCount1() + "\n"
                        + "geno 2 개수: " + resultData.getCount2() + "\n";
                textView.setText(resultText);
            } else {
                String resultText = "오류가 발생했습니다.";
                textView.setText(resultText);
            }
            progressDialog.dismiss();
        }
    };
}
