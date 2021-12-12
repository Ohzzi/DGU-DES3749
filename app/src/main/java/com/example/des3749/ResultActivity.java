package com.example.des3749;

import static android.view.View.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    TextView titleView;
    TextView resultView;
    TextView infoView;
    TextView subBar1;
    TextView subBar2;
    TextView subBar3;
    TextView subBar4;

    Intent intent;

    String userName;
    String age;
    String height;
    String weight;
    String bp_min;
    String bp_max;
    String gender;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_result);
        initialize();
        getInfo();
        getResult();
    }

    public void initialize() {
        intent = getIntent();
        titleView = findViewById(R.id.title_view);
        resultView = findViewById(R.id.result_view);
        infoView = findViewById(R.id.info_view);

        OnClickListener onSubBarClicked = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                changeView(id);
            }
        };

        subBar1 = findViewById(R.id.sub1);
        subBar2 = findViewById(R.id.sub2);
        subBar3 = findViewById(R.id.sub3);
        subBar4 = findViewById(R.id.sub4);

        subBar1.setOnClickListener(onSubBarClicked);
        subBar2.setOnClickListener(onSubBarClicked);
        subBar3.setOnClickListener(onSubBarClicked);
        subBar4.setOnClickListener(onSubBarClicked);
    }

    private void changeView(int id) {
        //TODO: 화면 전환 구현
    }

    public void getResult() {
        resultView.setText(intent.getExtras().getString("result"));
    }

    public void getInfo() {
        userName = intent.getExtras().getString("userName");
        age = intent.getExtras().getString("age");
        height = intent.getExtras().getString("height");
        weight = intent.getExtras().getString("weight");
        bp_min = intent.getExtras().getString("bp_min");
        bp_max = intent.getExtras().getString("bp_max");
        gender = intent.getExtras().getString("gender");
        String info = "당신의 정보\n" + age + "세 " + gender + "\n" + height + "cm " + weight + "kg \n" + "혈압 " + bp_min + " - " + bp_max;
        String title = getString(R.string.title, userName);
        titleView.setText(title);
        infoView.setText(info);
    }
}
