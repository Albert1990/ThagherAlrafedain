package com.brain_socket.thagheralrafedain;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.PhotoProvider;

import java.io.File;
import java.util.ArrayList;



public class PhotoPickerActivity extends AppCompatActivity implements OnClickListener {

    private int selectionLimit = 3;
    int checkedItemsCount = 0;

    private ArrayList<String> imageUrls;
    private ImageAdapter imageAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        init();
        Bundle bundle = getIntent().getExtras();
        String[] preSelectedImageUrls = new String[0];
        if(bundle != null) {
            if (bundle.containsKey("imgsPaths")) {
                preSelectedImageUrls = bundle.getStringArray("imgsPaths");
            }
            if (bundle.containsKey("selectionLimit"))
            {
                selectionLimit = bundle.getInt("selectionLimit");
            }
        }
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,null,orderBy + " DESC");

        // Query to get PDFs
//        Uri uri = MediaStore.Files.getContentUri("external");
//        String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
//        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
//        String[] selectionArgsPdf = new String[]{ mimeType };
//        Cursor imageCursor = getContentResolver().query(uri, projection, selectionMimeType, selectionArgsPdf, sortOrder);

        this.imageUrls = new ArrayList<>();
        for (int i = 0; i < imageCursor.getCount(); i++) {
            imageCursor.moveToPosition(i);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imageCursor.getString(dataColumnIndex));
        }

        imageAdapter = new ImageAdapter(this, imageUrls);
        if(preSelectedImageUrls != null)
            imageAdapter.setCheckedItems(preSelectedImageUrls);

        GridView gridView = (GridView) findViewById(R.id.gridGallery);
        gridView.setAdapter(imageAdapter);

        overridePendingTransition(R.anim.slide_in_from_bottom_dialog, R.anim.anim_nothing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.anim_nothing, R.anim.slide_out_to_bottom);
    }

    private void init() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        TextView tvDone = (TextView) findViewById(R.id.tvDone);
        ivBack.setOnClickListener(this);
        tvDone.setOnClickListener(this);
    }

    public class ImageAdapter extends BaseAdapter {

        ArrayList<String> mList;
        LayoutInflater mInflater;
        Context mContext;
        SparseBooleanArray mSparseBooleanArray;

        public ImageAdapter(Context context, ArrayList<String> imageList) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mSparseBooleanArray = new SparseBooleanArray();
            this.mList = imageList;

        }

        public ArrayList<String> getCheckedItems() {
            ArrayList<String> mTempArry = new ArrayList<String>();

            for (int i = 0; i < mList.size() && mTempArry.size()<10; i++) {
                if (mSparseBooleanArray.get(i)) {
                    mTempArry.add(mList.get(i));
                }
            }

            return mTempArry;
        }
        public void setCheckedItems(String[] checkedPaths){
            if(mList == null || checkedPaths == null )
                return;
            checkedItemsCount = 0;
            for (int j = 0; j < checkedPaths.length; j++) {
                for (int i = 0; i < mList.size(); i++) {
                    if (checkedPaths[j].equals("file://" +mList.get(i))) {
                        mSparseBooleanArray.put(i, true);
                        checkedItemsCount++;
                        break;
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.row_gallery, null);
                holder = new Holder();
                holder.ivImag = (ImageView) convertView.findViewById(R.id.ivImage);
                holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

//			ImageAware imageAware = new ImageViewAware(holder.ivImag , false);
//			PhotoProvider.getInstance().displayPicureForGallery("file://"+imageUrls.get(position), imageAware);

//            Uri imgUri = Uri.parse("file://" + imageUrls.get(position));
//            holder.ivImag.setImageURI(imgUri);

            PhotoProvider.getInstance().displayPhotoNormal("file://" + imageUrls.get(position), holder.ivImag);
            holder.mCheckBox.setTag(position);
            holder.mCheckBox.setOnCheckedChangeListener(null);
            holder.mCheckBox.setChecked(mSparseBooleanArray.get(position));
            holder.mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

            return convertView;
        }

        class Holder {
            ImageView ivImag;
            CheckBox mCheckBox;
        }

        OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && checkedItemsCount >= selectionLimit){
                    buttonView.setOnCheckedChangeListener(null);
                    buttonView.setChecked(false);
                    buttonView.setOnCheckedChangeListener(this);
                    return;
                }
                checkedItemsCount = isChecked?checkedItemsCount+1:checkedItemsCount-1;
                mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDone:
                returnSelectedImages();
                break;
            case R.id.ivBack:
                finish();
                break;
            default:
                break;
        }
    }

    private void returnSelectedImages() {
        ArrayList<String> selected = imageAdapter.getCheckedItems();

        String[] allPath = new String[selected.size()];
        for (int i = 0; i < allPath.length; i++) {
            allPath[i] = selected.get(i);
        }

        Intent data = new Intent().putExtra("all_path", allPath);
        setResult(RESULT_OK, data);
        finish();
    }


}