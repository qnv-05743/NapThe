package com.scan.napthe.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.cymaybe.foucsurfaceview.FocusSurfaceView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.scan.napthe.BuildConfig;
import com.scan.napthe.R;
import com.scan.napthe.ultils.Constants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Permission;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 0;
    private Toolbar toolbar;
    private CameraSource mCameraSource;
    final int requestPermissionID = 100;
    private SurfaceView mCameraView;
    private SeekBar seekBar;
    private AlertDialog alertDialog;
    public Camera camera;
    boolean isFlash = false;
    private ImageView image_flash;
    public Camera.Parameters cameraParameter;
    private Vibrator v;
    private ToneGenerator toneGen;
    boolean doubleBackToExitPressedOnce = false;
    private int REQUEST_PERMISSION_SETTING = 0;
    private int REQUEST_CAMERA = 3;
    private boolean isScanning = true;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case requestPermissionID: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        return;
                    try {
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                } else {
                    mCameraView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int check = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                            if (check != PackageManager.PERMISSION_GRANTED) {
                                showMessageOKCancel(R.string.notification,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                onPause();
                                            }
                                        });
                            } else if (check == PackageManager.PERMISSION_GRANTED) {

                            }

                        }
                    });
                }

            }


            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(int message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCameraView = (SurfaceView) findViewById(R.id.surfaceView);

        setSupportActionBar(toolbar);
        seekBar = (SeekBar) findViewById(R.id.seekbar_controls);
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        image_flash = (ImageView) findViewById(R.id.image_flash);
        startCameraSource();

        image_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_flash.setSelected(!image_flash.isSelected());
                int check = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                if (check != PackageManager.PERMISSION_GRANTED) {
                    showMessageOKCancel(R.string.notification_flash,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // onPause();
                                }
                            });
                } else if (check == PackageManager.PERMISSION_GRANTED) {
                    Context context = v.getContext();
                    if (camera != null && context.getPackageManager().hasSystemFeature(getPackageManager().FEATURE_CAMERA_FLASH)) {
                        Camera.Parameters params = camera.getParameters();
                        if (isFlash) {
                            image_flash.setImageResource(R.drawable.flashofff);
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            camera.setParameters(params);
                            isFlash = false;
                        } else {
                            image_flash.setImageResource(R.drawable.flashon);
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(params);
                            isFlash = true;
                        }
                    }
                    initCamera();
                }

            }

        });
        // Event on change zoom with the bar.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("PRO", "onProgressChanged: " + progress);

                if (camera != null && camera.getParameters().isZoomSupported()) {
                    Camera.Parameters params = camera.getParameters();
                    params.setZoom(progress);
                    seekBar.setMax(params.getMaxZoom());
                    camera.setParameters(params);
                }
                initCamera();
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;


        }


        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.back, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }

        }, 2000);

    }


    private void startCameraSource() {
        //Create the TextRecognizerd
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            //  Log.w("a", "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1024, 1080)
                    .setRequestedFps(40.0f)
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
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                            ActivityCompat.
                                    requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);


                        } else {
                            mCameraSource.start(mCameraView.getHolder());
                        }

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    initCamera();

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                /**
                 * Release resources for cameraSource
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    seekBar.setProgress(0);
                    mCameraSource.stop();
                }
            });


            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                    mCameraSource.stop();
                }


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
                                    s.split(NUMBER);
                                } else if (s.equals(detectCode(s)) && s.length() < 14) {
                                    if (isScanning) {
                                        isScanning = false;
                                        gotoCardDetail(s);
                                        Log.d("TAG", "run: " + s);

                                    }

                                }
                            }
                        });
                    }

                }
            });
        }
    }

    private void gotoCardDetail(String cardNumber) {
        notification();
        vibrator();
        Intent intent = new Intent(MainActivity.this, CardActivity.class);
        intent.putExtra(Constants.TEXT_CODE, cardNumber);
        startActivity(intent);
    }


    public void notification() {
        SharedPreferences pre = getSharedPreferences(Constants.KEY, MODE_PRIVATE);
        boolean isNoti = pre.getBoolean(Constants.KEY_BEEP, true);
        if (isNoti) {
            toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
        }
    }


    public void vibrator() {
        SharedPreferences pre = getSharedPreferences(Constants.KEY, MODE_PRIVATE);
        boolean isVira = pre.getBoolean(Constants.KEY_VIBRA, true);
        if (isVira) {
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            }
        }

    }


    String NUMBER = "0123456789";

    public String detectCode(String input) {

        Log.d("///////// ", "in" + input);
        String code = "";
        for (int i = 0; i < input.length(); i++) {
            String currentString = String.valueOf(input.charAt(i));
            Log.d("///////// ", "currentString " + currentString);
            if (NUMBER.contains(currentString)) {
                code += currentString;
            } else if (currentString.equals(" ") || currentString.equals("-")) {
                continue;
            } else if (currentString.length() == 12) {
//
            } else if (currentString.length() == 14) {

            } else if (code.length() <= 12) {
                code = "";
            } else {
                break;
            }
        }
        //
        Log.d("///////// ", "out " + code);
        return code;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_option, menu);
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
                Toast.makeText(this, R.string.info, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void displayAlertDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.setting);
        LayoutInflater inflater = getLayoutInflater();
        final View alertLayout = inflater.inflate(R.layout.option_dialog, null);
        final CheckBox cb_beep = (CheckBox) alertLayout.findViewById(R.id.cb_beep);
        final CheckBox cb_rung = (CheckBox) alertLayout.findViewById(R.id.cb_rung);
        final Button button = (Button) alertLayout.findViewById(R.id.btn_close);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();
        SharedPreferences pre = getSharedPreferences(Constants.KEY, MODE_PRIVATE);
        boolean isVira = pre.getBoolean(Constants.KEY_VIBRA, true);
        boolean isNoti = pre.getBoolean(Constants.KEY_BEEP, true);

        cb_beep.setChecked(isNoti);
        cb_rung.setChecked(isVira);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });


        cb_beep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY, Context.MODE_PRIVATE);
                Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.KEY_BEEP, cb_beep.isChecked());
                if (cb_beep.isChecked()) {
                    editor.putInt(Constants.KEY_BEEP_VALUE, 0);
                } else {
                    editor.putInt(Constants.KEY_BEEP_VALUE, 1);
                }
                editor.commit();


            }
        });

        cb_rung.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.KEY, Context.MODE_PRIVATE);
                Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.KEY_VIBRA, cb_rung.isChecked());
                if (cb_rung.isChecked()) {
                    editor.putInt(Constants.KEY_VIBRA_VALUE, 0);
                } else {
                    editor.putInt(Constants.KEY_VIBRA_VALUE, 1);
                }
                editor.commit();
            }
        });

    }


    private void initCamera() {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    camera = (Camera) field.get(mCameraSource);
                    if (camera != null) {
                        Camera.Parameters params = camera.getParameters();
                        //  camera.setParameters(params);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public void toggleFlash(View v) {


    }

    public void share(View view) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ứng dụng quét mã thẻ cào tự động");
            String shareMessage= "\nHãy để tôi giới thiệu cho bạn ứng dụng này\n\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    public void Guide(View view) {
        Intent intent = new Intent(getApplicationContext(), GuideActivity.class);
        startActivity(intent);
    }

}
