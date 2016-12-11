package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract.Data;
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
import com.brain_socket.thagheralrafedain.fragments.FragMap.MAP_TYPE;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.AppUser.USER_TYPE;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.view.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataStore.DataStoreUpdateListener {
    private SliderAdapter brandsSliderAdapter;
    private ProductsRecycleViewAdapter productsAdapter;
    private ArrayList<ProductModel> products;
    private ArrayList<BrandModel> brands;
    private int selectedBrandPosition = 0;
    private ViewPager vpBrands;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_home, menu);
//        AppUser me = DataStore.getInstance().getMe();
//        if(me == null) {
//            getMenuInflater().inflate(R.menu.menu_home, menu);
//        }else if(me.getUserType() == USER_TYPE.USER){
//            getMenuInflater().inflate(R.menu.menu_home_no_profile, menu);
//        }else
//            getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    private void actionProfileClicked(){
        try {
            AppUser user = DataStore.getInstance().getMe();
            Intent i =null;
            if(user == null) {
                i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
            }else{
                i = new Intent(MainActivity.this,WorkshopDetails.class);
                startActivity(i);
            }
//            Intent i = new Intent(MainActivity.this,WorkshopDetails.class);
//            startActivity(i);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
                actionProfileClicked();
                break;
            case R.id.action_workshops:
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                mapIntent.putExtras(MapActivity.getLauncherBundle(MAP_TYPE.SEARCH, null));
                startActivity(mapIntent);
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void init() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
            loadingDialog = ThagherApp.getNewLoadingDilaog(this);
            RecyclerView rvProducts = (RecyclerView) findViewById(R.id.rvProducts);
            vpBrands = (ViewPager) findViewById(R.id.vpBrands);

            //brands = DataStore.getInstance().getBrands();
            //addDummyCards2Brands();
            brandsSliderAdapter = new SliderAdapter();
            vpBrands.setAdapter(brandsSliderAdapter);
            vpBrands.setPageMargin(ThagherApp.getPXSize(0));
            vpBrands.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    position++;
                    if(position <= brands.size()) {
                        selectedBrandPosition = position;
                        products = brands.get(position).getProducts();
                        productsAdapter.updateAdapter();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            productsAdapter = new ProductsRecycleViewAdapter(this);
            rvProducts.setAdapter(productsAdapter);
            rvProducts.scheduleLayoutAnimation();

            loadingDialog.show();
            DataStore.getInstance().requestBrandsWithProducts(requestBrandsWithProductsCallback);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BrandModel getDummyBrandCard(){
        BrandModel dummyBrandModel = new BrandModel();
        dummyBrandModel.setId("-1");
        dummyBrandModel.setName("dummy");
        dummyBrandModel.setProducts(null);
        return dummyBrandModel;
    }

    private void addDummyCards2Brands(){
        ArrayList<BrandModel> tempBrands = new ArrayList<>();
        tempBrands.add(getDummyBrandCard());
        tempBrands.addAll(brands);
        tempBrands.add(getDummyBrandCard());
        brands = tempBrands;
    }

    DataStore.DataRequestCallback requestBrandsWithProductsCallback = new DataStore.DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            loadingDialog.dismiss();
            if (success) {
                brands = (ArrayList<BrandModel>) result.getValue("brands");
                addDummyCards2Brands();
                brandsSliderAdapter.updateAdapter();
                if (brands != null && brands.size() > 0) {
                    int middleBrand = (int) Math.floor(brands.size() / 2);
                    if(middleBrand <= brands.size())
                        vpBrands.setCurrentItem(middleBrand,true);
                }
            }
        }
    };

    public void setActionBarColor(int color) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
    }

    private void updateBody() {
        //brands = DataStore.getInstance().getBrands();
        //brandsSliderAdapter.updateAdapter();
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
        AppUser me = DataStore.getInstance().getMe();
        if(me!= null){

        }
        invalidateOptionsMenu();
    }

    class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
        private LayoutInflater inflater;

        View.OnClickListener onItemClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int itemPosition = (int) v.getTag();
                    Intent myIntent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                    myIntent.putExtra("selectedBrandPosition", selectedBrandPosition);
                    myIntent.putExtra("selectedProductPosition", itemPosition);
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
                if (products != null)
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
                ImageView ivBrand = (ImageView) v.findViewById(R.id.ivBrand);
                TextView tvBrandName = (TextView) v.findViewById(R.id.tvBrandName);
                TextView tvProductsCount = (TextView) v.findViewById(R.id.tvProductsCount);
                TextView tvCategory = (TextView) v.findViewById(R.id.tvCategory);

                BrandModel selectedBrand = brands.get(position);
                tvBrandName.setText(selectedBrand.getName());
                PhotoProvider.getInstance().displayPhotoNormal(selectedBrand.getLogo(), ivBrand);
                tvProductsCount.setText(Integer.toString(selectedBrand.getProducts().size()));
                container.addView(v);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return v;
        }

        @Override
        public int getCount() {
            if (brands == null)
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