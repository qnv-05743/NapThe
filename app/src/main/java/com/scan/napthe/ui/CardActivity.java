package com.scan.napthe.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.scan.napthe.R;
import com.scan.napthe.ultils.Constants;

import static com.scan.napthe.ultils.Constants.DIAL_HASHTAG;

public class CardActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText textView;
    private Button button;
    private static final int RC_HANDLE_CALL_PERM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.code);
        button = (Button) findViewById(R.id.btn_submit);
        String s = getIntent().getStringExtra(Constants.TEXT_CODE);
        textView.setText(Constants.DIAL_SERFIX + s + Constants.DIAL_HASHTAG);
        final TextView first = (TextView) findViewById(R.id.txt_slide);
       // final TextView second = (TextView) findViewById(R.id.txt_second);
        final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 0.5f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(8000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                final float width = first.getWidth();
                final float translationX = width * progress;
                first.setTranslationX(translationX);
              //  first.setTranslationX(translationX - width);
            }
        });
        animator.start();
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

    public void onBackPressed() {
        Intent myIntent = new Intent(CardActivity.this, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// clear back stack
        startActivity(myIntent);
        finish();
        return;
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
        String s = getIntent().getStringExtra(Constants.TEXT_CODE);
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copy Text", s);
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
        String shareBody = R.string.share + "" + "\n" +
                "https://play.google.com/store";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Quét mã thẻ điện thoại"));

    }
}
