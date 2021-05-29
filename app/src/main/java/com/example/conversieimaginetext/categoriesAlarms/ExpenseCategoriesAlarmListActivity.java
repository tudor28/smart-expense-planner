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
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Locale;

public class ExpenseCategoriesAlarmListActivity extends AppCompatActivity {

    private Button mBack;
    private TextView noAlarm;
    DatabaseReference mAlarmsReference;
    private ListView alarmList;
    ArrayList<String> alarms = new ArrayList<>();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_categories_alarm_list);

        alarmList = findViewById(R.id.alarmList);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.listview, alarms);
        alarmList.setAdapter(arrayAdapter);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = mUser.getUid();
        mAlarmsReference = FirebaseDatabase.getInstance().getReference().child("Alarme").child(userId);
        mAlarmsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String alarm = ds.getKey();
                    String period = ds.child("Perioada").getValue().toString();
                    String dateCreated = ds.child("Data").getValue().toString();
                    String threshold = ds.child("Prag").getValue().toString();
                    String currentSum = ds.child("Suma actuală").getValue().toString();

                    int daysNo = 31;
                    if (period.equals("Săptămânală")) {
                        daysNo = 7;
                    }

                    final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    long daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(dateCreated, formatter), LocalDate.parse(currentDate, formatter));

                    String alarmText = "Categorie: " + alarm + "\n Perioada: " + period + "\n Pragul: " + threshold + "\n Suma atinsă: " + currentSum + "\n Zile până la expirarea alarmei: " + (daysNo - daysBetween) + "\n";
                    alarms.add(alarmText);
                    arrayAdapter.notifyDataSetChanged();
                    count ++;
                }
                if (count == 0) {
                    noAlarm.setText("Nu aveți alarme setate!");
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
                Intent intent = new Intent(ExpenseCategoriesAlarmListActivity.this, ExpenseCategoriesAlarmMainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
