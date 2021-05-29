package com.example.conversieimaginetext.expenseCategories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.conversieimaginetext.R;
import com.example.conversieimaginetext.categoriesAlarms.ExpenseCategoriesAlarmActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ExpenseCategoriesListActivity extends AppCompatActivity {

    private Button mBack, mSetAlarm;
    private ListView categoryList;
    DatabaseReference categoriesDb;
    ArrayList<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_list);

        categoryList = findViewById(R.id.categoryList);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.listview, categories);
        categoryList.setAdapter(arrayAdapter);

        categoriesDb = FirebaseDatabase.getInstance().getReference().child("Categorii");
        categoriesDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String category = ds.getKey();
                    categories.add(category);
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSetAlarm = findViewById(R.id.setAlarmCategory);
        mSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesListActivity.this, ExpenseCategoriesAlarmActivity.class);
                startActivity(intent);
            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesListActivity.this, ExpensesCategoriesMainActivity.class);
                startActivity(intent);
            }
        });
    }
}
