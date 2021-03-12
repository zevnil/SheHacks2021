package com.example.shehacks2021;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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

    public void takeToCommunityActivity(View view) {
        Intent communityIntent = new Intent(MainActivity.this, CommunityActivity.class);
        startActivity(communityIntent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item) {
        switch(item.getItemId()){

            case R.id.nav_post:
                Toast.makeText(this, "Add a Post", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Friends activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "Find friends activity", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages activity", Toast.LENGTH_SHORT).show();
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