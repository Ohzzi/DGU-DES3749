package com.example.des3749;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.des3749.data.ReferenceData;
import com.example.des3749.info.ConnectionInfo;

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

public class MainActivity extends AppCompatActivity {

    /* reference data 들을 넣는 map 을 static 으로 선언하여 하나로 유지 */
    public static HashMap<String, ReferenceData> referenceDataHashMap = new HashMap<>();
    Disposable backgroundTask;
    String ip = ConnectionInfo.ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadReferenceData(ip);
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
            }
        }, 1000);
    }

    /* reference data 들을 읽어오는 메소드 */
    private void loadReferenceData(String serverIP) {
        // onPreExecute

        backgroundTask = Observable.fromCallable(() -> {
            // doInBackground
            try {
                URL url = new URL("http://" + serverIP + "/reference.php");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(10000);
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
                return sb.toString().trim(); // trim: 공백 제거
            } catch (Exception e) {
                e.printStackTrace();
                alertErrorAndExit();
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Throwable {
                        //onPostExcuse
                        if (result != null) {
                            getReferenceData(result);
                        } else {
                            alertErrorAndExit();
                        }
                        backgroundTask.dispose();
                    }
                });

    }

    private void getReferenceData(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                // JSON 데이터 파싱
                String SNP = object.getString("SNP");
                int CHR = Integer.parseInt(object.getString("CHR"));
                String PHENOTYPE = object.getString("PHENOTYPE");
                double BETAOR = Double.parseDouble(object.getString("BETA.OR."));
                double PVAL = Double.parseDouble(object.getString("P.VAL"));
                int BP = Integer.parseInt(object.getString("BP"));
                String minor = object.getString("minor");
                String major = object.getString("major");

                // 파싱한 데이터를 바탕으로 Reference 데이터를 만들고 Map에 저장
                ReferenceData referenceData = new ReferenceData(SNP, CHR, PHENOTYPE, BETAOR, PVAL, BP, minor, major);
                referenceDataHashMap.put(SNP, referenceData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            alertErrorAndExit();
        }
    }

    /* 오류 창을 생성하고 프로그램 자체를 종료시키는 메소드. HTTP 통신이 불가능 할 때 사용 */
    private void alertErrorAndExit() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
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