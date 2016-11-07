package fr.neamar.kiss;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class GetIconWorkerTask extends AsyncTask<Void, Void, Drawable> {

    private static final String TAG = "GetIconWorkerTask";
    private final IconPack iconPack;
    private final PackageManager packageManager;
    private final Context context;
    private final ImageView target;
    private final ComponentName componentName;

    public GetIconWorkerTask(Context context, IconPack iconPack, ImageView target, ComponentName componentName) {
        this.target = target;
        this.target.setTag(R.id.tag_icon_worker, componentName);
        this.iconPack = iconPack;
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.componentName = componentName;
    }


    @Override
    protected Drawable doInBackground(Void... params) {
        try {
            Drawable icon = null;

            // Search first in cache
            icon = cacheGetDrawable(componentName.toString());
            if (icon != null) {
                return icon;
            }

            // Do we use a custom theme?
            if (iconPack == null) {
                icon = packageManager.getActivityIcon(componentName);
            } else {
                icon = iconPack.getIcon(this.context, componentName);
            }

            Log.d(TAG, "ui thread: " + Looper.getMainLooper().equals(Looper.myLooper()));

            if (icon instanceof BitmapDrawable) {
                // If the icon is a BitmapDrawable, then we can cache it!
                cacheStoreDrawable(componentName.toString(), (BitmapDrawable) icon);
            }

            return icon;

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("tmp", "Unable to found component " + componentName.toString() + e);
            return null;
        }
    }

    protected void onPostExecute(Drawable result) {
        if (target != null) {
            if (target.getTag(R.id.tag_icon_worker) == null || target.getTag(R.id.tag_icon_worker).equals(componentName)) {
                target.setImageDrawable(result);
                Animation myFadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                myFadeInAnimation.setDuration(150);
                target.startAnimation(myFadeInAnimation);
            }
        }

    }

    private boolean isDrawableInCache(String key) {
        File drawableFile = cacheGetFileName(key);
        return drawableFile.isFile();
    }

    private boolean cacheStoreDrawable(String key, BitmapDrawable drawable) {
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

    private Drawable cacheGetDrawable(String key) {

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

    /**
     * create path for icons cache like this
     * {cacheDir}/icons/{icons_pack_package_name}_{key_hash}.png
     */
    private File cacheGetFileName(String key) {
        if (iconPack == null) {
            return new File(getIconsCacheDir() + "default_" + key.hashCode() + ".png");
        }
        return new File(getIconsCacheDir() + iconPack.packageName + "_" + key.hashCode() + ".png");
    }

    private File getIconsCacheDir() {
        return new File(context.getCacheDir().getPath() + "/icons/");
    }

    /**
     * Clear cache
     */
    private void cacheClear() {
        File cacheDir = getIconsCacheDir();

        if (!cacheDir.isDirectory())
            return;

        for (File item : cacheDir.listFiles()) {
            if (!item.delete()) {
                Log.w(TAG, "Failed to delete file: " + item.getAbsolutePath());
            }
        }
    }
}
