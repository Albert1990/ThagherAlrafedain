package com.brain_socket.thagheralrafedain;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.model.ProductModel;

import java.util.ArrayList;

public class ProductDetailsActivity extends AppCompatActivity {
    private ProductsRecycleViewAdapter productsAdapter;
    private ArrayList<ProductModel> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        init();
    }

    private void init(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.product_details_bar);
        RecyclerView rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
        WebView wvDescription = (WebView) findViewById(R.id.wvDescription);

        setSupportActionBar(toolbar);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productsAdapter = new ProductsRecycleViewAdapter(this);
        rvProducts.setAdapter(productsAdapter);
        rvProducts.scheduleLayoutAnimation();

        wvDescription.loadData("<h2>Hi soso</h2>","text/html; charset=UTF-8", null);
    }

    class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
        private LayoutInflater inflater;

        View.OnClickListener onItemClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int itemPosition = (int)v.getTag();
                    Intent myIntent = new Intent(ProductDetailsActivity.this, ProductDetailsActivity.class);
                    //myIntent.putExtra("key", value); //Optional parameters
                    startActivity(myIntent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        public ProductsRecycleViewAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void updateAdapter() {
            try {
                if(products != null)
                    notifyDataSetChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public ProductViewHolderItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = inflater.inflate(R.layout.layout_product_card, parent, false);
            ProductViewHolderItem holder = new ProductViewHolderItem(root);
            root.setOnClickListener(onItemClickListner);
            return holder;
        }

        @Override
        public void onBindViewHolder(ProductViewHolderItem holder, int position) {
            try {
                final ProductModel productModel = products.get(position);
                holder.root.setTag(position);
                holder.tvName.setText(productModel.getEnglishName());
                String strPrice = productModel.getPrice()+"$";
                holder.tvPrice.setText(strPrice);
                //PhotoProvider.getInstance().displayPhotoNormal(application.getPhoto(), viewHolder.ivIcon);
                //viewHolder.ivIcon.setTag(i);
//                if(selectedCategoriesIds.contains(application.getId())){
//                    viewHolder.tvName.setAlpha(1f);
//                    viewHolder.ivIcon.setAlpha(1f);
//                }else{
//                    viewHolder.tvName.setAlpha(0.5f);
//                    viewHolder.ivIcon.setAlpha(0.5f);
//                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (products == null)
                return 0;
            return products.size();
        }
    }

    static class ProductViewHolderItem extends RecyclerView.ViewHolder {
        public View root;
        public ImageView img;
        public TextView tvName;
        public TextView tvPrice;

        public ProductViewHolderItem(View v) {
            super(v);
            root = v;
            tvName = (TextView) v.findViewById(R.id.tvName);
            tvPrice = (TextView) v.findViewById(R.id.tvPrice);
        }
    }
}