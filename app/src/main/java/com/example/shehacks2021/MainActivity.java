package com.example.shehacks2021;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserId;
    FirebaseUser mFirebaseUser;

    private CircleImageView navProfileImage;
    private TextView navProfileUsername;

    private ListView communityListView;
    private ArrayList<String> communityArrayList;
    private ArrayAdapter<String> communityArrayAdapter;
    private DatabaseReference userCommunitiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //currentUserId = mAuth.getCurrentUser().getUid();
        mFirebaseUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar)findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Communities");

        communityListView = (ListView)findViewById(R.id.main_list_view);
        communityArrayList = new ArrayList<>();
        communityArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, communityArrayList);
        communityListView.setAdapter(communityArrayAdapter);

        communityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String com = communityListView.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, com, Toast.LENGTH_SHORT).show();
                Intent communityIntent = new Intent(MainActivity.this, CommunityActivity.class);
                communityIntent.putExtra("community", com);
                startActivity(communityIntent);
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView)navView.findViewById(R.id.nav_header_profile_image);
        navProfileUsername = (TextView)navView.findViewById(R.id.nav_header_username);

        if(mFirebaseUser != null) {
            currentUserId = mFirebaseUser.getUid(); //Do what you need to do with the id
            setTheListView();

            userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(snapshot.hasChild("username")){
                            String username = snapshot.child("username").getValue().toString();
                            navProfileUsername.setText(username);
                        }else{
                            Toast.makeText(MainActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                        /*
                        if(snapshot.hasChild("profileImage")){
                            String image = snapshot.child("profileImage").getValue().toString();
                            //Toast.makeText(MainActivity.this, image, Toast.LENGTH_SHORT).show();
                            Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);
                        }else{
                            Toast.makeText(MainActivity.this, "profile image not found", Toast.LENGTH_LONG).show();
                        }

                         */
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);
                return false;
            }
        });
    }

    private void setTheListView() {
        userCommunitiesRef = userRef.child(currentUserId).child("Communities");

        userCommunitiesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String communityName = snapshot.getKey().toString();
                communityArrayList.add(communityName);
                communityArrayAdapter.notifyDataSetChanged();
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

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
            sendUserToLoginActivity();
        else
            checkUserExistence();
    }

    private void checkUserExistence() {
        final String currentUserID = mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(currentUserID)){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item) {
        switch(item.getItemId()){

            case R.id.nav_profile:
                Toast.makeText(this, "Profile activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_add_community:
                Toast.makeText(this, "AddCommunity activity", Toast.LENGTH_SHORT).show();
                Intent addCommunityIntent = new Intent(MainActivity.this, AddCommunityActivity.class);
                startActivity(addCommunityIntent);
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
    }
}