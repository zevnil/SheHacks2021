package com.example.shehacks2021;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommunityActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    static String currentCommunity;

    //Finding secret santa match
    int volunteersCount, flag;
    ArrayList<String> volunteersList;
    private DatabaseReference volunteerRef;
    private DatabaseReference communitiesRef;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        Intent intent = getIntent();
        currentCommunity = intent.getStringExtra("community");

        mToolbar = (Toolbar)findViewById(R.id.community_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentCommunity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new DiscussionsFragment()).commit();

        //Matching
        communitiesRef = FirebaseDatabase.getInstance().getReference().child("Communities").child(currentCommunity);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Communities").child(currentCommunity);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String secretSantaTo = snapshot.child("secretSantaTo").getValue().toString();
                    if (secretSantaTo.equals("waiting")) {
                        Toast.makeText(CommunityActivity.this, "Searching for a match...", Toast.LENGTH_SHORT).show();
                        searchForAMatch();
                    } else if(!secretSantaTo.equals("none")){
                        if(!snapshot.hasChild("receiverInfo")){
                            String receiver = snapshot.child("secretSantaTo").getValue().toString();
                            getReceiverInfo(receiver);
                        }

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        setAddressAvailability();
    }

    String addressL1, addressL2, city, pincode, contact;
    private void setAddressAvailability() {
        DatabaseReference tempURef =  FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        tempURef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressL1 = snapshot.child("addressL1").getValue().toString();
                addressL2 = snapshot.child("addressL2").getValue().toString();
                city = snapshot.child("city").getValue().toString();
                pincode = snapshot.child("pincode").getValue().toString();
                contact = snapshot.child("phoneNo").getValue().toString();
                if(addressL1.equals("N/A") || addressL2.equals("N/A") || city.equals("N/A") ||pincode.equals("N/A") || contact.equals("N/A")){
                    userRef.child("addressAvailable").setValue("no");
                }else{
                    userRef.child("addressAvailable").setValue("yes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Toast.makeText(CommunityActivity.this, addressL1+addressL2+city+pincode+contact, Toast.LENGTH_SHORT).show()
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()){
                        case R.id.nav_gallery:
                            selectedFragment = new GalleryFragment();
                            break;
                        case R.id.nav_discussions:
                            selectedFragment = new DiscussionsFragment();
                            break;
                        case R.id.nav_secret_santa:
                            selectedFragment = new SecretSantaFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    private void searchForAMatch() {
        volunteersCount = 0;
        volunteersList = new ArrayList<>();
        communitiesRef.child("Volunteers").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                volunteersCount = (int)snapshot.getChildrenCount();

                String volunteerIDstr = snapshot.getKey().toString();
                volunteersList.add(volunteerIDstr);
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
        if(volunteersCount > 1){
            flag = 0;
            for(String volunteerID: volunteersList){
                if(!volunteerID.equals(currentUserID)){
                    volunteerRef = FirebaseDatabase.getInstance().getReference().child("Users").child(volunteerID).child("Communities").child(currentCommunity);
                    volunteerRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String volSSto = snapshot.child("secretSantaTo").getValue().toString();
                                if(volSSto.equals("waiting")){
                                    userRef.child("secretSantaTo").setValue(volunteerID);
                                    userRef.child("secretSanta").setValue(volunteerID);
                                    volunteerRef.child("secretSantaTo").setValue(currentUserID);
                                    volunteerRef.child("secretSanta").setValue(currentUserID);
                                    flag = 1;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                if(flag!=0)
                    break;
            }
        }
    }

    private void getReceiverInfo(String receiver) {
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiver);
        receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String receiverInfo = "";
                receiverInfo = "Name: "+ snapshot.child("fullname").getValue().toString() + "\n";
                receiverInfo += "Address: " + snapshot.child("address").getValue().toString() + "\n";
                receiverInfo += "Pin code: " + snapshot.child("pincode").getValue().toString() + "\n";
                receiverInfo += "Phone No: " + snapshot.child("phoneNo").getValue().toString() + "\n";
                userRef.child("receiverInfo").setValue(receiverInfo);
                Toast.makeText(CommunityActivity.this, "Receiver info updated in database", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}