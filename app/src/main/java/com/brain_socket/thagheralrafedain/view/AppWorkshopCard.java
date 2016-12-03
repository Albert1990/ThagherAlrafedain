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
import com.brain_socket.thagheralrafedain.model.WorkshopModel;


public class AppWorkshopCard extends FrameLayout implements OnClickListener {

    // views
    TextView tvProviderPreviewName;
    TextView tvProviderPreviewType;
    ImageView ivProviderPreviewLogo;
    View btnCall;
    View btnLocation;
    View vSep;

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
                tvProviderPreviewName = (TextView) findViewById(R.id.tvName);
                tvProviderPreviewType = (TextView) findViewById(R.id.tvType);
                ivProviderPreviewLogo = (ImageView) findViewById(R.id.ivLogo);
                btnCall = findViewById(R.id.btnCall);
                vSep = findViewById(R.id.vSep);
                btnCall.setOnClickListener(this);

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


            ///fill Data
            tvProviderPreviewName.setText(item.getName());
//            if (item.get() != null)
//                tvProviderPreviewType.setText(item.getCategory().getName());
            //PhotoProvider.getInstance().displayPhotoNormal(item.getLogo(), ivProviderPreviewLogo);

            //phone = SindContact.getContactInfoByType(item.getContacts(), SindContact.contactType.MOBILE);
            if (phone == null || phone.isEmpty())
                btnCall.setVisibility(GONE);
            else
                btnCall.setVisibility(VISIBLE);

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
    private void showNavigateToProvider() {
//        SearchActivity activity = (SearchActivity) getContext();
//        Intent i = new Intent(getContext(), MapActivity.class);
//        i.putExtras(MapActivity.getLauncherBundle(FragMap.MAP_TYPE.BRAND, item));
//        getContext().startActivity(i);
//        activity.openProvidersMap(item);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnCall:
                    callPhone();
                    break;
//                case R.id.btnLocation:
//                    showNavigateToProvider();
//                    break;
            }
        } catch (Exception e) {
        }
    }

}
