package com.example.conversieimaginetext.expenseCategories;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoriesDeleteActivity extends AppCompatActivity {

    private Button mBack, mDeleteCateg;
    private Spinner mCategorySpinner;
    DatabaseReference mCategoriesReference, mLocationReference;
    String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_delete);

        mCategorySpinner = findViewById(R.id.categories);

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
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ExpenseCategoriesDeleteActivity.this, R.layout.spinner_layout, categories);
                arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
                mCategorySpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDeleteCateg = findViewById(R.id.deleteCateg);
        mDeleteCateg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = String.valueOf(mCategorySpinner.getSelectedItem());
                if (selectedCategory.equals("Selectați categoria..")) {
                    Toast.makeText(ExpenseCategoriesDeleteActivity.this,
                            "Selectați o categorie!", Toast.LENGTH_SHORT).show();
                } else {
                    mCategoriesReference = FirebaseDatabase.getInstance().getReference().child("Categorii").child(selectedCategory);
                    mCategoriesReference.removeValue();

                    mLocationReference = FirebaseDatabase.getInstance().getReference().child("Locații");
                    mLocationReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.getValue().equals(selectedCategory)) {
                                    ds.getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Toast.makeText(ExpenseCategoriesDeleteActivity.this,
                            "Categoria a fost ștearsă cu succes!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesDeleteActivity.this, ExpensesCategoriesMainActivity.class);
                startActivity(intent);
            }
        });
    }
}
