package com.example.conversieimaginetext.expenseReport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.conversieimaginetext.R;
import com.example.conversieimaginetext.categoriesAlarms.ExpenseCategoriesAlarmActivity;
import com.example.conversieimaginetext.categoriesAlarms.ExpenseCategoriesAlarmMainActivity;
import com.example.conversieimaginetext.expenseCategories.ExpenseCategoriesDeleteActivity;
import com.example.conversieimaginetext.receiptScan.MainPageActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpensesReportActivity extends AppCompatActivity {

    private Button mBack, mReport;
    private Spinner mCategorySpinner, mPeriodSpinner;
    DatabaseReference mCategoriesReference;
    String selectedCategory, selectedInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_report);

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
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ExpensesReportActivity.this, R.layout.spinner_layout, categories);
                arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
                mCategorySpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mPeriodSpinner = findViewById(R.id.reportPeriod);
        List<String> periods = new ArrayList<>();
        periods.add("Selectați intervalul..");
        periods.add("Lunar");
        periods.add("Anual");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ExpensesReportActivity.this, R.layout.spinner_layout, periods);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        mPeriodSpinner.setAdapter(arrayAdapter);

        mReport = findViewById(R.id.generateReport);
        mReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = String.valueOf(mCategorySpinner.getSelectedItem());
                selectedInterval = String.valueOf(mPeriodSpinner.getSelectedItem());
                if (selectedCategory.equals("Selectați categoria..")) {
                    Toast.makeText(ExpensesReportActivity.this,
                            "Selectați o categorie!", Toast.LENGTH_SHORT).show();
                } else if (selectedInterval.equals("Selectați intervalul..")) {
                    Toast.makeText(ExpensesReportActivity.this,
                            "Selectați intervalul pentru raport!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(ExpensesReportActivity.this, ExpensesReportDisplayActivity.class);
                    intent.putExtra("CATEG", selectedCategory);
                    intent.putExtra("INTERV", selectedInterval);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesReportActivity.this, MainPageActivity.class);
                startActivity(intent);
            }
        });
    }
}
