package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.ServerResult;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail;
    private EditText etPassword;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init(){
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        TextView tvSignup = (TextView)findViewById(R.id.tvSignup);

        btnLogin.setOnClickListener(this);
        tvSignup.setOnClickListener(this);
        loadingDialog = ThagherApp.getNewLoadingDilaog(this);
    }

    private void attemptLogin(){
        boolean cancel = false;
        View focusView = null;
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if(email.isEmpty()){
            cancel = true;
            etEmail.setError(getString(R.string.error_required_field));
            focusView = etEmail;
        }
        else if(!ThagherApp.isValidEmail(email)){
            cancel = true;
            etEmail.setError(getString(R.string.error_incorrect_email));
            focusView = etEmail;
        }

        if(password.isEmpty()){
            cancel = true;
            etPassword.setError(getString(R.string.error_required_field));
            focusView = etPassword;
        }
        if(cancel)
            focusView.requestFocus();
        else
        {
            loadingDialog.show();
            DataStore.getInstance().attemptLogin(email,password,attemptLoginCallback);
        }
    }

    DataStore.DataRequestCallback attemptLoginCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.dismiss();
            if(success){
                if(result.containsKey("msg"))
                    Toast.makeText(getApplicationContext(),(String)result.getValue("msg"),Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnLogin)
            attemptLogin();

        else if(viewId == R.id.tvSignup){
            Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(i);
        }
    }
}