package com.example.onemed;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class medical_records_activity extends AppCompatActivity {

    private RecyclerView medicalRecordsRecyclerView;
    private MedicalRecordAdapter adapter;
    private List<MedicalRecord> recordList = new ArrayList<>();
    private SearchView searchView;
    private TabLayout tabLayout;
    private ViewGroup noResultContainer;
    private View noResultView;

    private ImageButton bookAppointment, homeButton, history_btn,add_record;
    private ImageView back_btn;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medical_records);

        //nav init
        back_btn=findViewById(R.id.back_btn);
        bookAppointment=findViewById(R.id.bookAppointment);
        homeButton = findViewById(R.id.homeButton);
        history_btn = findViewById(R.id.history_btn);

        //nav functions
        back_btn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        homeButton.setOnClickListener(v -> startActivity(new Intent(medical_records_activity.this, main_activity.class)));
        bookAppointment.setOnClickListener(v -> startActivity(new Intent(medical_records_activity.this, BookAppointment_activity.class)));
        history_btn.setOnClickListener(v->startActivity(new Intent(medical_records_activity.this, history_activity.class)));

        // add medical record button
        add_record=findViewById(R.id.add_record);
        add_record.setOnClickListener(v->startActivity
                (new Intent(medical_records_activity.this, add_record_activity.class))
        );

        // Firebase Auth and Firestore
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // View bindings
        medicalRecordsRecyclerView = findViewById(R.id.medicalRecordsRecyclerView);
        searchView = findViewById(R.id.search_bar);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.clearFocus();

        // Customize SearchView hint appearance
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setHint("Search Medical Records here...");
            searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
            searchEditText.setTextSize(12);
            searchEditText.setTypeface(Typeface.DEFAULT);
            searchEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

        noResultContainer = findViewById(R.id.no_result_container);
        noResultView = LayoutInflater.from(this).inflate(R.layout.no_result_found, noResultContainer,false);

        tabLayout = findViewById(R.id.tabLayout);

        // Setup RecyclerView
        adapter = new MedicalRecordAdapter(this, recordList);
        medicalRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicalRecordsRecyclerView.setAdapter(adapter);

        // Tab setup (assuming 2 tabs: "Official" and "Self-Uploaded")
        tabLayout.addTab(tabLayout.newTab().setText("Official"));
        tabLayout.addTab(tabLayout.newTab().setText("Self-Uploaded"));

        // Fetch default tab (Official)
        fetchMedicalRecords("official");

        // Tab change listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedType = tab.getText().toString().toLowerCase();
                fetchMedicalRecords(selectedType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

    }


    private void fetchMedicalRecords(String type) {
        // Clear current list
        recordList.clear();

        CollectionReference recordsRef = db.collection("users")
                .document(uid)
                .collection("medical_records_" + type);

        recordsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    MedicalRecord record = doc.toObject(MedicalRecord.class);
                    recordList.add(record);
                }
                adapter.notifyDataSetChanged();

                if (recordList.isEmpty()) {
                    Toast.makeText(medical_records_activity.this, "No records found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(medical_records_activity.this, "Error fetching data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        List<MedicalRecord> filteredList = new ArrayList<>();
        for (MedicalRecord item : recordList) {
            if (item.getRecordtitle().toLowerCase().contains(text.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }
    private void updateVisibility(boolean isEmpty) {
        if (isEmpty) {
            medicalRecordsRecyclerView.setVisibility(View.GONE);
            if (noResultView.getParent() == null) {
                noResultContainer.addView(noResultView);
            }
            noResultContainer.setVisibility(View.VISIBLE);
        } else {
            medicalRecordsRecyclerView.setVisibility(View.VISIBLE);
            noResultContainer.removeAllViews();
            noResultContainer.setVisibility(View.GONE);
        }
    }
}
