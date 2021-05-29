package com.example.conversieimaginetext.categoriesAlarms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.conversieimaginetext.R;
import com.example.conversieimaginetext.receiptScan.MainPageActivity;

public class ExpenseCategoriesAlarmMainActivity extends AppCompatActivity {

    private Button mListAlarms, mCreateAlarms, mDeleteAlarms, mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_alarm_main);

        mListAlarms = findViewById(R.id.listAlarm);
        mListAlarms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesAlarmMainActivity.this, ExpenseCategoriesAlarmListActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mCreateAlarms = findViewById(R.id.setAlarm);
        mCreateAlarms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesAlarmMainActivity.this, ExpenseCategoriesAlarmActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mDeleteAlarms = findViewById(R.id.deleteAlarm);
        mDeleteAlarms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesAlarmMainActivity.this, ExpenseCategoriesAlarmDeleteActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mBack = findViewById(R.id.alarmBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesAlarmMainActivity.this, MainPageActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

}
