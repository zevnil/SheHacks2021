package com.example.shehacks2021;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class SecretSantaFragment extends Fragment {

    private AppCompatButton volunteerButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ssFragView = inflater.inflate(R.layout.fragment_secret_santa, container, false);
        volunteerButton = (AppCompatButton)ssFragView.findViewById(R.id.secret_santa_frag_volunteer_button);
        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Volunteer button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        return ssFragView;
    }

    /*
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

     */
}
