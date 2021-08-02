package com.example.studyletapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.ProgressBar;

public class OpeningActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(OpeningActivity.this, LRContainerActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);

        loadingBar = findViewById(R.id.openingLoadingBar);

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                long finishedSeconds = 3000 - millisUntilFinished;
                int total = (int) (((float) finishedSeconds / (float) 3000) * 100.0);
                loadingBar.setProgress(total);
            }

            public void onFinish() {

            }

        }.start();
    }
}