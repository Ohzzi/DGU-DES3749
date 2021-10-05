package com.example.des3749;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.des3749.data.ReferenceData;
import com.example.des3749.data.ResultData;
import com.example.des3749.handler.DataHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SecondActivity extends AppCompatActivity {

    /* reference data 들을 넣는 map 을 static 으로 선언하여 하나로 유지 */
    public static HashMap<String, ReferenceData> referenceDataHashMap = new HashMap<>();

    Handler handler = new Handler();

    TextView textView;
    DataHandler dataHandler;
    ProgressDialog progressDialog;
    ResultData resultData;
    String jsonString;
    Disposable backgroundTask;

    // IP 주소 바뀔때마다 변경
    String serverIP = ConnectionInfo.ip;

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
        dataHandler = new DataHandler(SecondActivity.this);
        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadReferenceData(serverIP);
    }

    /* reference data 들을 읽어오는 메소드 */
    private void loadReferenceData(String serverIP) {
        // onPreExecute
        progressDialog.show();

        backgroundTask = Observable.fromCallable(() -> {
            // doInBackground
            try {
                URL url = new URL(serverIP + "reference.jsp");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                int responseStatus = httpURLConnection.getResponseCode();
                InputStream inputStream;
                if (responseStatus == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString().trim();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Throwable {
                        //onPostExcuse
                        progressDialog.dismiss();
                        if (result != null) {
                            jsonString = result;
                            getReferenceData();
                        } else {
                            alertError();
                        }
                        backgroundTask.dispose();
                    }
                });
    }

    private void getReferenceData() {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("Tree");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                String SNP = object.getString("SNP");
                int CHR = Integer.parseInt(object.getString("CHR"));
                String PHENOTYPE = object.getString("PHENOTYPE");
                double BETAOR = Double.parseDouble(object.getString("BETA.OR."));
                double PVAL = Double.parseDouble(object.getString("P.VAL"));
                int BP = Integer.parseInt(object.getString("BP"));
                String minor = object.getString("minor");
                String major = object.getString("major");

                ReferenceData referenceData = new ReferenceData(SNP, CHR, PHENOTYPE, BETAOR, PVAL, BP, minor, major);
                referenceDataHashMap.put(SNP, referenceData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* test set 데이터 읽어오는 버튼을 눌렀을 때 데이터를 읽어오는 메소드 */
    public void onLoadButtonClicked(View view) {
        progressDialog.show(); // 로딩 화면을 띄워줌
        buttonId = view.getId();
        buttonName = ((Button) view).getText().toString();
        textView.setText("");
        //Thread thread = new Thread(null, getTestData);
        //thread.start();
    }

    /* 데이터를 읽어오는 메소드. 멀티스레드 환경에서 실행 */
    /*
    private final Runnable getTestData = new Runnable() {
        @Override
        public void run() {
            URL url;
            try {
                // 클릭된 버튼과 버튼 1, 2, 3의 id를 비교해서 알맞는 데이터를 읽도록 설정
                switch (buttonId) {
                    case button1:
                        url = new URL(serverIP + "test1.jsp");
                        break;
                    case button2:
                        url = new URL(serverIP + "test2.jsp");
                        break;
                    case button3:
                        url = new URL(serverIP + "test3.jsp");
                        break;
                    default:
                        url = null;
                        alertError();
                        break;
                }
                backgroundTask = Observable.fromCallable(() -> {
                    // doInBackground
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.connect();

                        int responseStatus = httpURLConnection.getResponseCode();
                        InputStream inputStream;
                        if (responseStatus == HttpURLConnection.HTTP_OK) {
                            inputStream = httpURLConnection.getInputStream();
                        } else {
                            inputStream = httpURLConnection.getErrorStream();
                        }
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        return sb.toString().trim();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String result) throws Throwable {
                                //onPostExcuse
                                progressDialog.dismiss();
                                if (result != null) {
                                    jsonString = result;
                                    resultData = dataHandler.getTestData(jsonString);
                                    handler.post(updateResult);
                                } else {
                                    alertError();
                                }
                                backgroundTask.dispose();
                            }
                        });
            } catch (Exception e) {
                String resultText = "오류가 발생했습니다.";
                textView.setText(resultText);
            }
        }
    };
    */

    /* loadData에서 데이터를 읽으면 데이터 출력을 위해 실행하는 이벤트 */
    /*
    private final Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            // resultData 가 null 값이 반환 가능하므로 null 인지 아닌지 체크
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
            progressDialog.dismiss(); // 로딩이 끝났으므로 로딩화면 제거
        }
    };
     */

    private void alertError() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SecondActivity.this);
        alertBuilder.setTitle("오류");
        alertBuilder.setMessage("오류가 발생했습니다. 재시작해주세요");
        alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alertBuilder.show();
    }
}
