package com.example.conversieimaginetext.receiptScan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.conversieimaginetext.R;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class ReceiptScanSaveActivity extends AppCompatActivity {

    private static final String TAG = "aici";
    private SurfaceView surfaceView;
    private CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_scan_save);

    }

    public void goBack(View view) {
        //finish();
        setContentView(R.layout.activity_main);
    }

}
