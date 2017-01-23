package com.brain_socket.thagheralrafedain;

import android.Manifest.permission;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.ThagherApp.SUPPORTED_LANGUAGE;
import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.fragments.FragMap.MAP_TYPE;
import com.brain_socket.thagheralrafedain.model.AppUser;
import com.brain_socket.thagheralrafedain.model.AppUser.USER_TYPE;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;
import com.brain_socket.thagheralrafedain.view.RoundedImageView;
import com.brain_socket.thagheralrafedain.view.TextViewCustomFont;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataStore.DataStoreUpdateListener, OnClickListener {
    private SliderAdapter brandsSliderAdapter;
    private ProductsRecycleViewAdapter productsAdapter;
    private ArrayList<ProductModel> products;
    private ArrayList<BrandModel> brands;
    private BrandModel selectedBrand;
    private ViewPager vpBrands;
    private Dialog loadingDialog;

    // nav drawer
    private ImageView ivNavHeader;
    private TextView txtNotLoggedIn;
    private TextView btnLogin;
    private TextView btnWorkshopProfile;
    private TextView btnLogout;
    private TextView btnChangePsw;
    private View btnArabic;
    private View btnEnglish;
    private View vLoggedInOptionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if(ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ThagherApp.requestLastUserKnownLocation();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);

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
//            case R.id.action_profile:
//                actionProfileClicked();
//                break;
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

            // drawer
            ivNavHeader = (ImageView) findViewById(R.id.ivNavHeader);
            vLoggedInOptionsContainer = findViewById(R.id.vLoggedInOptions);
            btnLogin = (TextView) findViewById(R.id.btnLogin);
            btnWorkshopProfile = (TextView) findViewById(R.id.btnWorkshop);
            btnLogout = (TextView) findViewById(R.id.btnLogout);
            btnChangePsw = (TextView) findViewById(R.id.btnChangePsw);
            btnArabic = findViewById(R.id.btnArabic);
            btnEnglish= findViewById(R.id.btnEnglish);
            txtNotLoggedIn = (TextView) findViewById(R.id.txtNotLoggedIn);

            brandsSliderAdapter = new SliderAdapter();
            vpBrands.setAdapter(brandsSliderAdapter);
            vpBrands.setPageMargin(ThagherApp.getPXSize(0));
            vpBrands.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageSelected(int position) {
                    position++;
                    if(position <= brands.size()) {
                        selectedBrand = brands.get(position);
                        products = brands.get(position).getProducts();
                        productsAdapter.updateAdapter();
                    }
                }
                @Override
                public void onPageScrollStateChanged(int state) {}
            });

            rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
            productsAdapter = new ProductsRecycleViewAdapter(this);
            rvProducts.setAdapter(productsAdapter);
            rvProducts.scheduleLayoutAnimation();

            btnArabic.setOnClickListener(this);
            btnEnglish.setOnClickListener(this);
            btnWorkshopProfile.setOnClickListener(this);
            btnChangePsw.setOnClickListener(this);
            btnLogout.setOnClickListener(this);
            btnLogin.setOnClickListener(this);

            loadingDialog.show();
            DataStore.getInstance().requestBrandsWithProducts(requestBrandsWithProductsCallback);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            // configure drawer content
            AppUser me = DataStore.getInstance().getMe();
            if (me != null) {
                vLoggedInOptionsContainer.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                txtNotLoggedIn.setVisibility(View.GONE);
                if (me.getUserType() == USER_TYPE.USER) {
                    btnWorkshopProfile.setText(R.string.settings_upgrade_to_workshop);
                } else {
                    btnWorkshopProfile.setText(R.string.settings_edit_workshop);
                }
                PhotoProvider.getInstance().displayPhotoFade(me.getLogo(), ivNavHeader);
            } else { // not logged in
                ivNavHeader.setImageResource(R.drawable.login_logo);
                vLoggedInOptionsContainer.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
                txtNotLoggedIn.setVisibility(View.VISIBLE);
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    supportInvalidateOptionsMenu();
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {}
            };

//            drawer.addDrawerListener(toggle);
            toggle.syncState();

            // hide default app logo and name on the left of the toolbar
            final ActionBar ab = getSupportActionBar();
            ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
            ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)



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

    private void changeLanguage(SUPPORTED_LANGUAGE newLang) {
        try {
            if (newLang != null) {
                if (newLang != ThagherApp.getCurrentLanguage()) {
                    // to make sure fonts will be recreated for the new language
                    TextViewCustomFont.resetFonts();
                    DataStore.getInstance().triggerDataUpdate();
                    ThagherApp.setLanguage(newLang);
                    DataStore.getInstance().broadcastLanguageChange();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataStoreUpdate() {
        updateBody();
    }

    @Override
    public void onLanguageChanged() {
        recreate();
        //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
    }

    @Override
    public void onLoginStateChange() {
        AppUser me = DataStore.getInstance().getMe();
        if(me!= null){

        }
        recreate();
        //invalidateOptionsMenu();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnArabic:
                changeLanguage(SUPPORTED_LANGUAGE.AR);
                break;
            case R.id.btnEnglish:
                changeLanguage(SUPPORTED_LANGUAGE.EN);
                break;
            case R.id.btnChangePsw:
                Intent iChangePsw = new Intent(this, ChangePswActivity.class);
                startActivity(iChangePsw);
                break;
            case R.id.btnLogout:
                DataStore.getInstance().logout();
                DataStore.getInstance().triggerDataUpdate();
                recreate();
                break;
            case R.id.btnWorkshop:
                Intent i = new Intent(this, WorkshopDetails.class);
                startActivity(i);
                break;
            case R.id.btnLogin:
                Intent intrent = new Intent(this, LoginActivity.class);
                startActivity(intrent);
                break;
        }
    }

    private class ProductsRecycleViewAdapter extends RecyclerView.Adapter<ProductViewHolderItem> {
        private LayoutInflater inflater;

        View.OnClickListener onItemClickListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int itemPosition = (int) v.getTag();
                    Intent myIntent = new Intent(MainActivity.this, ProductDetailsActivity.class);
                    if(selectedBrand != null)
                    {
                    ProductModel selectedProduct = selectedBrand.getProducts().get(itemPosition);
                        if(selectedProduct != null){
                            myIntent.putExtra("selectedBrand", selectedBrand.getJsonString());
                            myIntent.putExtra("selectedProduct", selectedProduct.getJsonString());
                            startActivity(myIntent);
                        }
                    }
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

    class SliderAdapter extends PagerAdapter implements View.OnClickListener {
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
                v.setTag(position);
                v.setOnClickListener(this);

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

        @Override
        public void onClick(View v) {
            int selectedBrandIndex = (Integer) v.getTag();
            //selectedBrandIndex--;
            selectedBrandIndex = selectedBrandIndex < 0 ? 0 : selectedBrandIndex;
            Intent i = new Intent(MainActivity.this,BrandDetailsActivity.class);
            i.putExtra("brand",brands.get(selectedBrandIndex).getJsonString());
            startActivity(i);
        }
    }
}