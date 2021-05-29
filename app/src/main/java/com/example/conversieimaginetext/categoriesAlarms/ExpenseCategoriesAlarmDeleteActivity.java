package com.example.conversieimaginetext.categoriesAlarms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoriesAlarmDeleteActivity extends AppCompatActivity {

    private static final String TAG = "aici";

    private Button mBack, mDelete;
    private Spinner mCategorySpinner;
    DatabaseReference mCategoriesReference, mAlarmReference;
    String selectedCategory;
    int count = 0;
    TextView alarmText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_alarm_delete);

        mCategorySpinner = findViewById(R.id.categorySelect);

        mCategoriesReference = FirebaseDatabase.getInstance().getReference().child("Alarme").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mCategoriesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<String> categories = new ArrayList<>();
                categories.add("Selectați categoria..");

                for (DataSnapshot ds : snapshot.getChildren()) {
                    count ++;
                    String category = ds.getKey();
                    categories.add(category);
                }

                alarmText = findViewById(R.id.textView2);
                if (count == 0) alarmText.setText("Nu aveți alarme existente!");

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ExpenseCategoriesAlarmDeleteActivity.this, R.layout.spinner_layout, categories);
                arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
                mCategorySpinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDelete = findViewById(R.id.deleteAlarm);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedCategory = String.valueOf(mCategorySpinner.getSelectedItem());
                if (selectedCategory.equals("Selectați categoria..")) {
                    Toast.makeText(ExpenseCategoriesAlarmDeleteActivity.this,
                            "Selectați o categorie!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = user.getUid();
                    mAlarmReference = FirebaseDatabase.getInstance().getReference().child("Alarme").child(userId).child(selectedCategory);
                    mAlarmReference.removeValue();
                    Toast.makeText(ExpenseCategoriesAlarmDeleteActivity.this,
                            "Alarma pentru categoria " + selectedCategory + " a fost ștearsă cu succes!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBack = findViewById(R.id.categoryBack);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpenseCategoriesAlarmDeleteActivity.this, ExpenseCategoriesAlarmMainActivity.class);
                startActivity(intent);
            }
        });
    }
}