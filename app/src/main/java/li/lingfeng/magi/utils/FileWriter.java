package li.lingfeng.magi.utils;

import com.topjohnwu.superuser.Shell;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileWriter {

    public static void write(File file, String content) throws IOException {
        write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void write(File file, byte[] bytes) throws IOException {
        File tmpFile = new File(file.getPath() + ".tmp");
        FileUtils.writeByteArrayToFile(tmpFile, bytes);
        tmpFile.setReadable(true, false);
        tmpFile.setExecutable(true, false);
        Shell.Result result = Shell.su("chcon u:object_r:app_data_file:s0 " + tmpFile.getPath()).exec();
        if (result.getCode() != 0) {
            throw new IOException("chcon " + tmpFile + " return " + result.getCode());
        }
        if (file.exists()) {
            FileUtils.delete(file);
        }
        FileUtils.moveFile(tmpFile, file);
    }
}
