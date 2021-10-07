package com.example.des3749;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.des3749.data.ReferenceData;
import com.example.des3749.data.ResultData;
import com.example.des3749.handler.DataHandler;
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

public class SecondActivity extends AppCompatActivity {

    /* reference data 들을 넣는 map 을 static 으로 선언하여 하나로 유지 */
    public static HashMap<String, ReferenceData> referenceDataHashMap = new HashMap<>();

    Handler handler = new Handler();
    private ActivityResultLauncher<Intent> resultLauncher;

    TextView textView;
    Disposable backgroundTask;
    ContentResolver resolver;
    Uri fileUri;
    boolean isFileLoaded = false;

    /* 데이터 처리에 사용할 커스텀 클래스들 */
    DataHandler dataHandler;
    ResultData resultData;

    /* 로딩화면 보여줄 Dialog를 상속한 클래스 */
    ProgressDialog progressDialog;

    /* IP 주소 바뀔때마다 변경 */
    String serverIP = ConnectionInfo.ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        textView = findViewById(R.id.textView);
        dataHandler = new DataHandler(SecondActivity.this);
        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadReferenceData(serverIP);
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    // 파일 탐색기에서 파일을 선택한 뒤 실행
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                fileUri = intent.getData();
                            } else {
                                return;
                            }
                            resolver = getContentResolver();
                            String fileName = "선택된 파일: " + getFileName(fileUri);
                            textView.setText(fileName);
                            isFileLoaded = true;
                        }
                    }
                }
        );
    }

    /* reference data 들을 읽어오는 메소드 */
    private void loadReferenceData(String serverIP) {
        // onPreExecute
        progressDialog.show();

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
                        progressDialog.dismiss();
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

    /*
     * Test Data를 불러오고 데이터를 읽어 결과를 출력하는 로직
     */

    /* 파일을 불러오는 버튼을 누르면 파일 탐색기를 열고 테스트 데이터 파일을의 Uri를 가져오는 메소드 */
    public void onLoadButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*");
        resultLauncher.launch(intent);
    }

    /* test set 데이터 읽어오는 버튼을 눌렀을 때 데이터를 읽어오는 메소드 */
    public void onResultButtonClicked(View view) {
        if (!isFileLoaded) {
            alertError("오류", "불러온 파일이 없습니다.");
            return;
        }
        progressDialog.show(); // 로딩 화면을 띄워줌
        Thread thread = new Thread(null, loadData);
        thread.start();
    }

    /* DataHandler에서 데이터 처리를 해서 결과 값 가져오는 메소드 */
    private final Runnable loadData = new Runnable() {
        @Override
        public void run() {
            try {
                resultData = dataHandler.getTestData(fileUri, resolver);
                handler.post(printResult);
            } catch (Exception e) {
                e.printStackTrace();
                alertError("오류", "오류가 발생했습니다.");
                String resultText = "";
                textView.setText(resultText);
                isFileLoaded = false;
            }
        }
    };

    /* 가져온 결과 값을 출력하는 메소드 */
    private final Runnable printResult = new Runnable() {
        @Override
        public void run() {
            // 스레드 생성할 때 dialog를 show 했으므로 dismiss 해서 로딩 창 없애줌
            progressDialog.dismiss();
            String resultText;
            try {
                if (resultData != null) {
                    resultText = "분석 결과 \n"
                            + "P.VALUE 최소: " + resultData.getMinPvalData().getSNP() + " " + resultData.getMinPvalData().getPval() + "\n"
                            + "P.VALUE 최대: " + resultData.getMaxPvalData().getSNP() + " " + resultData.getMaxPvalData().getPval() + "\n"
                            + "geno 0 개수: " + resultData.getCount0() + "\n"
                            + "geno 1 개수: " + resultData.getCount1() + "\n"
                            + "geno 2 개수: " + resultData.getCount2() + "\n";
                } else {
                    resultText = "오류가 발생했습니다.";
                }
                textView.setText(resultText);
                isFileLoaded = false;
            } catch(NullPointerException e) {
                alertError("오류", "올바르지 않은 파일입니다.");
                resultText = "";
                textView.setText(resultText);
                isFileLoaded = false;
            }
        }
    };

    /*
     * 기타 유틸리티 메소드
     */

    /* 파일 탐색기에서 가져온 파일의 uri 정보를 바탕으로 파일의 이름을 가져오는 메소드 */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    /* 오류 발생시 오류 창 생성 메소드 */
    private void alertError(String title, String msg) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SecondActivity.this);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(msg);
        alertBuilder.show();
    }

    /* 오류 창을 생성하고 프로그램 자체를 종료시키는 메소드. HTTP 통신이 불가능 할 때 사용 */
    private void alertErrorAndExit() {
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
