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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

    Handler handler = new Handler();
    private ActivityResultLauncher<Intent> resultLauncher;

    TextView textView;
    Spinner spinner;
    ArrayAdapter genderAdapter;
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

    /* EditText로 입력받을 값 */
    String userName;
    String age;
    String height;
    String weight;
    String bp_min;
    String bp_max;
    String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initialize();
    }

    public void initialize() {
        progressDialog = new ProgressDialog(this);
        textView = findViewById(R.id.text_view);
        spinner = (Spinner)findViewById(R.id.gender_spinner);
        genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_layout);
        spinner.setAdapter(genderAdapter);

        dataHandler = new DataHandler(SecondActivity.this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

        if ((userName = getEditTextValue(R.id.user_name_text)) == null) {
            alertError("오류", "이름을 입력해주세요.");
            return;
        }
        if ((age = getEditTextValue(R.id.age_text)) == null) {
            alertError("오류", "나이를 입력해주세요.");
            return;
        }
        if ((height = getEditTextValue(R.id.height_text)) == null) {
            alertError("오류", "키를 입력해주세요.");
            return;
        }
        if ((weight = getEditTextValue(R.id.weight_text)) == null) {
            alertError("오류", "몸무게를 입력해주세요.");
            return;
        }
        if ((bp_min = getEditTextValue(R.id.bp_min)) == null) {
            alertError("오류", "혈압 수치를 입력해주세요.");
            return;
        }
        if ((bp_max = getEditTextValue(R.id.bp_max)) == null) {
            alertError("오류", "혈압 수치를 입력해주세요.");
            return;
        }
        Spinner spinner = (Spinner)findViewById(R.id.gender_spinner);
        gender = spinner.getSelectedItem().toString();

        progressDialog.show(); // 로딩 화면을 띄워줌
        Thread thread = new Thread(null, loadData);
        thread.start();
    }

    public String getEditTextValue(int viewId) {
        EditText editText = (EditText)findViewById(viewId);
        if (editText.getText().length() != 0) {
            return editText.getText().toString();
        } else {
            return null;
        }
    }

    /* DataHandler에서 데이터 처리를 해서 결과 값 가져오는 메소드 */
    private final Runnable loadData = new Runnable() {
        @Override
        public void run() {
            try {
                resultData = dataHandler.getTestData(fileUri, resolver);
                handler.post(getResult);
            } catch (Exception e) {
                e.printStackTrace();
                alertError("오류", "오류가 발생했습니다.");
                String resultText = "";
                textView.setText(resultText);
                isFileLoaded = false;
            }
        }
    };

    /* 가져온 결과 값을 텍스트로 만드는 메소드 */
    private final Runnable getResult = new Runnable() {
        @Override
        public void run() {
            // 스레드 생성할 때 dialog를 show 했으므로 dismiss 해서 로딩 창 없애줌
            progressDialog.dismiss();
            String resultText;
            try {
                if (resultData != null) {
                    resultText = "당신의 유전자 정보\n"
                            + "P.VALUE 최소: " + resultData.getMinPvalData().getSNP() + " " + resultData.getMinPvalData().getPval() + "\n"
                            + "P.VALUE 최대: " + resultData.getMaxPvalData().getSNP() + " " + resultData.getMaxPvalData().getPval() + "\n"
                            + "geno 0 개수: " + resultData.getCount0() + "\n"
                            + "geno 1 개수: " + resultData.getCount1() + "\n"
                            + "geno 2 개수: " + resultData.getCount2() + "\n";
                } else {
                    resultText = "오류가 발생했습니다.";
                }
                Intent intent = new Intent(SecondActivity.this, ResultActivity.class);
                intent.putExtra("result", resultText);
                intent.putExtra("userName", userName);
                intent.putExtra("age", age);
                intent.putExtra("height", height);
                intent.putExtra("weight", weight);
                intent.putExtra("gender", gender);
                intent.putExtra("bp_min", bp_min);
                intent.putExtra("bp_max", bp_max);
                startActivity(intent);

                textView.setText("");
                isFileLoaded = false;
            } catch (NullPointerException e) {
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

}
