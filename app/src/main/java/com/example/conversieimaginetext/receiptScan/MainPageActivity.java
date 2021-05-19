package com.example.conversieimaginetext.receiptScan;

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

import com.example.conversieimaginetext.MainActivity;
import com.example.conversieimaginetext.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;

public class MainPageActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private static final String TAG = "aici";
    private static final String TEXT = "testText";


    private TextView priceText, locationText;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;
    private TextToSpeech textToSpeech;
    private String stringResult = null;
    private RadioGroup radioGroup;
    private Button mLogout, mSave;
    private String recipientList = "tudor.alin97@yahoo.com, tudortudor2802@gmail.com";
    private String receiptTotal = "nedetectat";
    private String detectedLocation = "nedetectat";

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

//        mSave = findViewById(R.id.save);
//        mSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraSource.release();
//    }

    private void textRecognizer() {
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
                //final String regExp = "^[0-9]+([,.][0-9]{1,2})?";
                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                // first or second block should indicate the location
                if ((sparseArray.valueAt(0).getValue().matches("[\\s\\S]*S[.]*C[.]*[\\s\\S]*")) ||
                        (sparseArray.valueAt(0).getValue().matches("[\\s\\S]*S[.]*R[.]*L[.]*[\\s\\S]*")) ||
                        (sparseArray.valueAt(0).getValue().matches("[\\s\\S]*S[.]*A[.]*[\\s\\S]*"))) {
                    detectedLocation = sparseArray.valueAt(0).getValue();
                } else if ((sparseArray.valueAt(1).getValue().matches("[\\s\\S]*S[.]*C[.]*[\\s\\S]*")) ||
                        (sparseArray.valueAt(1).getValue().matches("[\\s\\S]*S[.]*R[.]*L[.]*[\\s\\S]*")) ||
                        (sparseArray.valueAt(1).getValue().matches("[\\s\\S]*S[.]*A[.]*[\\s\\S]*"))) {
                    detectedLocation = sparseArray.valueAt(1).getValue();
                }

                for (int i = 0; i<sparseArray.size(); ++i) {
                    TextBlock textBlock = sparseArray.valueAt(i);
                    //String[] sumKeywords = {"total", "suma", "suma totala", "pret", "cash"};
//                    Log.v(TEXT, textBlock.getValue().toLowerCase());
//                    if (textBlock.getValue().toLowerCase().matches("^.*penny*.|.*kaufland*.$")) {
//                        Log.v(TAG, "detected location: " + detectedLocation);
//                        detectedLocation = textBlock.getValue().toLowerCase();
//                    }
                    if (textBlock.getValue().toLowerCase().matches("^.*total*.|.*suma*.|.*subtotal*.$")) {
                        if (sparseArray.valueAt(i + 1).getValue().matches("[+-]?(?=\\d*[.eE])(?=\\.?\\d)\\d*\\.?\\d*(?:[eE][+-]?\\d+)?")) {
                            receiptTotal = sparseArray.valueAt(i + 1).getValue();
                        }
                       // final Pattern pattern = Pattern.compile(regExp);
                       // Matcher matcher = pattern.matcher(receiptTotal);
//                        if (!matcher.matches()) {
//                            receiptTotal = "nedetectat";
//                        }
                    }
                    Log.v(TAG, "position " + i + "text: " + textBlock.getValue());
                    //Log.v(TAG, textBlock.getValue());
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
        setContentView(R.layout.activity_receipt_scan_save);

        priceText = findViewById(R.id.scannedAmount);
        locationText = findViewById(R.id.detectedShop);

        float maxPrice = 0;
        float secondMax = 0;
        //Log.v(TAG, "result: " + stringResult);

        String[] resultArray = stringResult.split("\\s+");
        for(int i = 0; i < resultArray.length; i++) {
            //Log.v(TAG, "resultArray["+i+"]: " + resultArray[i]);
            // detect shop, hardcoded for now
//            if (resultArray[i].toLowerCase().matches(".*penny.*|.*kaufland*.")) {
//                detectedLocation = resultArray[i];
//            }

            // if the price was not detected on the surface view
            if (receiptTotal.equals("nedetectat")) {

                // replace , with .
                char[] resultChar = resultArray[i].toCharArray();
                for (int j = 0; j < resultChar.length; j++) {
                    if (resultChar[j] == ',') resultChar[j] = '.';
                }
                resultArray[i] = String.valueOf(resultChar);

                boolean isFloat = resultArray[i].matches("[+-]?(?=\\d*[.eE])(?=\\.?\\d)\\d*\\.?\\d*(?:[eE][+-]?\\d+)?");

                if (isFloat) {
                    float floatResult = Float.parseFloat(resultArray[i]);
                    if (floatResult >= maxPrice) {
                        secondMax = maxPrice;
                        maxPrice = floatResult;
                    }
                }
            }
        }
        if ((maxPrice > 0) & (receiptTotal.equals("nedetectat"))) {
            String secondMaxStr = String.format("%.02f", secondMax);
            receiptTotal = secondMaxStr;
        }
        //Intent intent = new Intent(this, ReceiptScanSaveActivity.class);
        //startActivity(intent);

        priceText.setText(receiptTotal);
        locationText.setText(detectedLocation);

        //textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null);
//        Toast.makeText(MainPageActivity.this,
//                "Bonul a fost scanat cu succes!", Toast.LENGTH_SHORT).show();
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
                        "Fișierul nu a fost creat.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void savePdf (String fileName) throws DocumentException, FileNotFoundException {

        if (stringResult == null) {
            Toast.makeText(MainPageActivity.this,
                    "Scanați o imagine mai întâi!", Toast.LENGTH_SHORT).show();
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
                "Fișierul .pdf a fost salvat cu succes!", Toast.LENGTH_SHORT).show();
        doc.close();
    }

    public void readerToFormat (String fileName) {

        try {
            if (stringResult == null) {
                Toast.makeText(MainPageActivity.this,
                        "Scanați o imagine mai întâi!", Toast.LENGTH_SHORT).show();
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
                            "Fișierul .txt a fost salvat cu succes!", Toast.LENGTH_SHORT).show();
                    writer.flush();
                    writer.close();
                }
            }

        } catch (IOException e) {
            Toast.makeText(MainPageActivity.this,
                    "Fișierul nu a fost creat. Exception catched.", Toast.LENGTH_SHORT).show();
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