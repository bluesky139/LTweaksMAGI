package li.lingfeng.magi.dex;

import android.content.Context;

import com.topjohnwu.superuser.Shell;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import li.lingfeng.magi.utils.FileWriter;
import li.lingfeng.magi.utils.Logger;

public class Dex extends BaseUpdate {

    public static final String PATH = BASE_FOLDER + "/MAGI.dex";

    public int checkAndUpdate(Context context) {
        try {
            if (!new File(BASE_FOLDER).exists()) {
                Shell.Result result = Shell.su("mkdir " + BASE_FOLDER
                        + " && chmod 755 " + BASE_FOLDER
                        + " && chcon u:object_r:dalvikcache_data_file:s0 " + BASE_FOLDER).exec();
                if (result.getCode() != 0) {
                    throw new Exception("Can't create " + BASE_FOLDER);
                }
            }

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
                        FileWriter.write(file, bytes);
                        status = STATUS_JUST_UPDATED;
                    } else {
                        Logger.i("Dex is already updated, len " + bytes.length);
                        status = STATUS_ALREADY_UPDATED;
                    }
                } else if ("classes2.dex".equals(name)) {
                    throw new Exception("Multi dex must be disabled.");
                }
            }
            zipFile.close();
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
