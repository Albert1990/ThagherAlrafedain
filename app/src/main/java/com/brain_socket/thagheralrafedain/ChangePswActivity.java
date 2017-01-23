package com.brain_socket.thagheralrafedain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.DataStore.DataRequestCallback;
import com.brain_socket.thagheralrafedain.data.ServerResult;

public class ChangePswActivity extends AppCompatActivity implements OnClickListener{

    EditText etOldPsw;
    EditText etNewPsw;
    View btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);
        init();
    }

    private void init(){
        etOldPsw = (EditText) findViewById(R.id.etOldPsw);
        etNewPsw = (EditText) findViewById(R.id.etNewPsw);
        btnSubmit =findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(this);
    }

    private void attemptSubmit(){

        String newPsw = etNewPsw.getText().toString();
        String oldPsw = etOldPsw.getText().toString();

        etNewPsw.setError(null);
        etOldPsw.setError(null);

        if(newPsw == null || newPsw.isEmpty()){
            etNewPsw.setError(getString(R.string.error_required_field));
        }else if(oldPsw == null || oldPsw.isEmpty()){
            etOldPsw.setError(getString(R.string.error_required_field));
        }else{
            DataStore.getInstance().attemptChangePsw(oldPsw, newPsw, new DataRequestCallback() {
                @Override
                public void onDataReady(ServerResult result, boolean success) {
                    if(success){
                        String msg = (String) result.get("msg");
                        if(msg != null && msg.equals("1")) {
                            Toast.makeText(ThagherApp.appContext, R.string.change_psw_success, Toast.LENGTH_LONG).show();
                            finish();
                        }else
                            Toast.makeText(ChangePswActivity.this, msg, Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(ChangePswActivity.this, R.string.change_psw_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSubmit:
                attemptSubmit();
                break;
        }
    }
}
