package com.brain_socket.thagheralrafedain;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.view.RoundedImageView;

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
        ImageView ivProduct = (ImageView) findViewById(R.id.ivProduct);
        TextView tvProductName = (TextView) findViewById(R.id.tvProductName);
        TextView tvBrandName = (TextView) findViewById(R.id.tvBrandName);
        TextView tvPrice = (TextView) findViewById(R.id.tvPrice);
        WebView wvDescription = (WebView) findViewById(R.id.wvDescription);

        int selectedBrandPosition = getIntent().getIntExtra("selectedBrandPosition",-1);
        int selectedProductPosition = getIntent().getIntExtra("selectedProductPosition",-1);
        if(selectedBrandPosition >= 0 && selectedProductPosition >= 0) {
            BrandModel selectedBrand = DataStore.getInstance().getBrands().get(selectedBrandPosition);
            products = selectedBrand.getProducts();
            ProductModel selectedProduct = selectedBrand.getProducts().get(selectedProductPosition);
            PhotoProvider.getInstance().displayPhotoNormal(selectedProduct.getImage(), ivProduct);
            tvProductName.setText(selectedProduct.getName());
            tvPrice.setText(selectedProduct.getPriceWithUnit());
            tvBrandName.setText(selectedBrand.getName());
            String decodedHtml = Html.fromHtml(selectedProduct.getDescription()).toString();
            wvDescription.loadData(decodedHtml,"text/html; charset=UTF-8", null);

            setSupportActionBar(toolbar);
            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            productsAdapter = new ProductsRecycleViewAdapter(this);
            rvProducts.setAdapter(productsAdapter);
            rvProducts.scheduleLayoutAnimation();
        }

    }

    private class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
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