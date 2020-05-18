package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mimgBtn;
    private Button mspeechBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mimgBtn = (Button) findViewById(R.id.imgBtn);
        mspeechBtn = (Button) findViewById(R.id.speechBtn);

        mimgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgintent = new Intent(MainActivity.this, ImgRecognitionActivity.class);
                startActivity(imgintent);
            }
        });

        mspeechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent speechintent = new Intent(MainActivity.this, SpeechRecognitionActivity.class);
                startActivity(speechintent);
            }
        });
    }

}
