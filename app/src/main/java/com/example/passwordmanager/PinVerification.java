package com.example.passwordmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PinVerification extends AppCompatActivity {

    private Button btn_setPinTest, btn_next;
    private TextView tv_enterPin, tv_countdown;
    private SharedPreferences preference;
    private int attempts = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinverification);

        this.preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // initialize views
        initializeViews();

        // Click Listener
        clickListener();
    }

    private void clickListener() {
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int realPin = Integer.parseInt(preference.getString("PIN",null));

                // check if input is empty
                if(String.valueOf(tv_enterPin.getText()).trim().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter PIN to proceed", Toast.LENGTH_SHORT).show();
                } else if (attempts >= 1) {
                // checks attempts left
                    int pinEntered = Integer.parseInt(String.valueOf(tv_enterPin.getText()).trim());
                    if (pinEntered != realPin) {
                        Toast.makeText(getApplicationContext(), "Incorrect PIN entered", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Attempts left " + attempts, Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(PinVerification.this, Vault.class));
                        finish();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Attempt limit exceed", Toast.LENGTH_SHORT).show();
                    new CountDownTimer(60000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            tv_countdown.setVisibility(View.VISIBLE);
                            btn_next.setVisibility(View.GONE);
                            tv_enterPin.setText("");
                            tv_enterPin.setFocusable(false);
                            tv_countdown.setText("try again in: " + millisUntilFinished / 1000 +" s");

                        }

                        public void onFinish() {
                            tv_countdown.setVisibility(View.GONE);
                            btn_next.setVisibility(View.VISIBLE);
                            tv_enterPin.setFocusable(true);
                            attempts = 2;
                        }

                    }.start();
                }
                attempts -=1;
            }
        });

        btn_setPinTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preference.edit();
                editor.putString("PIN", "123");
                // Commit the changes
                editor.apply();
            }
        });
    }

    private void initializeViews() {
        this.btn_setPinTest = findViewById(R.id.set_pin_test);
        this.btn_next = findViewById(R.id.next);

        this.tv_enterPin = findViewById(R.id.enter_pin);
        this.tv_countdown = findViewById(R.id.countdown);
    }


}