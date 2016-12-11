package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlacePicker.IntentBuilder;

import java.io.File;
import java.util.ArrayList;

public class WorkshopDetails extends AppCompatActivity implements View.OnClickListener{

    private static final int REQ_CODE_PICK_LOCATION = 445;
    public static final int REQUEST_FROM_CAMERA = 5001;
    public static final int REQUEST_FROM_GALLERY = 5002;

    private ArrayList<BrandModel> brands;
    private ArrayList<String> selectedBrandsIds;

    private EditText etFullName;
    private EditText etEmail;
    private EditText etPhone;
    private Spinner spinnerUserType;
    private EditText etAddress;
    private AppUser user;
    private Dialog loadingDialog;
    private ImageView ivLogo;
    private AppsAdapter brandsAdapter;
    private TextView btnAddress;

    boolean isFirstRegister;

    private Uri uriImg;
    private String imgPath;

    // data
    String address;
    float lat;
    float lng;

    DataStore.DataRequestCallback updateUserCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.dismiss();
            if(!success) {
                Toast.makeText(WorkshopDetails.this, R.string.err_check_connection, Toast.LENGTH_LONG).show();
            }else{
                setResult(RESULT_OK);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_details);
        init();
        bindUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_workshop_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.home:
            case android.R.id.home:
                if(!isFirstRegister)
                    finish();
                else
                    Toast.makeText(this, R.string.activity_workshop_details_missing_data_msg, Toast.LENGTH_LONG).show();
                break;
            case R.id.action_done:
                attemptUpdateUser();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if(isFirstRegister)
            return;
        super.onBackPressed();
    }

    private void init(){
        etFullName = (EditText)findViewById(R.id.etFullName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPhone = (EditText)findViewById(R.id.etPhone);
        spinnerUserType = (Spinner)findViewById(R.id.spinnerUserType);
        etAddress = (EditText) findViewById(R.id.etAddress);
        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        btnAddress = (TextView) findViewById(R.id.btnAddress);
        View fabPickPhoto = findViewById(R.id.fabPickPhoto);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvBrands);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        brandsAdapter = new AppsAdapter(this);
        recyclerView.setAdapter(brandsAdapter);

        loadingDialog = ThagherApp.getNewLoadingDilaog(this);
        user = DataStore.getInstance().getMe();
        brands = DataStore.getInstance().getBrands();
        selectedBrandsIds = new ArrayList<>();

        btnAddress.setOnClickListener(this);
        fabPickPhoto.setOnClickListener(this);
    }

    private void bindUserData(){
        if(user != null) {
            etFullName.setText(user.getFullname());
            etAddress.setText(user.getAddress());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getPhone());
            int selectedUserTypePosition = 0;
            String[] userTypes = getResources().getStringArray(R.array.workshop_array);
            for (int i = 0; i < userTypes.length; i++) {
                if (userTypes[i].substring(0,1).equalsIgnoreCase(user.getType())) {
                    selectedUserTypePosition = i;
                    break;
                }
            }
            spinnerUserType.setSelection(selectedUserTypePosition);
            if(user.getLogo() != null && !user.getLogo().isEmpty())
                PhotoProvider.getInstance().displayPhotoFade(user.getLogo(), ivLogo);

            lat = user.getLatitude()!= null?Float.valueOf(user.getLatitude()):0.0f;
            lng = user.getLongitude()!= null?Float.valueOf(user.getLongitude()):0.0f;
        }
    }

    private void attemptUpdateUser(){
        try{
            boolean cancel = false;
            View focusView = null;
            String fullName = etFullName.getText().toString();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String address = etAddress.getText().toString();
            String type = "W";
            if(spinnerUserType.getSelectedItemPosition() == 1)
                type = "S";

            etEmail.setError(null);
            etAddress.setError(null);
            etFullName.setError(null);
            etPhone.setError(null);

            if(ThagherApp.isNullOrEmpty(fullName)){
                cancel = true;
                etFullName.setError(getString(R.string.error_required_field));
                focusView = etFullName;
            }

            if(ThagherApp.isNullOrEmpty(phone)){
                cancel = true;
                etPhone.setError(getString(R.string.error_required_field));
                focusView = etPhone;
            }

            if(ThagherApp.isNullOrEmpty(email) || !ThagherApp.isValidEmail(email)){
                cancel = true;
                etEmail.setError(getString(R.string.error_required_field));
                focusView = etEmail;
            }


            if(address == null || address.isEmpty()){
                cancel = true;
                etAddress.setError(getString(R.string.error_required_field));
                focusView = etAddress;
            }

            if(lat == 0 && lng == 0){
                cancel = true;
                btnAddress.setError(getString(R.string.error_required_field));
                focusView = btnAddress;
            }

            if(!cancel){
                loadingDialog.show();
                DataStore.getInstance().attemptUpdateUser(fullName, phone, type, lat, lng, address, imgPath, selectedBrandsIds, updateUserCallback );
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void triggerPickLocation(){
        PlacePicker.IntentBuilder builder = new IntentBuilder();
        try {
            Intent i = builder.build(this);
            startActivityForResult(i, REQ_CODE_PICK_LOCATION);
        }catch (Exception e){
            Toast.makeText(this, "Please, Update yor Google play services",Toast.LENGTH_LONG).show();
        }
    }

    ///
    // ------ Pick photo ---
    ///

    /**
     *  checks for required permissions before launching photo picker
     */
    private void pickPhotos() {
        /// on android M we need to ask the user for permission at runtime to red the sd card
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionStatus = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE",
                        "android.permission.READ_EXTERNAL_STORAGE"}, 34);
            } else {
                lauchPickInvoicePhotoDiag();
            }
        } else {
            lauchPickInvoicePhotoDiag();
        }
    }

    private void lauchPickInvoicePhotoDiag() {
        final DiagPickPhoto diag;
        diag = new DiagPickPhoto(this, new DiagPickPhoto.PickDiagCallBack() {
            @Override
            public void onActionChoose(DiagPickPhoto.PickDiagActions action) {
                switch (action) {
                    case CAMERA:
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = ThagherApp.getNewFile();
                        uriImg = Uri.fromFile(f);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImg);
                        startActivityForResult(intent, REQUEST_FROM_CAMERA);
                        break;
                    case GALLERY:
                        Intent i = new Intent(WorkshopDetails.this, PhotoPickerActivity.class);
                        i.putExtra("selectionLimit", 1);
                        startActivityForResult(i, REQUEST_FROM_GALLERY);
                        break;
                    case CANCEL:
                        break;
                }
            }
        });
        diag.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_PICK_LOCATION){
            if(resultCode == RESULT_OK){
                if(data != null) {
                    Place place = PlacePicker.getPlace(this, data);
                    if (place != null) {
                        address = place.getAddress().toString();
                        lat = (float) place.getLatLng().latitude;
                        lng = (float) place.getLatLng().longitude;
                        etAddress.setText(place.getAddress());
                    }
                }
            }
        }else if(requestCode == REQUEST_FROM_GALLERY){
            if(data != null && data.hasExtra("all_path")) {
                String[] all_path = data.getStringArrayExtra("all_path");
                if (all_path != null && all_path.length > 0) {
                    for (String strUri : all_path) {
                        imgPath = strUri;
                        Uri imgUri = Uri.fromFile(new File(strUri));
                        ivLogo.setImageURI(imgUri);
                    }
                }
            }
        }else if(requestCode == REQUEST_FROM_CAMERA){
            if (uriImg != null) {
                imgPath = uriImg.getPath();
                ivLogo.setImageURI(uriImg);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.btnAddress:
                triggerPickLocation();
                break;
            case R.id.fabPickPhoto:
                pickPhotos();
                break;
        }
    }

    class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;

        public AppsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        OnClickListener onItemClick = new OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = (Integer) view.getTag();
                BrandModel type = brands.get(index);
                if(selectedBrandsIds.contains(type.getId())){
                    selectedBrandsIds.remove(type.getId());
                }else{
                    selectedBrandsIds.add(type.getId());
                }
                brandsAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.row_brand_select, viewGroup, false);
            root.setOnClickListener(onItemClick);
            AppViewHolder holder = new AppViewHolder(root);
            holder.tvBrandName = (TextView) root.findViewById(R.id.tvBrandName);
            holder.ivIndicator = (ImageView) root.findViewById(R.id.ivIndicator);
            holder.ivBrand = (ImageView) root.findViewById(R.id.ivBrand);
            holder.rootView = root;
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {
                BrandModel model = brands.get(i);
                viewHolder.rootView.setTag(i);
                viewHolder.tvBrandName.setText(model.getName());
                PhotoProvider.getInstance().displayPhotoNormal(model.getLogo(), viewHolder.ivBrand);
                if(selectedBrandsIds.contains(model.getId())) {
                    viewHolder.ivIndicator.setImageResource(R.drawable.ic_check_active);
                }else {
                    viewHolder.ivIndicator.setImageResource(R.drawable.ic_check);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (brands != null)
                return brands.size();
            return 0;
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        TextView tvBrandName;
        ImageView ivBrand, ivIndicator;
        View rootView;

        public AppViewHolder(View view) {
            super(view);
        }
    }
}
