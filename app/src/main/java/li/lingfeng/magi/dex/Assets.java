package li.lingfeng.magi.dex;

import android.content.Context;

import com.topjohnwu.superuser.Shell;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;

import li.lingfeng.magi.utils.FileWriter;
import li.lingfeng.magi.utils.IOUtils;
import li.lingfeng.magi.utils.Logger;

public class Assets extends BaseUpdate {

    public static final String FOLDER = BASE_FOLDER + "/assets";

    public int checkAndUpdate(Context context) {
        try {
            if (!new File(FOLDER).exists()) {
                Shell.Result result = Shell.su("mkdir " + FOLDER
                        + " && chmod 755 " + FOLDER
                        + " && chcon u:object_r:dalvikcache_data_file:s0 " + FOLDER).exec();
                if (result.getCode() != 0) {
                    throw new Exception("Can't create " + FOLDER);
                }
            }

            int status = STATUS_ALREADY_UPDATED;
            String[] names = context.getAssets().list("extract");
            for (String name : names) {
                File file = new File(FOLDER + '/' + name);
                byte[] newBytes = IOUtils.openAssetAsBytes("extract/" + name);
                byte[] oldBytes = file.exists() ? FileUtils.readFileToByteArray(file) : null;
                if (!ArrayUtils.isEquals(newBytes, oldBytes)) {
                    Logger.i("Extract asset " + name);
                    FileWriter.write(file, newBytes);
                    status = STATUS_JUST_UPDATED;
                }
            }
            return status;
        } catch (Throwable e) {
            Logger.e("Dex check and update exception.", e);
            return STATUS_ERROR;
        }
    }
}
