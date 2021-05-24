package com.example.conversieimaginetext.receiptScan;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ReceiptScanSaveActivity extends AppCompatActivity {

    private static final String TAG = "aici";
    private TextView mScannedAmount, mDetectedLocation;
    private Button mSaveReceipt;
    private DatabaseReference mDatabaseReference, mDateReference, mCategoriesReference, mLocationReference, mAlarmReference;
    private Spinner mCategorySpinner;

    String detectedAmount = "", detectedLocation = "", selectedCategory = "", existingLocationCategory = "", existingLocation = "";
    boolean canSave = true, alreadyExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_scan_save);

        Toast.makeText(ReceiptScanSaveActivity.this,"Bonul a fost scanat cu succes!", Toast.LENGTH_SHORT).show();

        mScannedAmount = findViewById(R.id.scannedAmount);
        mDetectedLocation = findViewById(R.id.detectedShop);

        if (getIntent().hasExtra("DETECTED_PRICE")) detectedAmount = getIntent().getStringExtra("DETECTED_PRICE");
        if (getIntent().hasExtra("DETECTED_LOCATION")) detectedLocation = getIntent().getStringExtra("DETECTED_LOCATION");
        if (detectedAmount.isEmpty()) detectedAmount = "nedetectat";
        if (detectedLocation.isEmpty()) detectedLocation = "nedetectat";

        mScannedAmount.setText(detectedAmount);
        mDetectedLocation.setText(detectedLocation);

        mCategorySpinner = findViewById(R.id.receiptCategory);

        mLocationReference = FirebaseDatabase.getInstance().getReference().child("Locații");
        mLocationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    existingLocation = ds.getKey();
                    double locationSimilarity = similarity(existingLocation, detectedLocation);
                    if (locationSimilarity > 0.9) {
                        existingLocationCategory = ds.getValue().toString();
                        //Log.v(TAG, "existing location category! " + existingLocationCategory);
                        //Log.v(TAG, "existing location! " + existingLocation);
                        break;
                    }
                }
                //Log.v(TAG, "LOCATION CATEG: " + existingLocationCategory);
                if (!existingLocationCategory.isEmpty()) {
                    SpinnerAdapter adapter = mCategorySpinner.getAdapter();
                    if (adapter != null) {
                        // Log.v(TAG, "TRYING TO SET SPINNER TO: " + existingLocationCategory);
                        for (int position = 0; position < adapter.getCount(); position++) {
                            if (adapter.getItem(position).toString().toLowerCase().equals(existingLocationCategory.toLowerCase())) {
                                mCategorySpinner.setSelection(position);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mCategoriesReference = FirebaseDatabase.getInstance().getReference().child("Categorii");
                //.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mCategoriesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final List<String> categories = new ArrayList<>();
                categories.add("Selectați categoria..");

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String category = ds.getKey();
                    categories.add(category);
                }
                // Log.v(TAG, "creating spinner items..");
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ReceiptScanSaveActivity.this, R.layout.spinner_layout, categories);
                arrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
                mCategorySpinner.setAdapter(arrayAdapter);
                // Log.v(TAG, "LOCATION CATEG: " + existingLocationCategory);
                if (!existingLocationCategory.isEmpty()) {
                    SpinnerAdapter adapter = mCategorySpinner.getAdapter();
                    if (adapter != null) {
                        Log.v(TAG, "TRYING TO SET SPINNER TO: " + existingLocationCategory);
                        for (int position = 0; position < adapter.getCount(); position++) {
                            if (adapter.getItem(position).toString().toLowerCase().equals(existingLocationCategory.toLowerCase())) {
                                mCategorySpinner.setSelection(position);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSaveReceipt = findViewById(R.id.saveReceipt);

        mSaveReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                selectedCategory = String.valueOf(mCategorySpinner.getSelectedItem());
                // Log.v(TAG, "selectedCateg: " + selectedCategory);

                try {
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String userId = mUser.getUid();

                    mDateReference = mDatabaseReference.child("Bonuri").child(userId).child(currentDate);
                    mDateReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String location = String.valueOf(ds.child("Locație").getValue());
                                double locationSimilarity = similarity(location, detectedLocation);
                                if ((String.valueOf(ds.child("Categorie").getValue()).equals(selectedCategory)) || (String.valueOf(ds.child("Preț").getValue()).equals(detectedAmount))
                                        && (locationSimilarity > 0.5)) {
                                    canSave = false;
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            if (alreadyExists) {
                                Toast.makeText(ReceiptScanSaveActivity.this,
                                        "Bonul există deja in baza de date!", Toast.LENGTH_SHORT).show();
                            } else if (detectedLocation.equals("nedetectat")) {
                                Toast.makeText(ReceiptScanSaveActivity.this,
                                        "Locația bonului nu a fost detectată!", Toast.LENGTH_SHORT).show();
                                canSave = false;
                            } else if (detectedAmount.equals("nedetectat")) {
                                Toast.makeText(ReceiptScanSaveActivity.this,
                                        "Prețul total al bonului nu a fost detectat!", Toast.LENGTH_SHORT).show();
                                canSave = false;
                            } else if (selectedCategory.equals("Selectați categoria..")) {
                                Toast.makeText(ReceiptScanSaveActivity.this,
                                        "Selectați o categorie!", Toast.LENGTH_SHORT).show();
                                canSave = false;
                            }
                            if (canSave && !alreadyExists) {
                                String id = mDatabaseReference.push().getKey();
                                mDateReference.child(id).child("Locație").setValue(detectedLocation);
                                mDateReference.child(id).child("Preț").setValue(detectedAmount);
                                mDateReference.child(id).child("Ora").setValue(currentTime);
                                mDateReference.child(id).child("Categorie").setValue(selectedCategory);

                                mLocationReference = mDatabaseReference.child("Locații").child(detectedLocation);
                                mLocationReference.setValue(selectedCategory);

                                Toast.makeText(ReceiptScanSaveActivity.this,
                                        "Bonul a fost salvat cu succes!", Toast.LENGTH_SHORT).show();

                                mAlarmReference = mDatabaseReference.child("Alarme").child(userId);
                                mAlarmReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        try {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                if (ds.getKey().equals(selectedCategory)) {
                                                    String currentTotal = ds.child("Suma actuală").getValue().toString();
                                                    float total = Float.parseFloat(currentTotal);
                                                    String active = ds.child("Activă").getValue().toString();
                                                    String period = ds.child("Perioada").getValue().toString();
                                                    String dateCreated = ds.child("Data").getValue().toString();
                                                    String threshold = ds.child("Prag").getValue().toString();
                                                    float thresholdFloat = Float.parseFloat(threshold);
                                                    int daysNo = 0;
                                                    String notifText = "";
                                                    if (period.equals("Săptămânal")) {
                                                        daysNo = 7;
                                                        notifText = "săptămânal";
                                                    }
                                                    else if (period.equals("Lunar")) {
                                                        daysNo = 31;
                                                        notifText = "lunar";
                                                    }
                                                    if (active.equals("da")) {
                                                        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                                        long daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(dateCreated, formatter), LocalDate.parse(currentDate, formatter));
                                                        Log.v(TAG, "days between: " + daysBetween);

                                                        if (daysBetween <= daysNo) {
                                                            total = total + Float.parseFloat(detectedAmount);
                                                            mAlarmReference.child(selectedCategory).child("Suma actuală").setValue(String.valueOf(total));
                                                            if (total >= thresholdFloat) {
                                                                createNotificationChannel();
                                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Threshold")
                                                                        .setSmallIcon(R.drawable.alarm_icon)
                                                                        .setContentTitle("Pragul de cheltuieli " + notifText + " a fost atins!")
                                                                        .setContentText("Pragul " + notifText + " de " + threshold + " RON pentru categoria " + selectedCategory + " a fost atins! Suma atinsă: " + total + " RON")
                                                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                                                Random rand = new Random();
                                                                notificationManager.notify(rand.nextInt(10000), builder.build());
                                                            }
                                                        } else {
                                                            mAlarmReference.child(selectedCategory).child("Activă").setValue("nu");
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.v(TAG, "EXCEPTION! " + e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Error e) {
                    Log.v(TAG, "ERROR: " + e.getMessage());
                }
            }
        });
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Threshold channel";
            String description = "Threshold channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Threshold", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void goBack(View view) {
        Intent intent = new Intent(ReceiptScanSaveActivity.this, MainPageActivity.class);
        intent.putExtra("RECEIPT_PRICE", detectedAmount);
        intent.putExtra("RECEIPT_LOCATION", detectedLocation);
        startActivity(intent);
        finish();
        return;
    }

}
