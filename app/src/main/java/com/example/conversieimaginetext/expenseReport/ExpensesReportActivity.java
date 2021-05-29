package com.example.conversieimaginetext.expenseReport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.conversieimaginetext.R;
import com.example.conversieimaginetext.receiptScan.MainPageActivity;

public class ExpensesReportActivity extends AppCompatActivity {

    private Button mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_report);

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
