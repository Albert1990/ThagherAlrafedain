package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;


public class DiagPickPhoto extends Dialog implements OnClickListener{

	public enum PickDiagActions {GALLERY, CAMERA, CANCEL };
	LinearLayout llGallery , llCamera , llCancel;
	PickDiagCallBack diagCallback;
	public DiagPickPhoto(Context context , PickDiagCallBack callback) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.diag_pick_photo);
		diagCallback = callback;
		init();
	}

	private void init() {
		llGallery = (LinearLayout) findViewById(R.id.llGallery);
		llCamera = (LinearLayout) findViewById(R.id.llCamera);
		llCancel = (LinearLayout) findViewById(R.id.llCancel);
		
		llGallery.setOnClickListener(this);
		llCamera.setOnClickListener(this);
		llCancel.setOnClickListener(this);

		WindowManager.LayoutParams wmlp = getWindow().getAttributes();
		wmlp.gravity = Gravity.BOTTOM |Gravity.CENTER_HORIZONTAL;
		wmlp.verticalMargin = 0f;
		wmlp.horizontalMargin = 0f;

		getWindow().getAttributes().windowAnimations = R.style.DialogAnimationFromBottom;
		getWindow().setAttributes(wmlp);
		//wmlp.x = 0;
		//wmlp.y = 0;
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.llGallery:
			diagCallback.onActionChoose(PickDiagActions.GALLERY);
			break;
		case R.id.llCamera:
			diagCallback.onActionChoose(PickDiagActions.CAMERA);
			break;
		case R.id.llCancel:
			diagCallback.onActionChoose(PickDiagActions.CANCEL);
			break;
		default:
			break;
		}
		dismiss();
	}

	public interface PickDiagCallBack{
		void onActionChoose(PickDiagActions action);
	}

}
