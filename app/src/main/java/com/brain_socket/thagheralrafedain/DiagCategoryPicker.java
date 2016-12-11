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
    private ArrayList<CategoryModel> categories;
    private ArrayList<String> selectedCategoriesIds;
    private AppsAdapter categoriesAdapter;

    public DiagCategoryPicker(Context context,CategoryDiagCallBack callback) {
        super(context);
        this.context = context;
        this.callback = callback;
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.diag_pick_category);
        init();
        bindUserData();
    }

    private void init(){
        RecyclerView rvCategories = (RecyclerView)findViewById(R.id.rvCategories);
        Button btnDone = (Button)findViewById(R.id.btnDone);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClose(selectedCategoriesIds);
                dismiss();
            }
        });
        rvCategories.setLayoutManager(new GridLayoutManager(context, 1));
        categoriesAdapter = new AppsAdapter(context);
        rvCategories.setAdapter(categoriesAdapter);
        setTitle(context.getString(R.string.diag_category_picker_title));


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
        categories = DataStore.getInstance().getCategories();
        categoriesAdapter.updateAdapter();
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
                CategoryModel model = categories.get(index);
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
                CategoryModel model = categories.get(i);
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
            if (categories != null)
                return categories.size();
            return 0;
        }

        public void updateAdapter() {
            try {
                if(categories != null)
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