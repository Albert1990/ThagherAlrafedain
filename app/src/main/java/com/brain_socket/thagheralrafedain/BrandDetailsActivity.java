package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.CategoryModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.view.RoundedImageView;

import java.util.ArrayList;

public class BrandDetailsActivity extends AppCompatActivity implements View.OnClickListener{
    private BrandModel brand;
    private ArrayList<ProductModel> products;
    private ImageView ivBrand;
    private View ivFilter;
    private ProductsRecycleViewAdapter productsAdapter;
    private Dialog categoryPickerDialog;
    private Dialog loadingDialog;
    private View tvNoData;
    private RecyclerView rvProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_details);
        init();
        bindUserData();
    }

    private void init(){
        ivBrand = (ImageView)findViewById(R.id.ivBrand);
        rvProducts = (RecyclerView)findViewById(R.id.rvProducts);
        ivFilter = findViewById(R.id.ivFilter);
        tvNoData = findViewById(R.id.tvNoData);

        ivFilter.setOnClickListener(this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productsAdapter = new ProductsRecycleViewAdapter(this);
        rvProducts.setAdapter(productsAdapter);
        rvProducts.scheduleLayoutAnimation();

        categoryPickerDialog = new DiagCategoryPicker(this,categoryPickerCallback);
        loadingDialog = ThagherApp.getNewLoadingDilaog(this);


    }

    private DiagCategoryPicker.CategoryDiagCallBack categoryPickerCallback = new DiagCategoryPicker.CategoryDiagCallBack() {
        @Override
        public void onClose(ArrayList<String> selectedCategoriesIds) {
            loadingDialog.show();
            DataStore.getInstance().requestProducts(brand.getId(), null, selectedCategoriesIds, requestProductsCallback);
        }
    };

    private DataStore.DataRequestCallback requestProductsCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.dismiss();
            if(success){
                products = (ArrayList<ProductModel>)result.getValue("products");
                productsAdapter.updateAdapter();
            }
        }
    };

    private void bindUserData(){
        int brandIndex = getIntent().getIntExtra("brandIndex",0);
        if(brandIndex >= 0) {
            ArrayList<BrandModel> brands = DataStore.getInstance().getBrands();
            if(brands != null && brands.size() > 0) {
                brand = brands.get(brandIndex);
                loadingDialog.show();
                DataStore.getInstance().requestProducts(brand.getId(),null,null,requestProductsCallback);
                //products = brand.getProducts();
                //productsAdapter.updateAdapter();
            }
            PhotoProvider.getInstance().displayPhotoNormal(brand.getLogo(), ivBrand);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId){
            case R.id.ivFilter:
                categoryPickerDialog.show();
                break;
        }
    }

    private class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
        private LayoutInflater inflater;

        View.OnClickListener onItemClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int itemPosition = (int)v.getTag();
                    Intent myIntent = new Intent(BrandDetailsActivity.this, ProductDetailsActivity.class);
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
                if(products != null) {
                    notifyDataSetChanged();
                    if(products.size() > 0) {
                        rvProducts.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                    }else{
                        rvProducts.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                }
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
                holder.tvName.setText(productModel.getName());
                String strPrice = productModel.getPriceWithUnit();
                holder.tvPrice.setText(strPrice);
                PhotoProvider.getInstance().displayPhotoNormal(productModel.getImage(), holder.ivProduct);
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
        public ImageView ivProduct;
        public TextView tvName;
        public TextView tvPrice;

        public ProductViewHolderItem(View v) {
            super(v);
            root = v;
            tvName = (TextView) v.findViewById(R.id.tvName);
            tvPrice = (TextView) v.findViewById(R.id.tvPrice);
            ivProduct = (RoundedImageView) v.findViewById(R.id.ivProduct);
        }
    }
}
