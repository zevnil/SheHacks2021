package com.example.shehacks2021;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText username, fullname, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private StorageReference userProfileImageRef;

    String currentUserID;

    final static int gallery_pic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile image");
        //New folder(node) created in the database to store all the profile images (together)(Ab inki urls respective users k database mein bhi daalenge baad mein)

        username = (EditText)findViewById(R.id.setup_username);
        fullname = (EditText)findViewById(R.id.setup_full_name);
        countryName = (EditText)findViewById(R.id.setup_country);
        saveInformationButton = (Button)findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView)findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);
        
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        /*
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(SetupActivity.this, "Profile image clicked", Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallery_pic);
                //Abhi tak ki mehenat bas Gallery tak pohochne k liye
            }
        });
        //Update the profile image on the SetupActivity
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("profileImage")){
                        String image = snapshot.child("profileImage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileImage);
                    }else{
                        Toast.makeText(SetupActivity.this, "Profile image not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         */
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_pic && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            //Abhi image mil gayi ab use crop karna hai

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        //After the user clicks on the "crop" button:
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait while we are updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                //Ab cropped image mil gayi, ab use Firebase mein store karne k liye:
                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");  //Abhi woh separate folder mein save kar rahe hein
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Image stored successfully into Firebase", Toast.LENGTH_SHORT).show();
                            //Ab user k personal database mein uska url save kar rahe hein
                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            userRef.child("profileImage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //To reload the SetupActivity so that other information can be added (galleryIntent se waapas aao)
                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                        startActivity(selfIntent);

                                        Toast.makeText(SetupActivity.this, "Image stored to Firebase Database", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(this, "Error occurred while cropping the image.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }
    }
    */

    private void saveAccountSetupInformation() {
        String usernameStr = username.getText().toString();
        String fullnameStr = fullname.getText().toString();
        String countryStr = countryName.getText().toString();

        if(TextUtils.isEmpty(usernameStr))
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(fullnameStr))
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(countryStr))
            Toast.makeText(this, "Please enter country", Toast.LENGTH_SHORT).show();
        else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait while we are creating your account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", usernameStr);
            userMap.put("fullname", fullnameStr);
            userMap.put("country", countryStr);
            userMap.put("status", "Hi there! I'm using the Social Network.");
            userMap.put("gender", "null");
            userMap.put("dob", "null");
            userMap.put("secretSantaTo", "none");

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(SetupActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                        sendUserToMainActivity();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}