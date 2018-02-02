package run.wallet.common;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by coops on 21/12/14.
 */
public class ImageCache {
    private static final ImageCache IC=new ImageCache();
    private Map<String,CacheBitmap> cache = new ConcurrentHashMap<String,CacheBitmap>();
    private List<String> cacheorder = new ArrayList<String>();
    public static final int CACHE_B_LOADING=0;
    public static final int CACHE_B_LOADED=1;

    public class CacheBitmap {
        public int status;
        public Bitmap bitmap;
    }
    public static ImageCache getImageCache() {
        return IC;
    }
    public static CacheBitmap getNewCacheBitmap() {
        return IC.new CacheBitmap();
    }
    public static void put(String path, CacheBitmap image) {
        IC.cache.put(path,image);
    }
    public static void putFinal(String path, CacheBitmap image) {
        IC.cache.put(path,image);
        ImageCache.addOrder(path);
        ImageCache.trimCache();
    }
    public static void addOrder(String path) {
        IC.cacheorder.add(path);
    }
    public static CacheBitmap get(String path) {
        return IC.cache.get(path);
    }
    public static Map<String,CacheBitmap> get() {
        return IC.cache;
    }
    public static List<String> getOrder() {
        return IC.cacheorder;
    }
    public static void clearCache() {
        IC.cache.clear();
    }
    private final int MAX_CACHE=40;
    public static synchronized void trimCache() {
        if(IC.cacheorder.size()>IC.MAX_CACHE) {
            String rem = IC.cacheorder.remove(0);
            IC.cache.remove(rem);
        }

    }
}
