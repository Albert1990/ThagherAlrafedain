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

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etEmail;
    private EditText etPassword;
    private Dialog loadingDialog;
    private TextView tvLoginStatusMessage;
    private String FbToken = null;
    private boolean linkWithFB = false;
    private View btnFBLogin;

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
        btnFBLogin = findViewById(R.id.btnFBLogin);

        btnLogin.setOnClickListener(this);
        tvSignup.setOnClickListener(this);
        btnFBLogin.setOnClickListener(this);
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
                    ThagherApp.Toast((String)result.getValue("msg"));
            }
        }
    };

    FacebookProviderListener facebookLoginListner = new FacebookProviderListener() {

        @Override
        public void onFacebookSessionOpened(String accessToken, String userId, HashMap<String, Object> map) {
            tvLoginStatusMessage.setText(R.string.login_progress_signing_in);

            FbToken = accessToken;
            Profile profile = com.facebook.Profile.getCurrentProfile();
            String fullName = profile.getFirstName()+" "+profile.getLastName();
            String id = profile.getId();
            String email = (String) map.get("email");

            linkWithFB = true;
            FacebookProvider.getInstance().unregisterListener();
            DataStore.getInstance().attemptSocialLogin(fullName,email,"facebook",id,attemptSocialLoginCallback);
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

    private DataStore.DataRequestCallback forgetPasswordCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            if(success)
            {
                String msg = result.getValue("msg").toString();
                if(msg.equals("1"))
                    ThagherApp.Toast("Done");
                else
                    ThagherApp.Toast(msg);
            }
        }
    };

    DataStore.DataRequestCallback attemptSocialLoginCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.dismiss();
        }
    };

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
        switch(viewId)
        {
            case R.id.btnLogin:
                attemptLogin();
                break;
            case R.id.btnFBLogin:
                attempFBtLogin();
                break;
            case 22:
                loadingDialog.show();
                DataStore.getInstance().requestForgetPassword("",forgetPasswordCallback);
                break;
            case R.id.tvSignup:
                Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(i);
                break;
        }
    }
}