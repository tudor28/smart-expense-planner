package com.example.conversieimaginetext;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import static android.Manifest.permission.CAMERA;

public class MainPageActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private static final String TAG = "aici";

    private TextView textView;
    private SurfaceView surfaceView;

    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;

    private TextToSpeech textToSpeech;
    private String stringResult = null;

    private RadioGroup radioGroup;

    private Button mLogout, mSave;

    private String recipientList = "tudor.alin97@yahoo.com, tudortudor2802@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainPageActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } catch (Exception e) {
                    Toast.makeText(MainPageActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSave = findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraSource.release();
//    }

    private void textRecognizer(){
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i<sparseArray.size(); ++i){
                    TextBlock textBlock = sparseArray.valueAt(i);
                    if (textBlock != null && textBlock.getValue() !=null){
                        stringBuilder.append(textBlock.getValue() + " ");
                    }
                }

                final String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stringResult = stringText;
                    }
                });
            }
        });
    }


    public void buttonStart(View view) {
        setContentView(R.layout.surfaceview);
        textRecognizer();
    }

    public void takePhoto(View view) {
        String stringResult1 = "initial";
        String price = "";
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        char resultArray[] = stringResult.toCharArray();
        for (int i = 0; i < stringResult.length(); i++) {
            if(resultArray[i] == '.' && (resultArray[i-1] >= '0' && resultArray[i-1] <= '9') && (resultArray[i+1] >= '0' && resultArray[i+1] <= '9')) {
                int j = i;
                while (resultArray[j-1] >= '0' && resultArray[j-1] <= '9') {
                    price = price + resultArray[j-1];
                    j--;
                }
                price = new StringBuilder(price).reverse().toString();
                j = i;
                price = price + ".";
                while (resultArray[j+1] >= '0' && resultArray[j+1] <= '9') {
                    price = price + resultArray[j+1];
                    j++;
                }
                stringResult1 = "OK!";
            }
        }
        textView.setText(price);
        //textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null);
        Toast.makeText(MainPageActivity.this,
                "Imaginea a fost scanat?? cu succes!", Toast.LENGTH_SHORT).show();
    }

    public void writeToFile(View view) {
        try {
            radioGroup = findViewById(R.id.radioGroup);
            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton rb = findViewById(selectedId);
            String radioText = rb.getText().toString();

            Toast.makeText(MainPageActivity.this,
                    radioText, Toast.LENGTH_SHORT).show();
           // Log.v(TAG, stringResult);

           // Log.v(TAG, String.valueOf(radioText.contains("Format text")));
            if (radioText.contains("text")) {
               // Log.v(TAG, "text");
               readerToFormat("test");
            } else if (radioText.contains("pdf")) {
                //Log.v(TAG, "docx");
                savePdf("test");
            } else {
                Toast.makeText(MainPageActivity.this,
                        "Fi??ierul nu a fost creat.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void savePdf (String fileName) throws DocumentException, FileNotFoundException {

        if (stringResult == null) {
            Toast.makeText(MainPageActivity.this,
                    "Scana??i o imagine mai ??nt??i!", Toast.LENGTH_SHORT).show();
            return;
        }

        Document doc=new Document();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = fileName + timeStamp;
        String path = Environment.getExternalStorageDirectory() + "/My Files/" + fileName + ".pdf";
        Font smallBold=new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
        PdfWriter.getInstance(doc,new FileOutputStream(path));
        doc.open();
        doc.addAuthor("Tudor");
        doc.add(new Paragraph(stringResult, smallBold));
        Toast.makeText(MainPageActivity.this,
                "Fi??ierul .pdf a fost salvat cu succes!", Toast.LENGTH_SHORT).show();
        doc.close();
    }

    public void readerToFormat (String fileName) {

        try {
            if (stringResult == null) {
                Toast.makeText(MainPageActivity.this,
                        "Scana??i o imagine mai ??nt??i!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE);
                } else {
                    File path = Environment.getExternalStorageDirectory();
                    File dir = new File(path + "/My Files/");
                    dir.mkdirs();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    fileName = fileName + timeStamp + ".txt";
                    File file = new File(dir, fileName);
                    // Log.v(TAG, Environment.getExternalStorageDirectory().toString() + "/" + fileName + ".txt");

                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileWriter writer = new FileWriter(file);
                    writer.append(stringResult);
                    Toast.makeText(MainPageActivity.this,
                            "Fi??ierul .txt a fost salvat cu succes!", Toast.LENGTH_SHORT).show();
                    writer.flush();
                    writer.close();
                }
            }

        } catch (IOException e) {
            Toast.makeText(MainPageActivity.this,
                    "Fi??ierul nu a fost creat. Exception catched.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void sendMail (View view) {
        String[] recipients = recipientList.split(",");
        String subject = "Text citit din imagine";
        String message = stringResult;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an email client"));
    }

    public void goBack(View view) {
        //finish();
        setContentView(R.layout.activity_main);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
}