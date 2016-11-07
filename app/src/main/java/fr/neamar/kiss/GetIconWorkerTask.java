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
    private final IconDiskCache cache;

    public GetIconWorkerTask(Context context, IconPack iconPack,
                             ImageView target, ComponentName componentName,
                             IconDiskCache cache) {
        this.target = target;
        this.target.setTag(R.id.tag_icon_worker, componentName);
        this.iconPack = iconPack;
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.componentName = componentName;
        this.cache = cache;
    }


    @Override
    protected Drawable doInBackground(Void... params) {
        try {
            Drawable icon = null;

            // Search first in cache
            icon = cache.cacheGetDrawable(context, componentName.toString(), iconPack);
            if (icon != null) {
                return icon;
            }

            // Do we use a custom theme?
            if (iconPack != null) {
                icon = iconPack.getIcon(this.context, componentName);
            } else {
                icon = packageManager.getActivityIcon(componentName);
            }

            Log.d(TAG, "ui thread: " + Looper.getMainLooper().equals(Looper.myLooper()));

            if (icon instanceof BitmapDrawable) {
                // If the icon is a BitmapDrawable, then we can cache it!
                cache.cacheStoreDrawable(context, componentName.toString(), iconPack, (BitmapDrawable) icon);
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
}
