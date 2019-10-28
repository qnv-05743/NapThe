package com.scan.napthe.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.scan.napthe.R;


public class GuideActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.txt_help);
        textView.setPadding(0,10,0,0);

    }

//    public void onBackPressed() {
////        Intent myIntent = new Intent(GuideActivity.this, MainActivity.class);
////        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// clear back stack
////        startActivity(myIntent);
////        finish();
////        return;
//    }

}
