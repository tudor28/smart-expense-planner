package com.example.conversieimaginetext.expenseReport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.conversieimaginetext.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ExpensesReportDisplayActivity extends AppCompatActivity {

    private static final String TAG = "aici";
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private String recipientList = "tudor.alin97@yahoo.com";
    private TextView categoryText, totalText;
    private Button mBack;
    private ListView expenseReport;
    ArrayList<String> expenses = new ArrayList<>();
    DatabaseReference mExpensesReference, mExpenseReference;
    String interval = "", searchedCateg = "", emailMessage = "Raport lunar de cheltuieli", fileText = "Raport lunar de cheltuieli";
    float total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_report_display);

        if (getIntent().hasExtra("INTEV")) {
            interval = getIntent().getStringExtra("INTERV");
        }

        if (getIntent().hasExtra("CATEG")) {
            categoryText = findViewById(R.id.categoryReport);
            categoryText.setText(getIntent().getStringExtra("CATEG"));
            searchedCateg = getIntent().getStringExtra("CATEG");
            fileText = fileText + " pentru categoria " + searchedCateg + " \n\n";
            emailMessage = emailMessage + " pentru categoria " + searchedCateg + " \n\n";
        }

        expenseReport = findViewById(R.id.expenseReport);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.listview, expenses);
        expenseReport.setAdapter(arrayAdapter);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = mUser.getUid();
        mExpensesReference = FirebaseDatabase.getInstance().getReference().child("Bonuri").child(userId);
        mExpensesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        if (getMonth(ds.getKey()) == currentMonth) {
                            final String deit = ds.getKey();
                            mExpenseReference = FirebaseDatabase.getInstance().getReference().child("Bonuri").child(userId).child(deit);
                            mExpenseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds1 : snapshot.getChildren()) {
                                        String category = ds1.child("Categorie").getValue().toString();
                                        String price = ds1.child("Preț").getValue().toString();
                                        String location = ds1.child("Locație").getValue().toString();
                                        if (category.equals(searchedCateg)) {
                                            expenses.add(price + " RON în locația " + location + ", cheltuială înregistrată în data de " + deit);
                                            emailMessage = emailMessage + price + " RON în locația " + location + ", cheltuială înregistrată în data de " + deit + "\n";
                                            fileText = fileText + price + " RON în locatia " + location + ", cheltuiala inregistrata in data de " + deit + "\n";
                                            total += Float.parseFloat(price);
                                            arrayAdapter.notifyDataSetChanged();
                                            totalText = findViewById(R.id.total);
                                            totalText.setText("Total pentru luna curentă: " + total + " RON");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    } catch (ParseException e) {
                        Log.v(TAG, "ERROR!" + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesReportDisplayActivity.this, ExpensesReportActivity.class);
                startActivity(intent);
            }
        });
    }

    public int getMonth(String dateText) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date myday = sdf.parse(dateText);
        System.out.println(myday.getMonth()+1);
        return myday.getMonth() + 1;
    }

    public int getYear(String dateText) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date myday = sdf.parse(dateText);
        return myday.getYear();
    }

    public void sendMail (View view) {
        String[] recipients = recipientList.split(",");
        String subject = "Raport lunar de cheltuieli";
        emailMessage = emailMessage + "\nTOTAL: " + total;
        String message = emailMessage;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an email client"));
    }

    public void writeToFile (View view) {

        try {
            fileText = fileText + "\nTOTAL: " + total;
            savePdf("Raport_");
        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(ExpensesReportDisplayActivity.this,
                "Fișierul nu a fost creat.", Toast.LENGTH_SHORT).show();
        }

    }

    public void savePdf (String fileName) throws DocumentException, FileNotFoundException {

        if (fileText.equals("")) {
            Toast.makeText(ExpensesReportDisplayActivity.this,
                    "Nu aveți cheltuieli de salvat!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE);
        }

        Document doc = new Document();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = fileName + timeStamp;
        String path = Environment.getExternalStorageDirectory() + "/My Files/" + fileName + ".pdf";
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
        PdfWriter.getInstance(doc, new FileOutputStream(path));
        doc.open();
        doc.addAuthor("Tudor");
        doc.add(new Paragraph(fileText, smallBold));
        Toast.makeText(ExpensesReportDisplayActivity.this,
                "Raportul a fost salvat cu succes!", Toast.LENGTH_SHORT).show();
        doc.close();
    }

}
