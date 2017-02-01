package com.brain_socket.thagheralrafedain.data;

import android.widget.ImageView;

import com.brain_socket.thagheralrafedain.R;
import com.brain_socket.thagheralrafedain.ThagherApp;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

import java.io.File;
import java.io.IOException;

public class PhotoProvider
{

    public static long CACHE_DURATION = 7*24*3600*1000; // 1 week

    private static PhotoProvider photoProvider = null;
    // Image loader library objects
    private static ImageLoader imageLoader = null;
    private DisplayImageOptions optionsFade, optionsNormal, optionsProfilePicture, optionsCover;

    private PhotoProvider()
    {
        initImageLoader();
    }

    public static PhotoProvider getInstance()
    {
        if(photoProvider == null) {
            photoProvider = new PhotoProvider();
        }
        return photoProvider;
    }

    /**
     * Initializes the Image Loader singleton, and sets up the display
     * options used in the app
     */
    void initImageLoader()
    {
        try {
            File cacheDir = ThagherApp.getAppContext().getExternalCacheDir();
            if (cacheDir == null){//cache dir not available
                cacheDir = ThagherApp.getAppContext().getCacheDir();
            }
            File thisCacheDir = new File(cacheDir.getPath(), "Sind");
            if ((!thisCacheDir.isDirectory()) || (thisCacheDir.isFile())){
                thisCacheDir.delete();
                thisCacheDir.mkdir();
            }

            try {
                File file = new File(thisCacheDir, ".nomedia");
                file.createNewFile();
            }
            catch (IOException ignored) {}


            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(ThagherApp.getAppContext());
            builder.denyCacheImageMultipleSizesInMemory();
            int limit = (int) (Runtime.getRuntime().maxMemory()/10);
            builder.memoryCache(new UsingFreqLimitedMemoryCache(limit));
//			builder.memoryCache(new UsingFreqLimitedMemoryCache(2000000));
            builder.discCache(new UnlimitedDiskCache(thisCacheDir));
            try {
                L.disableLogging();} catch (Exception ignored) {}

            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

            checkLastCacheClearTimestamp();

            optionsFade = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.shape_transparent)
                    .showImageForEmptyUri(R.drawable.shape_transparent)
                    .showImageOnFail(R.drawable.shape_transparent)
                    .considerExifParams(true)
                    .delayBeforeLoading(100)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();

            optionsNormal = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.shape_cover_place_holder)
                    .showImageForEmptyUri(R.drawable.shape_transparent)
                    .showImageOnFail(R.drawable.shape_transparent)
                    .considerExifParams(true)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .build();

            optionsProfilePicture = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.shape_transparent)
                    .showImageForEmptyUri(R.drawable.shape_transparent)
                    .showImageOnFail(R.drawable.shape_transparent)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .considerExifParams(true)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .build();

            optionsCover = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.shape_cover_place_holder)
                    .showImageForEmptyUri(R.drawable.shape_cover_place_holder)
                    .showImageOnFail(R.drawable.shape_cover_place_holder)
                    .considerExifParams(true)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .build();
        }
        catch (Exception ignored) {}
    }

    /**
     * Loads the photo and displays it in the ImageView passed,
     * using the Fade options
     * @param photoUrl
     * The url of the photo to be displayed
     * @param iv
     * The ImageView holding the photo
     */
    public void displayPhotoFade(final String photoUrl, final ImageView iv)
    {
        try {
            if(imageLoader == null) {
                initImageLoader();
            }

            imageLoader.displayImage(photoUrl, iv, optionsFade);
        }
        catch (Exception ignored) {}
    }

    /**
     * Loads the photo and displays it in the ImageView passed,
     * using the Normal options
     * @param photoUrl
     * The url of the photo to be displayed
     * @param iv
     * The ImageView holding the photo
     */
    public void displayPhotoNormal(final String photoUrl, final ImageView iv)
    {
        try {
            if(imageLoader == null) {
                initImageLoader();
            }

            iv.setImageResource(R.drawable.shape_cover_place_holder);
            imageLoader.displayImage(photoUrl, iv, optionsNormal);
        }
        catch (Exception ignored) {}
    }

    public void displayProfilePicture(final String photoUrl, final ImageView iv){
        try {
            if(imageLoader == null) {
                initImageLoader();
            }

            imageLoader.displayImage(photoUrl, iv, optionsProfilePicture);
        }
        catch (Exception ignored) {}
    }

    public void displayCoverPhoto(final String photoUrl, final ImageView iv){
        try {
            if(imageLoader == null) {
                initImageLoader();
            }
            imageLoader.displayImage(photoUrl, iv, optionsCover);
        }catch (Exception ignored) {}
    }

    /**
     * Loads the Facebook Photo and displays it in the given {@link android.widget.ImageView},
     * using the Normal display options
     * @param id : the id of the Facebook user
     * @param iv : the {@link android.widget.ImageView} holding the photo
     * @param dimen : the dimension x*y of square photo to be displayed
     */
    public void displayFacebookThumbnailPhoto(final String id, final ImageView iv, final int dimen)
    {
        try {
            String photoUrl = "https://graph.facebook.com/" + id + "/picture?width=" + dimen + "&height=" + dimen;
            displayPhotoNormal(photoUrl, iv);
        }
        catch (Exception ignored) {}
    }


    /**
     * It will check the date of the last time the cache was cleared. If it
     * is more than a week, a full cache clear will run.
     */
    private void checkLastCacheClearTimestamp()
    {
        try {
            long timestamp = DataCacheProvider.getInstance().getStoredPhotoClearedCacheTimestamp();
            long timenow = ThagherApp.getTimestampNow();
            // compare times
            if(timenow > timestamp + CACHE_DURATION) {
                // clear cache
                clearCache();
                // store new timetsamp
                DataCacheProvider.getInstance().storePhotoClearedCacheTimestamp(timenow);
            }
        }
        catch (Exception ignored) {}

    }

    /**
     * Called to run a full clear of cache (memory and disc)
     */
    public void clearCache()
    {
        try {
            if(imageLoader == null) {
                initImageLoader();
            }

            imageLoader.clearDiscCache();
            imageLoader.clearMemoryCache();
        }
        catch (Exception ignored) {}
    }


}