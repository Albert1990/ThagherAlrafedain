package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail;
    private EditText etPassword;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init(){
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        Button btnSignup = (Button)findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(this);
        loadingDialog = ThagherApp.getNewLoadingDilaog(this);
    }

    private void attemptSignup(){
        Intent i = new Intent(RegisterActivity.this,WorkshopDetails.class);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnSignup)
            attemptSignup();
    }
}
