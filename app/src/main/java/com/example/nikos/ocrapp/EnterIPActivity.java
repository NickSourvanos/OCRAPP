package com.example.nikos.ocrapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EnterIPActivity extends AppCompatActivity {
    private Button enter;
    private EditText serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_ip);

        serverIP = (EditText) findViewById(R.id.editText);
        enter = (Button) findViewById(R.id.enter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EnterIPActivity.this, MainActivity.class);
                intent.putExtra("serverAddress", serverIP.getText().toString());
                startActivity(intent);
            }
        });

        serverIP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                serverIP.setHint("Enter Server's IP");
            }
        });

    }
}
