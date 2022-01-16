package li.lingfeng.magi.dex;

import android.content.Context;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import li.lingfeng.magi.utils.Logger;

public class Dex {

    public static final String FOLDER = "/data/local/LTweaksMAGI";
    public static final String PATH = FOLDER + "/MAGI.dex";

    public static final int STATUS_ALREADY_UPDATED = 0;
    public static final int STATUS_JUST_UPDATED = 1;
    public static final int STATUS_ERROR = 2;

    public int checkAndUpdate(Context context) {
        try {
            File file = new File(PATH);
            byte[] oldBytes = null;
            if (file.exists()) {
                oldBytes = FileUtils.readFileToByteArray(file);
            }
            int status = -1;
            String apkPath = context.getPackageCodePath();
            ZipFile zipFile = new ZipFile(apkPath);
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if ("classes.dex".equals(name)) {
                    byte[] bytes = IOUtils.toByteArray(zipFile.getInputStream(entry));
                    if (!Arrays.equals(oldBytes, bytes)) {
                        Logger.i("Extract classes.dex to " + PATH + ", len " + bytes.length);
                        FileUtils.writeByteArrayToFile(file, bytes);
                        status = STATUS_JUST_UPDATED;
                    } else {
                        Logger.i("Dex is already updated, len " + bytes.length);
                        status = STATUS_ALREADY_UPDATED;
                    }
                } else if ("classes2.dex".equals(name)) {
                    throw new Exception("Multi dex must be disabled.");
                }
            }
            if (status == -1) {
                throw new Exception("No classes.dex?");
            }
            return status;
        } catch (Throwable e) {
            Logger.e("Dex check and update exception.", e);
            return STATUS_ERROR;
        }
    }
}
