package com.brain_socket.thagheralrafedain;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataStore.DataStoreUpdateListener {
    private SliderAdapter brandsSliderAdapter;
    private ProductsRecycleViewAdapter productsAdapter;
    private ArrayList<ProductModel> products;
    private ArrayList<BrandModel> brands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.home:
            case android.R.id.home:
                finish();
                break;
            case R.id.action_profile:
                try{
                    Intent i =new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(i);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                break;
            case R.id.action_workshops:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void init() {
        try {
            Toolbar toolbar = (Toolbar)findViewById(R.id.app_bar);
            RecyclerView rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
            ViewPager vpBrands = (ViewPager) findViewById(R.id.vpBrands);

            brands = DataStore.getInstance().getBrands();
            brandsSliderAdapter = new SliderAdapter();
            vpBrands.setAdapter(brandsSliderAdapter);
            vpBrands.setPageMargin(ThagherApp.getPXSize(15));
            vpBrands.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    DataStore.getInstance().requestBrandProducts(brands.get(position).getId(),requestBrandProductsCallback);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            productsAdapter = new ProductsRecycleViewAdapter(this);
            rvProducts.setAdapter(productsAdapter);
            rvProducts.scheduleLayoutAnimation();

            setSupportActionBar(toolbar);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DataStore.DataRequestCallback requestBrandProductsCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            try{
                products = (ArrayList<ProductModel>) result.getValue("products");
                productsAdapter.updateAdapter();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };

    public void setActionBarColor(int color) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
    }

    private void updateBody() {
        brands = DataStore.getInstance().getBrands();
        brandsSliderAdapter.updateAdapter();
        productsAdapter.updateAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataStore.getInstance().addUpdateBroadcastListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataStore.getInstance().removeUpdateBroadcastListener(this);
    }

    @Override
    public void onDataStoreUpdate() {
        updateBody();
    }

    @Override
    public void onNewEventNotificationsAvailable() {

    }

    @Override
    public void onLoginStateChange() {

    }

    class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
        private LayoutInflater inflater;

        View.OnClickListener onItemClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int itemPosition = (int)v.getTag();
                    Intent myIntent = new Intent(MainActivity.this, ProductDetailsActivity.class);
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

    class SliderAdapter extends PagerAdapter {
        private Context context;
        private LayoutInflater inflater;

        public SliderAdapter() {
            this.context = ThagherApp.getAppContext();
            this.inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        }

        public void updateAdapter() {
            try {
                if (brands != null)
                    notifyDataSetChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = null;
            try {
                v = inflater.inflate(R.layout.layout_brand_card, container, false);
                ImageView ivBrand = (ImageView)v.findViewById(R.id.ivBrand);
                TextView tvBrandName = (TextView)v.findViewById(R.id.tvBrandName);

                tvBrandName.setText(brands.get(position).getEnglishName());
                PhotoProvider.getInstance().displayPhotoNormal(brands.get(position).getLogo(), ivBrand);
                container.addView(v);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return v;
        }

        @Override
        public int getCount() {
            if(brands == null)
                return 0;
            return brands.size();
        }

        @Override
        public float getPageWidth(int position) {
            return 0.333f;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

    }
}