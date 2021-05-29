package com.example.conversieimaginetext.categoriesAlarms;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.conversieimaginetext.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseCategoriesAlarmActivity extends AppCompatActivity {

    private static final String TAG = "aici";
    private Spinner mCategorySpinner, mPeriodSpinner;
    private Button mBack, mCreateAlarm;
    private EditText mTreshold;
    DatabaseReference mCategoriesReference, mAlarmsReference;
    String selectedCategory, tresholdText;
    boolean canSave = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_alarm);

        mCategorySpinner = findViewById(R.id.categorySelect);

        mCategoriesReference = FirebaseDatabase.getInstance().getReference().child("Categorii");
        mCategoriesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<String> categories = new ArrayList<>();
                categories.add("Selectați categoria..");

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String category = ds.getKey();
                    categories.add(category);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ExpenseCategoriesAlarmActivity.this, R.layout.spinner_layout, categories);
                arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
                mCategorySpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mPeriodSpinner = findViewById(R.id.periodSelect);
        List<String> periods = new ArrayList<>();
        periods.add("Selectați perioada..");
        periods.add("Săptămânală");
        periods.add("Lunară");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ExpenseCategoriesAlarmActivity.this, R.layout.spinner_layout, periods);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        mPeriodSpinner.setAdapter(arrayAdapter);

        mCreateAlarm = findViewById(R.id.createAlarm);
        mCreateAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = String.valueOf(mCategorySpinner.getSelectedItem());
                mTreshold = findViewById(R.id.tresholdText);

                if (selectedCategory.equals("Selectați categoria..")) {
                    Toast.makeText(ExpenseCategoriesAlarmActivity.this,
                            "Selectați o categorie!", Toast.LENGTH_SHORT).show();
                } else {
                    tresholdText = mTreshold.getText().toString();
                    if (tresholdText.equals("")) {
                        Toast.makeText(ExpenseCategoriesAlarmActivity.this,
                                "Introduceți un prag!", Toast.LENGTH_SHORT).show();
                    } else if (Float.parseFloat(tresholdText) <= 0) {
                        Toast.makeText(ExpenseCategoriesAlarmActivity.this,
                                "Introduceți un prag mai mare decât 0!", Toast.LENGTH_SHORT).show();
                    } else if (String.valueOf(mPeriodSpinner.getSelectedItem()).equals("Selectați perioada..")) {
                        Toast.makeText(ExpenseCategoriesAlarmActivity.this,
                                "Alegeți perioada pentru alarmă!", Toast.LENGTH_SHORT).show();
                    } else {
                        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                        final String userId = mUser.getUid();
                        mAlarmsReference = FirebaseDatabase.getInstance().getReference().child("Alarme").child(userId);
                        mAlarmsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    if (ds.getKey().equals(selectedCategory) && ds.child("Activă").getValue().equals("da")) {
                                        String dateCreated = ds.child("Data").getValue().toString();
                                        String period = ds.child("Perioada").getValue().toString();
                                        String toastText = "";
                                        int daysNo = 31;
                                        if (period.equals("Săptămânală")) {
                                            toastText = "săptâmănală";
                                            daysNo = 7;
                                        }
                                        else if (period.equals("Lunară")) {
                                            toastText = "lunară";
                                        }

                                        final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                        long daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(dateCreated, formatter), LocalDate.parse(currentDate, formatter));
                                        daysBetween = daysNo - daysBetween;
                                        Toast.makeText(ExpenseCategoriesAlarmActivity.this,
                                                "Aveți o alarmă " + toastText + " activă pentru această categorie, care expiră în " + daysBetween + " zile!", Toast.LENGTH_SHORT).show();
                                        canSave = false;
                                    }
                                }
                                if (canSave) {
                                    mAlarmsReference = FirebaseDatabase.getInstance().getReference().child("Alarme").child(userId).child(selectedCategory);
                                    mAlarmsReference.child("Prag").setValue(tresholdText);
                                    mAlarmsReference.child("Perioada").setValue(mPeriodSpinner.getSelectedItem());
                                    mAlarmsReference.child("Data").setValue(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
                                    mAlarmsReference.child("Ora").setValue(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                                    mAlarmsReference.child("Suma actuală").setValue(0);
                                    mAlarmsReference.child("Activă").setValue("da");

                                    Toast.makeText(ExpenseCategoriesAlarmActivity.this,
                                            "Noua alarmă a fost adăugată cu succes!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesAlarmActivity.this, ExpenseCategoriesAlarmMainActivity.class);
                startActivity(intent);
            }
        });
    }
}
