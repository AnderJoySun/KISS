package fr.neamar.kiss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class IconDiskCache {
    private static final String TAG = "GetIconWorkerTask";

    private final Context context;
    private final IconPack iconPack;

    public IconDiskCache(Context context, IconPack iconPack) {
        this.iconPack = iconPack;
        this.context = context;
    }

    /**
     * create path for icons cache like this
     * {cacheDir}/icons/{icons_pack_package_name}_{key_hash}.png
     */
    private File cacheGetFileName(String key) {
        if (iconPack == null) {
            return new File(getIconsCacheDir(context) + "default_" + key.hashCode() + ".png");
        }
        return new File(getIconsCacheDir(context) + iconPack.packageName + "_" + key.hashCode() + ".png");
    }

    private File getIconsCacheDir(Context context) {
        return new File(context.getCacheDir().getPath() + "/icons/");
    }

    /**
     * Clear cache
     */
    public void cacheClear(Context context) {
        File cacheDir = getIconsCacheDir(context);

        if (!cacheDir.isDirectory())
            return;

        for (File item : cacheDir.listFiles()) {
            if (!item.delete()) {
                Log.w(TAG, "Failed to delete file: " + item.getAbsolutePath());
            }
        }
    }

    public boolean isDrawableInCache(String key) {
        File drawableFile = cacheGetFileName(key);
        return drawableFile.isFile();
    }

    public boolean cacheStoreDrawable(Context context, String key, IconPack iconPack, BitmapDrawable drawable) {
        File drawableFile = cacheGetFileName(key);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(drawableFile);
            ((BitmapDrawable) drawable).getBitmap().compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Unable to store drawable in cache " + e);
        }
        return false;
    }

    public Drawable cacheGetDrawable(String key) {
        if (!isDrawableInCache(key)) {
            return null;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(cacheGetFileName(key));
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(fis));
            fis.close();
            return drawable;
        } catch (Exception e) {
            Log.e(TAG, "Unable to get drawable from cache " + e);
        }

        return null;
    }
}
