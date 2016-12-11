package com.brain_socket.thagheralrafedain.view;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.R;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel.WORKSHOP_TYPE;


public class AppWorkshopCard extends FrameLayout implements OnClickListener {

    // views
    TextView tvName;
    TextView tvType;
    TextView tvPhone;
    TextView tvAddress;
    ImageView ivLogo;
    View btnLocation;

    //Data
    WorkshopModel item;
    String phone;

    public AppWorkshopCard(Context context) {
        super(context);
        initialize();
    }

    public AppWorkshopCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AppWorkshopCard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    /**
     * Inflates and initializes the views and tool of this custom view
     */
    void initialize() {
        try {
            if (!isInEditMode()) {
                inflate(getContext(), R.layout.row_workshop_map, this);

                // views
                tvName = (TextView) findViewById(R.id.tvName);
                tvType = (TextView) findViewById(R.id.tvType);
                tvPhone = (TextView) findViewById(R.id.tvPhone);
                tvAddress = (TextView) findViewById(R.id.tvAddress);
                ivLogo = (ImageView) findViewById(R.id.ivLogo);
                btnLocation = findViewById(R.id.btnLocation);

                tvPhone.setOnClickListener(this);
                btnLocation.setOnClickListener(this);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Updates the view data
     */
    public void updateUI(WorkshopModel item) {
        try {
            this.item = item;

            //fill Data
            tvName.setText(item.getName());
            tvAddress.setText(item.getAddress());
            if (item.getType() == WORKSHOP_TYPE.SHOW_ROOM)
                tvType.setText(R.string.workshop_details_show_room);
            else
                tvType.setText(R.string.workshop_details_workshop);

            PhotoProvider.getInstance().displayPhotoNormal(item.getLogo(), ivLogo);

            phone = item.getPhone();
            tvPhone.setText(item.getPhone());
            if (phone == null || phone.isEmpty())
                tvPhone.setEnabled(false);
            else
                tvPhone.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callPhone() {
        try {
            if (phone != null) {
                String number = "tel:" + phone.trim();
                //ACTION_CALL needs permission
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(number));
                getContext().startActivity(callIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * opens Google Maps App to show the user how to get from the current location to the Provider location
     */
    private void showNavigateToProvider(){
        try{
            String url = "http://maps.google.com/maps?f=d&daddr="+item.getLat()+","+item.getLon();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
            getContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.tvPhone:
                    callPhone();
                    break;
                case R.id.btnLocation:
                    showNavigateToProvider();
                    break;
            }
        } catch (Exception e) {
        }
    }

}
