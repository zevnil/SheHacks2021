package com.example.shehacks2021;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextView usernameTv, fullnameTv, dobTv, genderTv, addressTv, countryTv, contactTv;
    private AppCompatButton editProfileButton;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference profileUserRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mToolbar = (Toolbar)findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usernameTv = (TextView)findViewById(R.id.profile_username_display);
        fullnameTv = (TextView)findViewById(R.id.profile_fullname_display);
        dobTv = (TextView)findViewById(R.id.profile_dob_display);
        genderTv = (TextView)findViewById(R.id.profile_gender_display);
        addressTv = (TextView)findViewById(R.id.profile_address_display);
        countryTv = (TextView)findViewById(R.id.profile_country_display);
        contactTv = (TextView)findViewById(R.id.profile_contact_no_display);
        editProfileButton = (AppCompatButton) findViewById(R.id.profile_edit_button);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String username = snapshot.child("username").getValue().toString();
                    String fullname = snapshot.child("fullname").getValue().toString();
                    String dob = snapshot.child("dob").getValue().toString();
                    String gender = snapshot.child("gender").getValue().toString();
                    String address = snapshot.child("address").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String contact = snapshot.child("phoneNo").getValue().toString();

                    usernameTv.setText(username);
                    fullnameTv.setText(fullname);
                    dobTv.setText(dob);
                    genderTv.setText(gender);
                    addressTv.setText(address);
                    countryTv.setText(country);
                    contactTv.setText(contact);
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Snapshot doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateProfileIntent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
                startActivity(updateProfileIntent);
            }
        });
    }
}