package com.scan.napthe.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.flags.impl.DataUtils;
import com.google.zxing.common.StringUtils;
import com.scan.napthe.R;
import com.scan.napthe.ultils.Constants;

import static com.scan.napthe.ultils.Constants.*;
import static com.scan.napthe.ultils.Constants.DIAL_HASHTAG;
import static com.scan.napthe.ultils.Constants.DIAL_SERFIX;

public class CardActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText textView;
    private TextView editText1, editText2;
    private Button button;
    private static final int RC_HANDLE_CALL_PERM = 3;
    private int REQUEST_PERMISSION_SETTING = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.code);
        editText1 = findViewById(R.id.firs);
        editText2 = findViewById(R.id.second);
        button = (Button) findViewById(R.id.btn_submit);
        String s = getIntent().getExtras().getString(TEXT_CODE);
        textView.setText(s);
        editText1.setText(DIAL_SERFIX);
        editText2.setText(DIAL_HASHTAG);
        final TextView first = (TextView) findViewById(R.id.txt_slide);
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
            }
        });
        animator.start();
    }

    public void Call(View view) {
        String s = textView.getText().toString();
        editText1.setText(DIAL_SERFIX);
        editText2.setText(DIAL_HASHTAG);
        if (s.equals("")) {
            textView.setError("Không được để trống!");
        } else if (s.length() > 12) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.message)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", editText1.getText().toString() + textView.getText().toString() + editText2.getText().toString(), null));
                            int check = ActivityCompat.checkSelfPermission(CardActivity.this, Manifest.permission.CALL_PHONE);
                            if (check != PackageManager.PERMISSION_GRANTED) {
                                requestCallPermission();
                            } else if (check == PackageManager.PERMISSION_GRANTED) {
                                startActivity(intent);
                            }

                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

        } else {
            textView.setError("Mã thẻ phải lớn hơn 12 số!");

        }
    }

    private void showMessageOKCancel(int message, DialogInterface.OnClickListener okListener) {
        new androidx.appcompat.app.AlertDialog.Builder(CardActivity.this)
                .setTitle(R.string.noti)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void requestCallPermission() {
        final String[] permissions = new String[]{Manifest.permission.CALL_PHONE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CALL_PERM);
            return;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {

            showMessageOKCancel(R.string.notifica,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onPause();
                        }
                    });
        }
    }

    public void onBackPressed() {
        Intent myIntent = new Intent(CardActivity.this, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// clear back stack
        startActivity(myIntent);
        finish();
        return;
    }

    public void refresh(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return;
    }


    public void Copy(View view) {
        setClipboard();
        Toast.makeText(this, R.string.copy, Toast.LENGTH_SHORT).show();
    }

    private void setClipboard() {
        String s = textView.getText().toString();
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copy Text", s);
        clipboard.setPrimaryClip(clip);
    }

    public void Guide(View view) {
        Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
        startActivity(intent);
    }

    public void Share(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = R.string.share + "" + "\n" +
                "https://play.google.com";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Quét mã thẻ điện thoại"));
    }
}
