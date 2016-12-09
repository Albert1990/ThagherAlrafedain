package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.CheckableBrandModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;

import org.json.JSONObject;

import java.util.ArrayList;

public class WorkshopDetails extends AppCompatActivity implements View.OnClickListener{
    private ArrayList<BrandModel> brands;
    private ArrayList<String> selectedBrandsIds;
    private EditText etFullName;
    private EditText etPhone;
    private Spinner spinnerUserType;
    private EditText etAddress;
    private AppUser user;
    private Dialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_details);
        init();
    }

    private void init(){
        etFullName = (EditText)findViewById(R.id.etFullName);
        etPhone = (EditText)findViewById(R.id.etPhone);
        spinnerUserType = (Spinner)findViewById(R.id.spinnerUserType);
        etAddress = (EditText)findViewById(R.id.etAddress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvBrands);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AppsAdapter(this));

        loadingDialog = ThagherApp.getNewLoadingDilaog(this);
        user = DataStore.getInstance().getMe();
        ArrayList<BrandModel> brands = DataStore.getInstance().getBrands();
        selectedBrandsIds = new ArrayList<>();
    }

    private void bindUserData(){
        etFullName.setText(user.getFullname());
        etPhone.setText(user.getPhone());
        int selectedUserTypePosition = 0;
        String[] userTypes = getResources().getStringArray(R.array.workshop_array);
        for(int i = 0;i < userTypes.length;i++) {
            if (userTypes[i].equals(user.getType())) {
                selectedUserTypePosition = i;
                break;
            }
        }
        spinnerUserType.setSelection(selectedUserTypePosition);
        etAddress.setText(user.getAddress());
    }

    DataStore.DataRequestCallback updateUserCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {

        }
    };

    private void attemptUpdateUser(){
        try{
            boolean cancel = false;
            View focusView = null;
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String type =  (String)spinnerUserType.getSelectedItem();

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

            if(!cancel)
            {
                loadingDialog.show();
                DataStore.getInstance().attemptUpdateUser(fullName,phone,type,updateUserCallback);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case 1:
                attemptUpdateUser();
                break;
        }
    }

    class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;

        public AppsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.row_brand_select, viewGroup, false);
            root.setTag(i);
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
                viewHolder.tvBrandName.setText(model.getName());
                viewHolder.ivIndicator.setBackgroundResource(R.drawable.ic_check);
                viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView ivIndicator = (ImageView)v.findViewById(R.id.ivIndicator);
                        int position = (int) v.getTag();
                        BrandModel selectedBrand = brands.get(position);
                        if(selectedBrandsIds.contains(selectedBrand.getId())) {
                            selectedBrandsIds.remove(selectedBrand.getId());
                            ivIndicator.setBackgroundResource(R.drawable.ic_check);
                        }
                        else {
                            selectedBrandsIds.add(selectedBrand.getId());
                            ivIndicator.setBackgroundResource(R.drawable.ic_check_active);
                        }
                        selectedBrandsIds.add(selectedBrand.getId());
                    }
                });
                PhotoProvider.getInstance().displayPhotoNormal(model.getLogo(), viewHolder.ivBrand);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return brands.size();
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
