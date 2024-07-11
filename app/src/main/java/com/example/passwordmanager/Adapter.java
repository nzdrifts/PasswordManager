package com.example.passwordmanager;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{

    private List<UserData> userDataList;
    private Context context;

    public Adapter(ArrayList<UserData> userDataList,Context context){
        this.userDataList = userDataList;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_widget, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        String type = userDataList.get(position).getType();
        String username = userDataList.get(position).getUsername();
        String password = userDataList.get(position).getPassword();

        viewHolder.getLabel().setText(type);
        viewHolder.getUsername().setText(username);
        viewHolder.getPassword().setText("*".repeat(password.length()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return userDataList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView label,username,password;
        private ImageView visibility,edit,copy,delete;
        Boolean isVisible = Boolean.FALSE;

        public ViewHolder(View view) {
            super(view);

            label = view.findViewById(R.id.label);
            username = view.findViewById(R.id.username);
            password = view.findViewById(R.id.password);

            visibility = view.findViewById(R.id.toggle_visibility);
            edit = view.findViewById(R.id.edit);
            copy = view.findViewById(R.id.copy);
            delete = view.findViewById(R.id.delete);


            visibility.setOnClickListener(v -> {
                if (!isVisible){
                    password.setText(userDataList.get(getAdapterPosition()).getPassword());
                    visibility.setImageResource(R.drawable.ic_visible);
                    switchVisibility();
                } else{
                    password.setText("*".repeat(userDataList.get(getAdapterPosition()).getPassword().length()));
                    visibility.setImageResource(R.drawable.ic_invisible);
                    switchVisibility();
                }
            });

            edit.setOnClickListener(v -> {
                // Inflate the custom popup
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_edit_account, null);

                // Create a new PopupWindow
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(edit, Gravity.CENTER, 0, 0);

                //initialize views inside the popup
                Button set = (Button) popupView.findViewById(R.id.edit_account_set);
                TextView editType = popupView.findViewById(R.id.edit_type);
                TextView editUsername = popupView.findViewById(R.id.edit_username);
                TextView editPassword = popupView.findViewById(R.id.edit_password);

                editType.setText(userDataList.get(getAdapterPosition()).getType());
                editUsername.setText(userDataList.get(getAdapterPosition()).getUsername());
                editPassword.setText(userDataList.get(getAdapterPosition()).getPassword());

                set.setOnClickListener(v1 -> {
                    // add userdata to list
                    UserData userData = new UserData(String.valueOf(editType.getText()), String.valueOf(editUsername.getText()),String.valueOf(editPassword.getText()));
                    userDataList.set(getAdapterPosition(),userData);
                    // save data to file
                    saveData();
                    notifyItemChanged(getAdapterPosition());
                    // close popup
                    popupWindow.dismiss();
                });

            });

            copy.setOnClickListener(v -> {
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                String copyPass = userDataList.get(getAdapterPosition()).getPassword();
                ClipData clip = ClipData.newPlainText("pass",copyPass);

                // flag clipboard data as sensitive
                PersistableBundle extras = new PersistableBundle();
                extras.putBoolean("android.content.extra.IS_SENSITIVE", true);
                clip.getDescription().setExtras(extras);

                clipboard.setPrimaryClip(clip);
            });

            delete.setOnClickListener(v -> {
                // Inflate the custom popup
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_confirm_delete, null);

                // Create a new PopupWindow
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(delete, Gravity.CENTER, 0, 0);

                //initialize views inside the popup
                Button no = (Button) popupView.findViewById(R.id.cancel_delete);
                Button yes = (Button) popupView.findViewById(R.id.confirm_delete);

                no.setOnClickListener(v1 -> {
                    popupWindow.dismiss();
                });
                yes.setOnClickListener(v1 -> {
                    userDataList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    saveData();
                    Toast.makeText(context.getApplicationContext(), "Data Removed Successfully", Toast.LENGTH_SHORT).show();
                    popupWindow.dismiss();
                });
            });
        }

        private void switchVisibility() {
            if (isVisible){
                isVisible = Boolean.FALSE;
            }else{
                isVisible = Boolean.TRUE;
            }
        }

        public TextView getLabel(){return label;}
        public TextView getUsername(){return username;}
        public TextView getPassword(){return password;}
    }
    private void saveData() {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preference.edit();
        //convert list to String
        Gson gson = new Gson();
        String json = gson.toJson(userDataList);
        editor.putString("vault", json);
        editor.apply();
    }


}
