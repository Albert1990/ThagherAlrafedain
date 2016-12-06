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
import com.brain_socket.thagheralrafedain.data.FacebookProvider;
import com.brain_socket.thagheralrafedain.data.FacebookProviderListener;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.facebook.Profile;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail;
    private EditText etPassword;
    private Dialog loadingDialog;
    private TextView tvLoginStatusMessage;
    private String FbToken = null;
    private boolean linkWithFB = false;

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
        tvLoginStatusMessage = (TextView)findViewById(R.id.tvLoginStatusMessage);

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

    FacebookProviderListener facebookLoginListner = new FacebookProviderListener() {

        @Override
        public void onFacebookSessionOpened(String accessToken, String userId) {
            tvLoginStatusMessage.setText(R.string.login_progress_signing_in);

            FbToken = accessToken;
            Profile profile = com.facebook.Profile.getCurrentProfile();
            String fname = profile.getFirstName();
            String lname = profile.getLastName();
            String id = profile.getId();


            //DataStore.getInstance().attemptSignUp("t1@gmail.com", fname, lname, attemptingCountryCode, DataStore.VERSIOIN_ID, id, apiLoginCallback);
            linkWithFB = true;
            FacebookProvider.getInstance().unregisterListener();
        }

        @Override
        public void onFacebookSessionClosed() {
        }

        @Override
        public void onFacebookException(Exception exception) {

        }
    };

    /**
     * try login first using facebook if success then singning up to the API Server using the
     * facebook Id and phone number entered in the previous stage
     */
    public void attempFBtLogin() {
        //Session.openActiveSession(this, true, permissions, callback)
        FacebookProvider.getInstance().registerListener(facebookLoginListner);
        FacebookProvider.getInstance().requestFacebookLogin(this);
        //Session.StatusCallback callback =  new LoginStatsCallback() ;
        //Session.openActiveSession(LoginActivity.this, true, perm1, callback ) ;
        loadingDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FacebookProvider.getInstance().onActiviyResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
        //Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

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