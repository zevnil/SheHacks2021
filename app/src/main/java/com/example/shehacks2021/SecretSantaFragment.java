package com.example.shehacks2021;

import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SecretSantaFragment extends Fragment {

    private AppCompatButton volunteerButton, giftSentButton, giftReceivedButton;
    private TextView waitingTextView, receiverInfoTextView, sendersMessageTextView;
    private RelativeLayout secretSantaLayout;
    private EditText receiverMessageEditText;

    private DatabaseReference communitiesRef;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserID;
    private DatabaseReference volunteerRef;

    Calendar calForDate;
    String saveCurrentDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ssFragView = inflater.inflate(R.layout.fragment_secret_santa, container, false);

        calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        secretSantaLayout = (RelativeLayout) ssFragView.findViewById(R.id.secret_santa_layout);

        waitingTextView = (TextView)ssFragView.findViewById(R.id.secret_santa_waiting_textView);
        receiverInfoTextView = (TextView)ssFragView.findViewById(R.id.secret_santa_receiver_info_textview);
        sendersMessageTextView = (TextView)ssFragView.findViewById(R.id.secret_santa_sender_message_textview);

        receiverMessageEditText = (EditText) ssFragView.findViewById(R.id.secret_santa_receiver_message_edittext);

        volunteerButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_frag_volunteer_button);
        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Volunteer button clicked", Toast.LENGTH_SHORT).show();
                volunteerInSecretSanta();
            }
        });

        giftSentButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_gift_sent_button);
        giftSentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        giftReceivedButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_gift_received_button);
        giftReceivedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        communitiesRef = FirebaseDatabase.getInstance().getReference().child("Communities").child(CommunityActivity.currentCommunity);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("Communities").child(CommunityActivity.currentCommunity);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String secretSantaTo = snapshot.child("secretSantaTo").getValue().toString();
                    if(secretSantaTo.equals("none")){
                        volunteerButton.setVisibility(View.VISIBLE);
                    }else if(secretSantaTo.equals("waiting")){
                        waitingTextView.setVisibility(View.VISIBLE);
                        searchForAMatch();
                    }else{
                        secretSantaLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return ssFragView;
    }

    private void searchForAMatch() {
        final int[] volunteersCount = new int[1];
        ArrayList<String> volunteersList = new ArrayList<>();
        String volunteerID;
        communitiesRef.child("Volunteers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                volunteersCount[0] = (int)snapshot.getChildrenCount();
                String volunteerIDstr = snapshot.getKey().toString();
                volunteersList.add(volunteerIDstr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(volunteersCount[0] > 1){
            final int[] flag = {0};
            for(int i=0; i<volunteersCount[0] && flag[0] == 0; ++i){
                volunteerID = volunteersList.get(i);
                if(!volunteerID.equals(currentUserID)){
                    volunteerRef = FirebaseDatabase.getInstance().getReference().child("Users").child(volunteerID).child("Communities").child(CommunityActivity.currentCommunity);
                    String finalVolunteerID = volunteerID;
                    volunteerRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String volSSto = snapshot.child("secretSantaTo").getValue().toString();
                                if(volSSto.equals("waiting")){
                                    userRef.child("secretSantaTo").setValue(finalVolunteerID);
                                    userRef.child("secretSanta").setValue(finalVolunteerID);
                                    volunteerRef.child("secretSantaTo").setValue(currentUserID);
                                    volunteerRef.child("secretSanta").setValue(currentUserID);
                                    flag[0] = 1;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        }
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
