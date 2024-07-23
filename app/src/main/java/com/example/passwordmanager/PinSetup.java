package com.example.passwordmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinSetup  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpin);

        Button setPin = findViewById(R.id.set_pin);
        TextView enter_pin = findViewById(R.id.enter_set_pin);
        TextView confirm_pin = findViewById(R.id.confirm_set_pin);

        setPin.setOnClickListener(v -> {
            if (enter_pin.getText().toString().isEmpty() && confirm_pin.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter PIN to Proceed", Toast.LENGTH_SHORT).show();
            } else if (confirm_pin.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Confirm PIN to Proceed", Toast.LENGTH_SHORT).show();
            } else {
                int ePIN = Integer.parseInt(enter_pin.getText().toString());
                int cPIN = Integer.parseInt(confirm_pin.getText().toString());
                if (enter_pin.getText().toString().length() < 4 || enter_pin.getText().toString().length() > 6) {
                    Toast.makeText(getApplicationContext(), "PIN less than 4 and greater than 6 digits not allowed", Toast.LENGTH_SHORT).show();
                } else if (ePIN != cPIN) {
                    Toast.makeText(getApplicationContext(), "Enter the same PIN as above", Toast.LENGTH_SHORT).show();
                } else {
                    String PIN = enter_pin.getText().toString().trim();
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    // encrypts string and stores it
                    try{
                        editor.putString("PIN", AES.encrypt(PIN));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editor.commit();
                    startActivity(new Intent(PinSetup.this, Vault.class));
                    finish();
                }
            }
        });
    }

}
