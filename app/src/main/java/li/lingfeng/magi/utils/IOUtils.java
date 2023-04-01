package li.lingfeng.magi.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;

import li.lingfeng.magi.Loader;
import li.lingfeng.magi.dex.Assets;

public class IOUtils {

    public static byte[] openAssetAsBytes(String path) throws Throwable {
        InputStream stream = Loader.getApplication().getAssets().open(path);
        byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
        stream.close();
        return bytes;
    }

    public static byte[] openLAssetAsBytes(String path) throws Throwable {
        return FileUtils.readFileToByteArray(new File(Assets.FOLDER + '/' + path));
    }

    public static Drawable openLAssetAsDrawable(String path) throws Throwable {
        byte[] bytes = openLAssetAsBytes(path);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return new BitmapDrawable(bitmap);
    }
}
