package com.example.passwordmanager;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Vault extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton btn_add;
    ArrayList<UserData> userDataList;

    SharedPreferences preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Initialize views
        recyclerView = findViewById(R.id.recycler);
        btn_add = findViewById(R.id.add_account);

        // Load data
        loadData();
        if (userDataList == null) {
            userDataList = new ArrayList<>();
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter(userDataList, this));

        // Setup click listener
        clickListener();
    }


    // + FAB button
    private void clickListener() {
        btn_add.setOnClickListener(v -> {

            // Inflate the custom layout/view
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_add_account, null);

            // Create a new PopupWindow
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            boolean focusable = true;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.showAtLocation(btn_add, Gravity.CENTER, 0, 0);

            //initialize views inside the popup
            EditText tvAddType = (EditText) popupView.findViewById(R.id.add_type);
            EditText tvAddUsername = (EditText) popupView.findViewById(R.id.add_username);
            EditText tvAddPassword = (EditText) popupView.findViewById(R.id.add_password);
            Button btn_submit = popupView.findViewById(R.id.add_account_submit);


            // listener for adding account
            btn_submit.setOnClickListener(v2 -> {
                // first check to see that no input box is empty
                if (String.valueOf(tvAddType.getText()).isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter a Type", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(String.valueOf(tvAddUsername.getText()).isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter a Username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(String.valueOf(tvAddPassword.getText()).isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter a Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // add userdata to list
                UserData userData = new UserData(String.valueOf(tvAddType.getText()), String.valueOf(tvAddUsername.getText()),String.valueOf(tvAddPassword.getText()));
                userDataList.add(userData);

                // save data to file
                saveData();

                //!!!!!!
                // notify the recycler adapter that a new thing has been added
                // or reload the recycler from the file
                // !!!!!

                // end popup
                Toast.makeText(getApplicationContext(), "Successfully added " + String.valueOf(tvAddType.getText()) + " account", Toast.LENGTH_SHORT).show();
                //btn_add.setVisibility(View.VISIBLE);
                popupWindow.dismiss();


            });



        });
    }

    private void saveData() {
        SharedPreferences.Editor editor = preference.edit();
        //convert list to String
        Gson gson = new Gson();
        String json = gson.toJson(userDataList);
        editor.putString("vault", json);
        editor.apply();
    }

    //loads data into userDataList ArrayList
    private void loadData() {
        Gson gson = new Gson();
        String json = preference.getString("vault", null);
        Type type = new TypeToken<ArrayList<UserData>>() {}.getType();
        userDataList = gson.fromJson(json, type);
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

}
