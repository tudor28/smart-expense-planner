package com.example.conversieimaginetext.expenseCategories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.conversieimaginetext.R;
import com.example.conversieimaginetext.receiptScan.MainPageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExpenseCategoriesCreateActivity extends AppCompatActivity {

    private EditText mNewCategory;
    private Button mSubmit, mBack;
    private DatabaseReference mDatabaseReference, mCategoriesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_create);

        mNewCategory = findViewById(R.id.newCategoryText);

        mSubmit = findViewById(R.id.createCateg);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mNewCategory.getText().toString().equals("")) {
                    Toast.makeText(ExpenseCategoriesCreateActivity.this,
                            "Introduceți numele categoriei!", Toast.LENGTH_SHORT).show();
                } else {
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    //String userId = mUser.getUid();
                    mCategoriesReference = mDatabaseReference.child("Categorii");
                            //.child(userId);

                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean categoryExists = false;
                            if (snapshot.hasChild(mNewCategory.getText().toString())) {
                                Toast.makeText(ExpenseCategoriesCreateActivity.this,
                                        "Categoria există deja!", Toast.LENGTH_SHORT).show();
                                categoryExists = true;
                            }
                            if (!categoryExists) {
                                mCategoriesReference.child(mNewCategory.getText().toString()).setValue(true);
                                Toast.makeText(ExpenseCategoriesCreateActivity.this,
                                        "Categoria a fost creată cu succes!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    mCategoriesReference.addListenerForSingleValueEvent(valueEventListener);
                }
            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesCreateActivity.this, ExpensesCategoriesMainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

}
