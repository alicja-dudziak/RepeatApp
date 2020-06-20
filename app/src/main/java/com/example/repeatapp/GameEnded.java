package com.example.repeatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameEnded extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_ended);
        Intent intent = getIntent();
        String durationTime = intent.getStringExtra("DurationTime");

        SetDurationTime(durationTime);
        AddButtonListener();
    }

    private void SetDurationTime(String durationTime)
    {
        TextView durationTimeTextView = findViewById(R.id.durationTime);
        durationTimeTextView.setText("Duration time: "+durationTime);
    }

    private void AddButtonListener()
    {
        Button backButton = findViewById(R.id.backToMainViewButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
