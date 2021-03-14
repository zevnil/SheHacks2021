package com.example.shehacks2021;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class JoinCommunityActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ListView joinCommunityListView;
    private ArrayList<String> joinCommunityArrayList;
    private ArrayAdapter<String> joinCommunityArrayAdapter;

    private ArrayList<String> userCommunitiesList;
    //private ArrayList<String> allCommunitiesList;

    private DatabaseReference communitiesRef;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserID;

    Calendar calForDate;
    String saveCurrentDate;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_community);

        calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar)findViewById(R.id.join_community_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Join Communities");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Communities");
        communitiesRef = FirebaseDatabase.getInstance().getReference().child("Communities");

        userCommunitiesList = new ArrayList<>();
        getUserCommunitiesList();

        joinCommunityListView = (ListView)findViewById(R.id.join_community_list_view);
        joinCommunityArrayList = new ArrayList<>();
        joinCommunityArrayAdapter = new ArrayAdapter<String>(JoinCommunityActivity.this, android.R.layout.simple_list_item_1, joinCommunityArrayList);
        joinCommunityListView.setAdapter(joinCommunityArrayAdapter);

        joinCommunityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String com = joinCommunityListView.getItemAtPosition(position).toString();
                Toast.makeText(JoinCommunityActivity.this, com, Toast.LENGTH_SHORT).show();

                loadingBar.setTitle("Adding you to community");
                loadingBar.setMessage("Please wait while we are add you to the community...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                addCommunityToUserDatabase(com);
                addUserToCommunityDatabase(com);
                loadingBar.dismiss();
                sendUserToMainActivity();
            }
        });
        setCommunityListView();
    }

    private void setCommunityListView() {
        communitiesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String acn = snapshot.getKey().toString();
                if(!userCommunitiesList.contains(acn)){
                    joinCommunityArrayList.add(acn);
                    joinCommunityArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserCommunitiesList() {
        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String ucn = snapshot.getKey().toString();
                userCommunitiesList.add(ucn);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addUserToCommunityDatabase(String selectedCommunity) {
        DatabaseReference selectedCommunityRef = FirebaseDatabase.getInstance().getReference().child("Communities").child(selectedCommunity);
        selectedCommunityRef.child("Members").child(currentUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(JoinCommunityActivity.this, "Added you to the community", Toast.LENGTH_SHORT).show();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(JoinCommunityActivity.this, "Error(Unable to add you to the community): "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addCommunityToUserDatabase(String communityName) {

        HashMap userMap = new HashMap();
        userMap.put("secretSantaTo", "none");
        userMap.put("secretSanta", "none");
        userMap.put("date", saveCurrentDate);
        userMap.put("messageReceived", "none");
        userMap.put("messageSent", "no");
        userMap.put("sentStatus", "no");
        userMap.put("receivedStatus", "no");

        userRef.child(communityName).updateChildren(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(JoinCommunityActivity.this, "Added to your communities", Toast.LENGTH_SHORT).show();
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(JoinCommunityActivity.this, "Error(Unable to add to your communities): "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(JoinCommunityActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}