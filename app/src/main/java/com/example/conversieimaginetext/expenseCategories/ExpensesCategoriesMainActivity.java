package com.example.conversieimaginetext.expenseCategories;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.conversieimaginetext.MainActivity;
import com.example.conversieimaginetext.R;

public class ExpensesCategoriesMainActivity extends AppCompatActivity {

    private Button mNewCategory, mListCategories, mDeleteCategory, mSetAlarm, mDeleteAlarm, mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_categories_main);

        mListCategories = findViewById(R.id.listCategories);
        mListCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesCategoriesMainActivity.this, ExpenseCategoriesListActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mNewCategory = findViewById(R.id.newCategory);
        mNewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesCategoriesMainActivity.this, ExpenseCategoriesCreateActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mDeleteCategory = findViewById(R.id.deleteCategory);
        mDeleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesCategoriesMainActivity.this, ExpenseCategoriesDeleteActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mSetAlarm = findViewById(R.id.setAlarm);
        mSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesCategoriesMainActivity.this, ExpenseCategoriesAlarmActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mDeleteAlarm = findViewById(R.id.deleteAlarm);
        mDeleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesCategoriesMainActivity.this, ExpenseCategoriesAlarmDelete.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mBack = findViewById(R.id.back1);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpensesCategoriesMainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
