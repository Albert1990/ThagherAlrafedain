package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Albert on 12/11/16.
 */
public class DiagForgetPassword extends Dialog {
    private Context context;
    private ForgetPasswordDiagCallback callback;
    private EditText tvEmail;

    public DiagForgetPassword(Context context,ForgetPasswordDiagCallback callback){
        super(context);
        this.context = context;
        this.callback = callback;
        init();
    }

    private void init(){
        final TextView tvEmail = (TextView)findViewById(R.id.tvEmail);
        View btnDone = findViewById(R.id.btnDone);

        setTitle(context.getString(R.string.diag_forget_password_title));
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean cancel = false;
                View focusView = null;
                String email = tvEmail.getText().toString().trim();
                if(ThagherApp.isNullOrEmpty(email))
                {
                    cancel = true;
                    tvEmail.setError(context.getString(R.string.diag_forget_password_email_required));
                    focusView = tvEmail;
                }
                else
                {
                    if(!ThagherApp.isValidEmail(email))
                    {
                        cancel = true;
                        tvEmail.setError(context.getString(R.string.diag_forget_password_invalid_email));
                        focusView = tvEmail;
                    }
                }

                if(!cancel) {
                    callback.onClose(email);
                    dismiss();
                }
                else
                    focusView.requestFocus();
            }
        });
    }

    interface ForgetPasswordDiagCallback{
        void onClose(String email);
    }
}
