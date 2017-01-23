package com.brain_socket.thagheralrafedain;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.FacebookProvider;
import com.brain_socket.thagheralrafedain.data.FacebookProviderListener;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.AppUser.USER_TYPE;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    public static final int REQ_CODE_SIGNUP = 203;
    private EditText etEmail;
    private EditText etPassword;
    private Dialog loadingDialog;
    private TextView tvLoginStatusMessage;
    private String FbToken = null;
    private boolean linkWithFB = false;
    private View btnFBLogin;
    private View btnForgetPassword;
    private static final int RC_SIGN_IN = 007;
    private GoogleApiClient mGoogleApiClient;
    private Dialog forgetPasswordDiag;

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
        View btnGmailLogin = findViewById(R.id.btnGmailLogin);
        btnForgetPassword = findViewById(R.id.btnForgetPassword);

        forgetPasswordDiag = new DiagForgetPassword(this,forgetPasswordDiagCallback);
        btnGmailLogin.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        tvSignup.setOnClickListener(this);
        btnFBLogin.setOnClickListener(this);
        loadingDialog = ThagherApp.getNewLoadingDilaog(this);
        btnForgetPassword.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Customizing G+ button
        //googleSigninButton.setSize(SignInButton.SIZE_STANDARD);
        //googleSigninButton.setScopes(gso.getScopeArray());
    }

    private DiagForgetPassword.ForgetPasswordDiagCallback forgetPasswordDiagCallback = new DiagForgetPassword.ForgetPasswordDiagCallback() {
        @Override
        public void onClose(String email) {
            loadingDialog.show();
            DataStore.getInstance().requestForgetPassword(email,forgetPasswordCallback);
        }
    };

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
            loadingDialog.hide();
            if(success){
                if(result.containsKey("msg")) {
                    ThagherApp.Toast((String) result.getValue("msg"));
                }else{ // login success
                    AppUser me = DataStore.getInstance().getMe();
                    if(me != null && me.getUserType() != USER_TYPE.USER) {
                        // logged in successfully as workshop or showroom
                        Intent intent = new Intent(LoginActivity.this, WorkshopDetails.class);
                        startActivity(intent);
                    }
                    setResult(RESULT_OK);
                    finish();
                }
            }else {
                Toast.makeText(LoginActivity.this, R.string.err_check_connection, Toast.LENGTH_LONG).show();
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
            DataStore.getInstance().attemptSocialLogin(fullName,email,"facebook",id, attemptSocialLoginCallback);
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
                if(msg.equals("1")) {
                    loadingDialog.dismiss();
                    ThagherApp.Toast(getString(R.string.login_check_your_mail_box));
                }
                else
                    ThagherApp.Toast(msg);
            }
        }
    };

    DataStore.DataRequestCallback attemptSocialLoginCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.hide();
            AppUser me = DataStore.getInstance().getMe();

            // if user logged in successfully, and is not a workshop owner already, ask him to enter workshop details
            if(me != null && me.getUserType() == USER_TYPE.USER) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setCancelable(false)
                        .setMessage(R.string.login_add_your_workshop_msg)
                        .setNegativeButton(R.string.login_add_your_workshop_cancel, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.login_add_your_workshop_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(LoginActivity.this, WorkshopDetails.class);
                                startActivity(intent);
                                dialogInterface.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }).show();
            }else{
                if(me != null){ // user registered successfully and he is already a workshop owner
                    setResult(RESULT_OK);
                    finish();
                }else{ // register failure
                    ThagherApp.Toast(getString(R.string.err_check_connection));
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FacebookProvider.getInstance().onActiviyResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        if(resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
        //Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String fullName = acct.getDisplayName();
            //String personPhotoUrl = acct.getPhotoUrl().toString();
            String email = acct.getEmail();
            String id = acct.getId();
            DataStore.getInstance().attemptSocialLogin(fullName,email,"google",id,attemptSocialLoginCallback);

//            Glide.with(getApplicationContext()).load(personPhotoUrl)
//                    .thumbnail(0.5f)
//                    .crossFade()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(imgProfilePic);
        }
    }

    private void googleSignin(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void forgetPassword(){
        forgetPasswordDiag.show();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch(viewId){
            case R.id.btnLogin:
                attemptLogin();
                break;
            case R.id.btnFBLogin:
                attempFBtLogin();
                break;
            case R.id.btnGmailLogin:
                googleSignin();
                break;
            case R.id.btnForgetPassword:
                forgetPassword();
                break;
            case R.id.tvSignup:
                Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(i, REQ_CODE_SIGNUP);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}