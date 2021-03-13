package com.example.shehacks2021;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddCommunityActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText name, about;
    private AppCompatButton addButton;

    private ProgressDialog loadingBar;

    private DatabaseReference communitiesRef;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserID;

    Calendar calForDate;
    String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_community);

        mToolbar = (Toolbar)findViewById(R.id.add_community_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add Community");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Communities");

        name = (EditText)findViewById(R.id.add_community_name);
        about = (EditText)findViewById(R.id.add_community_about);
        addButton = (AppCompatButton)findViewById(R.id.add_community_add_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addActivityToDatabase();
            }
        });

        calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

    }

    private void addActivityToDatabase() {
        String communityName = name.getText().toString();
        String communityAbout = about.getText().toString();

        if(TextUtils.isEmpty(communityName))
            Toast.makeText(this, "Community name needed", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(communityAbout))
            Toast.makeText(this, "Community about needed", Toast.LENGTH_SHORT).show();
        else{
            loadingBar.setTitle("Creating Community");
            loadingBar.setMessage("Please wait while we are creating your community...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("name", communityName);
            userMap.put("about", communityAbout);

            communitiesRef = FirebaseDatabase.getInstance().getReference().child("Communities").child(communityName);

            communitiesRef.child("Details").updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(AddCommunityActivity.this, "Community created successfully", Toast.LENGTH_SHORT).show();
                        addCommunityToUserDatabase(communityName);
                        addUserToCommunityDatabase();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(AddCommunityActivity.this, "Error(Unable to create community): "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });


        }
    }

    private void addUserToCommunityDatabase() {
        communitiesRef.child("Members").child(currentUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AddCommunityActivity.this, "Added you to the community", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(AddCommunityActivity.this, "Error(Unable to add you to the community): "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addCommunityToUserDatabase(String communityName) {
        userRef.child(communityName).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(AddCommunityActivity.this, "Added to your communities", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(AddCommunityActivity.this, "Error(Unable to add to your communities): "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(AddCommunityActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}