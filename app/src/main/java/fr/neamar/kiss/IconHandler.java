package fr.neamar.kiss;

import android.content.Context;


public class IconHandler {

    private static final String TAG = "IconHandler";
    private final IconMemoryCache memoryCache = new IconMemoryCache();

    public IconHandler(Context ctx) {
        super();
        memoryCache.setLimit(1024 * 1024 * 2); // 2MB should be more than enough
    }
}


