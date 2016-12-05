package com.brain_socket.thagheralrafedain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.model.BrandModel;

import java.util.ArrayList;

public class WorkshopDetails extends AppCompatActivity implements View.OnClickListener{
    private LayoutInflater inflater;
    private ArrayList<BrandModel> brands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshop_details);
        init();
    }

    private void init(){
        ListView lvBrands = (ListView)findViewById(R.id.lvBrands);


        inflater = getLayoutInflater();
        brands = DataStore.getInstance().getBrands();
        BrandsListViewAdapter brandsAdapter = new BrandsListViewAdapter();
        lvBrands.setAdapter(brandsAdapter);
    }



    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){

        }
    }

    class BrandsListViewAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return brands.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem viewHolderItem;

            if(convertView == null){
                convertView = inflater.inflate(R.layout.layout_brand_list_item,parent,false);

                viewHolderItem = new ViewHolderItem();
                viewHolderItem.ivBrand = (ImageView)convertView.findViewById(R.id.ivBrand);
                viewHolderItem.tvBrandName = (TextView)convertView.findViewById(R.id.tvBrandName);
                viewHolderItem.cbChecked = (CheckBox)convertView.findViewById(R.id.cbChecked);
                convertView.setTag(viewHolderItem);
            }
            else
                viewHolderItem = (ViewHolderItem) convertView.getTag();

            BrandModel selectedBrand = brands.get(position);
            PhotoProvider.getInstance().displayPhotoNormal(selectedBrand.getLogo(), viewHolderItem.ivBrand);
            viewHolderItem.tvBrandName.setText(selectedBrand.getName());

            return convertView;
        }
    }

    static class ViewHolderItem{
        ImageView ivBrand;
        TextView tvBrandName;
        CheckBox cbChecked;
    }
}
