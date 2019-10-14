package com.scan.napthe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.scan.napthe.R;
import com.scan.napthe.ultils.Constants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

//implements ZXingScannerView.ResultHandler
public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    CameraSource mCameraSource;
    final int requestPermissionID = 1001;
    private SurfaceView mCameraView;
    private SeekBar seekBar;
    AlertDialog alertDialog;
    Camera camera;
    boolean isFlash = false;
    

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case requestPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCameraView = (SurfaceView) findViewById(R.id.surfaceView);

        seekBar = findViewById(R.id.seekbar_controls);
        setSupportActionBar(toolbar);
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        //  contentFrame.addView(mScannerView);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("Error", "progress:"+progress);

                // YOur code here in set zoom for pinch zooming, sth like this

                if(camera.getParameters().isZoomSupported()){
                   Camera.Parameters params = camera.getParameters();
                    params.setZoom(progress);
                    camera.setParameters(params);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startCameraSource();
    }

    private void startCameraSource() {
        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w("a", "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1080, 720)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {


                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                /**
                 * Release resources for cameraSource
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {

                        mCameraView.post(new Runnable() {
                            @Override
                            public void run() {

                                StringBuilder stringBuilder = new StringBuilder();

                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                String s = detectCode(stringBuilder.toString());
                                if (s.isEmpty()) {
                                    onPause();
                                } else if (s.length() <= 13){
                                    vibrator();
                                    notifycation();
                                    Intent intent = new Intent(MainActivity.this, CardActivity.class);
                                    intent.putExtra(Constants.CODE_NUMBER, s);
                                    Log.d("TAG", "run: " + s);
                                    startActivity(intent);
                                }

                            }

                        });
                    }
                }
            });
        }
    }

    private void notifycation() {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
    }

    public void vibrator() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));

        }
    }

    String NUMBER = "0123456789";

    public String detectCode(String input) {

        Log.d("///////// ", "in " + input);
        String code = "";
        for (int i = 0; i < input.length(); i++) {
            String currentString = String.valueOf(input.charAt(i));
            Log.d("///////// ", "currentString " + currentString);
            if (NUMBER.contains(currentString)) {
                code += currentString;
            } else if (currentString.equals(" ") || currentString.equals("-")) {
                continue;
            } else if (code.length() < 12) {
                code = "";
            } else {
                break;
            }
        }
        Log.d("///////// ", "out " + code);
        return code;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                displayAlertDialog();
                break;
            case R.id.menu2:
                String url1 = "https://play.google.com/store?hl=en";
                Intent ii = new Intent(Intent.ACTION_VIEW);
                ii.setData(Uri.parse(url1));
                startActivity(ii);
                break;
            case R.id.menu3:
                String url = "http://www.thongtincongty.com/company/3f88c631-cong-ty-co-phan-mgosu-viet-nam/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.menu4:
                Toast.makeText(this, "PHIÊN BẢN 1.0.0", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayAlertDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("TÙY CHỌN RUNG VÀ TIẾNG BEEP");

        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.option_dialog, null);
        final CheckBox checkbeep = (CheckBox) alertLayout.findViewById(R.id.cb_beep);
        final CheckBox checkrung = (CheckBox) alertLayout.findViewById(R.id.cb_rung);
        final Button button = (Button) alertLayout.findViewById(R.id.btn_close);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        checkbeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        checkrung.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

    }


    public void toggleFlash(View v) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    camera = (Camera) field.get(mCameraSource);
                    if (camera != null) {
                        Camera.Parameters params = camera.getParameters();
                        if (!isFlash) {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            ///  flashImage.setColorFilter(getResources().getColor(R.color.yellow));
                            isFlash = true;
                        } else {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            // flashImage.setColorFilter(getResources().getColor(R.color.greyLight));
                            isFlash = false;
                        }
                        camera.setParameters(params);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public void share(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Ứng dụng Quét mã thẻ nạp VIETTEL,MOBIFONE,VINAPHONE" + "" + "\n" + Uri.parse("https://play.google.com/store");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Quét mã thẻ điện thoại"));
    }

    public void Guide(View view) {
        Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
        startActivity(intent);
    }
}
