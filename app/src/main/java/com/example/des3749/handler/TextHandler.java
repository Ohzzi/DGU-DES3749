package com.example.des3749.handler;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextHandler {

    private final Context context;

    public TextHandler(Context context) {
        this.context = context;
    }

    public String readTextResource(int id) {
        StringBuilder strBuilder = new StringBuilder();
        try {
            InputStream is = context.getResources().openRawResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line=reader.readLine())!=null) {
                strBuilder.append(line).append("\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return strBuilder.toString();
    }

    public String readTextFileByAssets(String fileName) {
        StringBuilder strBuilder = new StringBuilder();
        try {
            AssetManager as = context.getAssets();
            InputStream is = as.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line=reader.readLine())!=null) {
                strBuilder.append(line).append("\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return strBuilder.toString();
    }
}
