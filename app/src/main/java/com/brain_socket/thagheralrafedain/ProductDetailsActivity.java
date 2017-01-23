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
import android.view.MenuItem;
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
    private BrandModel selectedBrand;
    private RecyclerView rvProducts;
    private ImageView ivProduct;
    private TextView tvProductName;
    private TextView tvBrandName;
    private TextView tvPrice;
    //private WebView wvDescription;
    private TextView tvDescription;
    private TextView tvTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        init();
        bindUserData();
    }

    private void init(){
        try {
            Toolbar toolbar = (Toolbar)findViewById(R.id.product_details_bar);
            rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
            ivProduct = (ImageView) findViewById(R.id.ivProduct);
            tvProductName = (TextView) findViewById(R.id.tvProductName);
            tvBrandName = (TextView) findViewById(R.id.tvBrandName);
            tvPrice = (TextView) findViewById(R.id.tvPrice);
            //wvDescription = (WebView) findViewById(R.id.wvDescription);
            tvDescription = (TextView) findViewById(R.id.tvDescription);
            tvTitle = (TextView) findViewById(R.id.tvTitle);

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void bindUserData(){
        try{
            selectedBrand = BrandModel.fromJsonString(getIntent().getStringExtra("selectedBrand"));
            ProductModel selectedProduct = ProductModel.fromJsonString(getIntent().getStringExtra("selectedProduct"));

            if(selectedBrand != null && selectedProduct != null) {
                products = selectedBrand.getProducts();

                // fill data in views
                PhotoProvider.getInstance().displayPhotoNormal(selectedProduct.getImage(), ivProduct);
                tvProductName.setText(selectedProduct.getName());
                tvPrice.setText(selectedProduct.getPriceWithUnit());
                tvBrandName.setText(selectedBrand.getName());
                tvTitle.setText(selectedProduct.getName());
                // WebView
                //String decodedHtml = Html.fromHtml(selectedProduct.getDescription()).toString();
                //wvDescription.loadData(decodedHtml,"text/html; charset=UTF-8", null);
                tvDescription.setText(selectedProduct.getDescription());

                rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
                productsAdapter = new ProductsRecycleViewAdapter(this);
                rvProducts.setAdapter(productsAdapter);
                rvProducts.scheduleLayoutAnimation();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
        private LayoutInflater inflater;

        View.OnClickListener onItemClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int itemPosition = (int)v.getTag();
                    Intent myIntent = new Intent(ProductDetailsActivity.this, ProductDetailsActivity.class);
                    ProductModel selectedProduct = selectedBrand.getProducts().get(itemPosition);
                    if(selectedProduct != null){
                        myIntent.putExtra("selectedBrand", selectedBrand.getJsonString());
                        myIntent.putExtra("selectedProduct", selectedProduct.getJsonString());
                        startActivity(myIntent);
                    }
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
                holder.tvName.setText(productModel.getName().toUpperCase());
                String strPrice = productModel.getPriceWithUnit();
                holder.tvPrice.setText(R.string.main_prod_view_product);
                holder.tvBrand.setText(getString(R.string.main_prod_price) + strPrice);
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
        public TextView tvBrand;

        public ProductViewHolderItem(View v) {
            super(v);
            root = v;
            tvName = (TextView) v.findViewById(R.id.tvName);
            tvPrice = (TextView) v.findViewById(R.id.tvPrice);
            tvBrand = (TextView) v.findViewById(R.id.tvBrand);
            ivProduct = (RoundedImageView) v.findViewById(R.id.ivProduct);
        }
    }
}