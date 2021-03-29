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

public class MainActivity extends AppCompatActivity {

    TextView textView;
    TextHandler textHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textHandler = new TextHandler(MainActivity.this);
    }

    public void onTextLoadButtonClicked(View view) {
        int resId = R.raw.sample;
        String text = textHandler.readTextResource(resId);
        //String fileName = "sample.txt";
        //String text = textHandler.readTextFileByAssets(fileName);
        textView.setText(text);
    }

}