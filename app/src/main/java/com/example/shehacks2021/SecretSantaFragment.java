package com.example.shehacks2021;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

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

public class SecretSantaFragment extends Fragment {

    private AppCompatButton volunteerButton, giftSentButton, giftReceivedButton, messageSentButton;
    private TextView waitingTextView, receiverInfoTextView, sendersMessageTextView;
    private RelativeLayout secretSantaLayout;
    private EditText receiverMessageEditText;

    private DatabaseReference communitiesRef;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserID;

    Calendar calForDate;
    String saveCurrentDate;
    String secretSantaTo;
    String addressAvailable;

    private ProgressDialog loadingBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ssFragView = inflater.inflate(R.layout.fragment_secret_santa, container, false);

        calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        loadingBar = new ProgressDialog(getContext());

        secretSantaLayout = (RelativeLayout) ssFragView.findViewById(R.id.secret_santa_layout);

        waitingTextView = (TextView)ssFragView.findViewById(R.id.secret_santa_waiting_textView);
        receiverInfoTextView = (TextView)ssFragView.findViewById(R.id.secret_santa_receiver_info_textview);
        sendersMessageTextView = (TextView)ssFragView.findViewById(R.id.secret_santa_sender_message_textview);

        receiverMessageEditText = (EditText) ssFragView.findViewById(R.id.secret_santa_receiver_message_edittext);

        communitiesRef = FirebaseDatabase.getInstance().getReference().child("Communities").child(CommunityActivity.currentCommunity);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Communities").child(CommunityActivity.currentCommunity);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    secretSantaTo = snapshot.child("secretSantaTo").getValue().toString();
                    addressAvailable = snapshot.child("addressAvailable").getValue().toString();

                    if(secretSantaTo.equals("none")){
                        volunteerButton.setVisibility(View.VISIBLE);
                    }else if(secretSantaTo.equals("waiting")){
                        waitingTextView.setVisibility(View.VISIBLE);
                    }else{
                        secretSantaLayout.setVisibility(View.VISIBLE);
                        String receiverInfo = snapshot.child("receiverInfo").getValue().toString();
                        receiverInfoTextView.setText(receiverInfo);

                        String receivedMessages = snapshot.child("messageReceived").getValue().toString();
                        String sentStatus = snapshot.child("sentStatus").getValue().toString();
                        String receivedStatus = snapshot.child("receivedStatus").getValue().toString();
                        String messageSentStatus = snapshot.child("messageSent").getValue().toString();

                        if(!receivedMessages.equals("none")){
                            sendersMessageTextView.setText(receivedMessages);
                            sendersMessageTextView.setEnabled(true);
                        }
                        if(sentStatus.equals("yes")){
                            giftSentButton.setEnabled(false);
                        }
                        if(receivedStatus.equals("yes")){
                            giftReceivedButton.setEnabled(false);
                            if(messageSentStatus.equals("no")){
                                receiverMessageEditText.setEnabled(true);
                                messageSentButton.setEnabled(true);
                            }else{
                                receiverMessageEditText.setEnabled(false);
                                receiverMessageEditText.setText(messageSentStatus);
                                messageSentButton.setEnabled(false);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        volunteerButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_frag_volunteer_button);
        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Volunteer button clicked", Toast.LENGTH_SHORT).show();

                if(addressAvailable.equals("no"))
                    Toast.makeText(getContext(), "Please upload your contact details before joining.", Toast.LENGTH_SHORT).show();
                else
                    volunteerInSecretSanta();
            }
        });

        giftSentButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_gift_sent_button);
        giftSentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child("sentStatus").setValue("yes");
                giftSentButton.setEnabled(false);
            }
        });
        giftReceivedButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_gift_received_button);
        giftReceivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child("receivedStatus").setValue("yes");
                giftReceivedButton.setEnabled(false);
                receiverMessageEditText.setEnabled(true);
                messageSentButton.setEnabled(true);
            }
        });
        messageSentButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_message_sent_button);
        messageSentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = receiverMessageEditText.getText().toString();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(getContext(), "Message is empty...", Toast.LENGTH_SHORT).show();
                }else{
                    DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference().child("Users").child(secretSantaTo).child("Communities").child(CommunityActivity.currentCommunity);
                    receiverRef.child("messageReceived").setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), "Message sent!", Toast.LENGTH_SHORT).show();
                                userRef.child("messageSent").setValue(message);
                                messageSentButton.setEnabled(false);
                                receiverMessageEditText.setEnabled(false);
                            }else{
                                String error = task.getException().toString();
                                Toast.makeText(getContext(), "Error in sending message: "+error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        return ssFragView;
    }


    private void volunteerInSecretSanta() {
        communitiesRef.child("Volunteers").child(currentUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            userRef.child("secretSantaTo").setValue("waiting").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getContext(), "VolunteeredSuccessfully", Toast.LENGTH_SHORT).show();
                                        volunteerButton.setVisibility(View.INVISIBLE);
                                        waitingTextView.setVisibility(View.VISIBLE);
                                    }else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(getContext(), "Error(Unable to set secretSantaTo to waiting): "+message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            String message = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error(Unable to add to volunteers): "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /*
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
     */
}
