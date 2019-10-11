package com.scan.napthe.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scan.napthe.R;
import com.scan.napthe.ultils.Constants;

import static com.scan.napthe.ultils.Constants.DIAL_HASHTAG;

public class CardActivity extends AppCompatActivity {
    Toolbar toolbar;
    private EditText textView;
    private Button button;
    private static final int RC_HANDLE_CALL_PERM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.ma_so);
        button = (Button) findViewById(R.id.btn_napthe);
        String s = getIntent().getStringExtra(Constants.CODE_NUMBER);
        textView.setText(Constants.DIAL_SERFIX + s + Constants.DIAL_HASHTAG);

    }


    public void Call(View view) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", textView.getText().toString(), null));
        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (check != PackageManager.PERMISSION_GRANTED) {
            requestCallPermission();
        } else {
            startActivity(intent);
        }


    }

    private void requestCallPermission() {
        final String[] permissions = new String[]{Manifest.permission.CALL_PHONE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CALL_PERM);
            return;
        }
    }

    public void refresh(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void setClipboard() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", textView.getText());
        clipboard.setPrimaryClip(clip);
    }

    public void Copy(View view) {

        setClipboard();
        Toast.makeText(this, "Đã sao chép !", Toast.LENGTH_SHORT).show();
    }

    public void Guide(View view) {
        Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
        startActivity(intent);
    }

    public void Share(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Ứng dụng Quét mã thẻ nạp VIETTEL,MOBIFONE,VINAPHONE" + "" + "\n" +
                "https://play.google.com/store";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Quét mã thẻ điện thoại"));

    }
}
