package com.example.passwordmanager;

import static androidx.biometric.BiometricConstants.ERROR_NEGATIVE_BUTTON;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PinVerification extends AppCompatActivity {

    private Button btn_setPinTest, btn_deletePinTest, btn_next;
    private TextView tv_enterPin, tv_countdown;
    private SharedPreferences preference;
    private int attempts = 5;

    private BiometricPrompt biometricPrompt=null;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinverification);

        this.preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // check to see if pin has been set
        if (preference.getString("PIN",null) == ""){
            startActivity(new Intent(PinVerification.this, PinSetup.class));
            finish();
        }

        // initialize views
        initializeViews();
        //hide test buttons
        btn_setPinTest.setVisibility(View.GONE);
        btn_deletePinTest.setVisibility(View.GONE);

        // biometric prompt
        if(biometricPrompt==null){
            biometricPrompt=new BiometricPrompt(this,executor,callback);
        }
        checkAndAuthenticate();

        // Click Listener
        clickListener();
    }

    private void clickListener() {
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pinString = preference.getString("PIN",null);
                try {
                pinString = AES.decrypt(pinString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // check if input is empty
                if(String.valueOf(tv_enterPin.getText()).trim().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter PIN to proceed", Toast.LENGTH_SHORT).show();
                } else if (attempts >= 1) {
                // checks attempts left
                    String pinEntered = String.valueOf(tv_enterPin.getText()).trim();
                    if (!pinEntered.equals(pinString)) {
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

        // TEST
        btn_setPinTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preference.edit();
                editor.putString("PIN", "123");
                // Commit the changes
                editor.apply();
            }
        });

        btn_deletePinTest.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("PIN", "");
            // Commit the changes
            editor.apply();
        });
    }

    private void initializeViews() {
        this.btn_setPinTest = findViewById(R.id.set_pin_test);
        this.btn_deletePinTest = findViewById(R.id.delete_pin_test);
        this.btn_next = findViewById(R.id.next);

        this.tv_enterPin = findViewById(R.id.enter_pin);
        this.tv_countdown = findViewById(R.id.countdown);
    }



    // Biometric methods
    private void checkAndAuthenticate(){
        BiometricManager biometricManager=BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS){
            BiometricPrompt.PromptInfo promptInfo = buildBiometricPrompt();
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private BiometricPrompt.PromptInfo buildBiometricPrompt() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login")
                .setSubtitle("FingerPrint Authentication")
                .setDescription("Please place your finger on the sensor to unlock")
                .setDeviceCredentialAllowed(true)
                .build();

    }

    private BiometricPrompt.AuthenticationCallback callback=new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    if(errorCode==ERROR_NEGATIVE_BUTTON && biometricPrompt!=null)
                        biometricPrompt.cancelAuthentication();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    startActivity(new Intent(PinVerification.this, Vault.class));
                    finish();
                }

                @Override
                public void onAuthenticationFailed() {
                    Toast.makeText(getApplicationContext(), "Try again", Toast.LENGTH_SHORT).show();
                }
            };

}