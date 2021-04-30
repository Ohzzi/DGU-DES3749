package com.example.firstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    TextHandler textHandler;
    HashMap<String, ReferenceData> referenceDataHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textHandler = new TextHandler(MainActivity.this);
        loadReferenceData(R.raw.itrc_snp_hypertension_sm);
    }

    public void onTextLoadButtonClicked(View view) {
        int resId = R.raw.sample;
        String text = textHandler.readTextResource(resId);
        //String fileName = "sample.txt";
        //String text = textHandler.readTextFileByAssets(fileName);
        textView.setText(text);
    }

    public void loadReferenceData(int resId) {
        try {
            InputStream is = this.getResources().openRawResource(resId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] strArray = line.split(",");
                referenceDataHashMap.put(strArray[0], new ReferenceData(strArray));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}