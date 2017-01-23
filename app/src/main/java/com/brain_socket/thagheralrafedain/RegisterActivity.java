package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.ServerResult;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private Dialog loadingDialog;
    private CheckBox cbWorkshop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init(){
        etFullName = (EditText)findViewById(R.id.etFullName);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPhone = (EditText)findViewById(R.id.etPhone);
        etPassword = (EditText)findViewById(R.id.etPassword);
        cbWorkshop = (CheckBox)findViewById(R.id.cbWorkshop);
        Button btnSignup = (Button)findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(this);
        loadingDialog = ThagherApp.getNewLoadingDilaog(this);
    }

    private DataStore.DataRequestCallback attemptSignupCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.dismiss();
            if(success){
                if(result.containsKey("msg")) {
                    String msg = result.getValue("msg").toString();
                    ThagherApp.Toast(msg);

                }else if(result.containsKey("appUser")){
                    ThagherApp.Toast(getString(R.string.activity_register_success));
                    // to make sure the login activity will close
                    setResult(RESULT_OK);
                    finish();

                    // if signed up as workshop, open workshop details
                    if(cbWorkshop.isChecked()) {
                        Intent i = new Intent(RegisterActivity.this, WorkshopDetails.class);
                        startActivity(i);
                    }
                }
            }
        }
    };

    private void attemptSignup(){
//        Intent i = new Intent(RegisterActivity.this,WorkshopDetails.class);
//        startActivity(i);

        try{
            boolean cancel = false;
            View focusView = null;
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString();
            String userType = "I"; //workshop

            if(ThagherApp.isNullOrEmpty(fullName)){
                cancel = true;
                etEmail.setError(getString(R.string.error_required_field));
                focusView = etFullName;
            }
            if(ThagherApp.isNullOrEmpty(email)){
                cancel = true;
                etEmail.setError(getString(R.string.error_required_field));
                focusView = etEmail;
            }
            else if(!ThagherApp.isValidEmail(email)){
                cancel = true;
                etEmail.setError(getString(R.string.error_incorrect_email));
                focusView = etEmail;
            }

            if(ThagherApp.isNullOrEmpty(phone)){
                cancel = true;
                etPhone.setError(getString(R.string.error_required_field));
                focusView = etPhone;
            }

            if(ThagherApp.isNullOrEmpty(password)){
                cancel = true;
                etPassword.setError(getString(R.string.error_required_field));
                focusView = etPassword;
            }

            if(cbWorkshop.isChecked())
                userType = "W";

            if(cancel)
                focusView.requestFocus();
            else
            {
                loadingDialog.show();
                DataStore.getInstance().attemptSignUp(email, password, fullName, phone, userType, attemptSignupCallback);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnSignup)
            attemptSignup();
    }
}
