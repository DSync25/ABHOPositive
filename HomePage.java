package com.blooddonation.abhopositive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;

    private String userID, getChildID;

    private DatabaseReference dbRef;

    private TextView postExistMsg;

    private List<String> mArrayList;

    private boolean checkFulfillment;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        if (loadState() == true){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.darkTheme);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme);
        }

        postExistMsg = (TextView) findViewById(R.id.ErrorMessage);

        Button userRequests = (Button) findViewById(R.id.UserRequests);
        final Button postRequest = (Button) findViewById(R.id.PostRequest);
        Button bloodRequests = (Button) findViewById(R.id.BloodRequests);
        Button responses = (Button) findViewById(R.id.Response);

        userRequests.setOnClickListener(this);
        postRequest.setOnClickListener(this);
        bloodRequests.setOnClickListener(this);
        responses.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.toolbar);

        /*----Toolbar----*/
        setSupportActionBar(toolbar);
        /*-----Navigation  drawer menu------*/
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navHome:
                startActivity(new Intent(HomePage.this, HomePage.class));
                break;
            case R.id.navRequests:
                startActivity(new Intent(HomePage.this, yourRequestActivity.class));
                break;
            case R.id.theme:
                item.setActionView(R.layout.theme_switch);
                final Switch themeSwitch = (Switch) item.getActionView().findViewById(R.id.action_switch);
                if (loadState() == true) {
                    themeSwitch.setChecked(true);
                }
                themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            saveState(true);
                            recreate();
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            saveState(false);
                        }
                    }
                });
                break;
            case R.id.navProfile:
                startActivity(new Intent(HomePage.this, profileActivity.class));
                break;
            case R.id.navLogout:
                Toast.makeText(HomePage.this, "Signing out...", Toast.LENGTH_SHORT).show();
                mAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(HomePage.this, MainActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.UserRequests:
                postExistMsg.setVisibility(View.INVISIBLE);
                startActivity(new Intent(HomePage.this, yourRequestActivity.class));
                break;

            case R.id.PostRequest:
                mArrayList = new ArrayList<>();
                userID = mAuth.getCurrentUser().getUid();
                dbRef = FirebaseDatabase.getInstance().getReference().child("Blood Requests History");
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot item_snapshot : dataSnapshot.getChildren()) {
                            getChildID = item_snapshot.child("UserID").getValue().toString();
                            if (getChildID.equals(userID)) {
                                DataSnapshot mfulfillment = item_snapshot.child("Fulfillment").child("Fulfilled");
                                if (mfulfillment.exists()) {
                                    if (mfulfillment.getValue().equals("yes")) {
                                        checkFulfillment = true;
                                    } else {
                                        checkFulfillment = false;
                                    }
                                } else {
                                    checkFulfillment = false;
                                }
                            }
                            mArrayList.add(getChildID);
                        }
                        if (mArrayList.contains(userID)) {
                            if (!checkFulfillment) {
                                postExistMsg.setVisibility(View.VISIBLE);
                            } else {
                                startActivity(new Intent(HomePage.this, PostRequest.class));
                            }
                        } else {
                            startActivity(new Intent(HomePage.this, PostRequest.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;

            case R.id.BloodRequests:
                postExistMsg.setVisibility(View.INVISIBLE);
                startActivity(new Intent(HomePage.this, RequestActivity.class));
                break;


            case R.id.Response:
                postExistMsg.setVisibility(View.INVISIBLE);
                startActivity(new Intent(HomePage.this, ResponsesActivity.class));
                break;
        }
    }

    private void saveState(Boolean state){
        SharedPreferences sharedPreferences = getSharedPreferences("ABHOPositive", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }

    private Boolean loadState(){
        SharedPreferences sharedPreferences = getSharedPreferences("ABHOPositive", MODE_PRIVATE);
        Boolean state = sharedPreferences.getBoolean("NightMode", false);
        return state;
    }

}