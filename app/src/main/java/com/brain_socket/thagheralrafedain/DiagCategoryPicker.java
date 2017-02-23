package com.brain_socket.thagheralrafedain;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.R;
import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.CategoryModel;
import com.brain_socket.thagheralrafedain.model.ProductModel;

import java.util.ArrayList;

/**
 * Created by Albert on 12/10/16.
 */
public class DiagCategoryPicker extends Dialog{
    private Context context;
    private CategoryDiagCallBack callback;
    private ArrayList<CategoryModel> brandCategories;
    private ArrayList<String> selectedCategoriesIds;
    private AppsAdapter categoriesAdapter;
    private BrandModel brand;

    public DiagCategoryPicker(Context context,BrandModel brand,CategoryDiagCallBack callback) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.diag_pick_category);
        this.context = context;
        this.callback = callback;
        this.brand = brand;
        init();
        bindUserData();
    }

    private void init(){
        RecyclerView rvCategories = (RecyclerView)findViewById(R.id.rvCategories);
        TextView btnDone = (TextView)findViewById(R.id.btnDone);
        TextView btnCancel = (TextView)findViewById(R.id.btnCancel);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClose(selectedCategoriesIds);
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCategoriesIds = new ArrayList<String>();
                categoriesAdapter.notifyDataSetChanged();
                callback.onClose(selectedCategoriesIds);
                dismiss();
            }
        });

        rvCategories.setLayoutManager(new GridLayoutManager(context, 1));
        categoriesAdapter = new AppsAdapter(context);
        rvCategories.setAdapter(categoriesAdapter);


        // window params
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        wmlp.verticalMargin = 0f;
        wmlp.horizontalMargin = 0f;

        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        getWindow().setAttributes(wmlp);
        wmlp.x = 0;
        wmlp.y = 0;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void bindUserData(){
        selectedCategoriesIds = new ArrayList<>();
        brandCategories = new ArrayList<CategoryModel>();
        ArrayList<CategoryModel> allCategoies = DataStore.getInstance().getCategories();
        ArrayList<String> brandCategoriesIds = brand.getCategoriesIds();
        if(brandCategoriesIds != null){
            for(int i=0;i<brandCategoriesIds.size();i++){
                CategoryModel categoryModel = getCategoryById(brandCategoriesIds.get(i),allCategoies);
                if(categoryModel != null)
                    brandCategories.add(categoryModel);
            }
        }
        categoriesAdapter.updateAdapter();
    }

    private CategoryModel getCategoryById(String id,ArrayList<CategoryModel> categories){
        CategoryModel categoryModel = null;
        for(int i=0;i<categories.size();i++){
            if(categories.get(i).getId().equals(id)){
                categoryModel = categories.get(i);
                break;
            }
        }
        return categoryModel;
    }

    public interface CategoryDiagCallBack{
        void onClose(ArrayList<String> selectedCategoriesIds);
    }

    class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;

        public AppsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        View.OnClickListener onItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = (Integer) view.getTag();
                CategoryModel model = brandCategories.get(index);
                if(selectedCategoriesIds.contains(model.getId())){
                    selectedCategoriesIds.remove(model.getId());
                }else{
                    selectedCategoriesIds.add(model.getId());
                }
                categoriesAdapter.notifyDataSetChanged();
            }
        };

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.row_category_select, viewGroup, false);
            root.setOnClickListener(onItemClick);
            AppViewHolder holder = new AppViewHolder(root);
            holder.tvCategoryName = (TextView) root.findViewById(R.id.tvCategoryName);
            holder.ivIndicator = (ImageView) root.findViewById(R.id.ivIndicator);
            holder.ivCategory = (ImageView) root.findViewById(R.id.ivCategory);
            holder.rootView = root;
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {
                CategoryModel model = brandCategories.get(i);
                viewHolder.rootView.setTag(i);
                viewHolder.tvCategoryName.setText(model.getName());
                PhotoProvider.getInstance().displayPhotoNormal(model.getIcon(), viewHolder.ivCategory);
                if(selectedCategoriesIds.contains(model.getId())) {
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
            if (brandCategories != null)
                return brandCategories.size();
            return 0;
        }

        public void updateAdapter() {
            try {
                if(brandCategories != null)
                    notifyDataSetChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategoryName;
        ImageView ivCategory, ivIndicator;
        View rootView;

        public AppViewHolder(View view) {
            super(view);
        }
    }
}