package li.lingfeng.magi.utils;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FileWriter {

    public static void write(File file, String content) throws IOException {
        write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void write(File file, byte[] bytes) throws IOException {
        File tmpFile = SuFile.open(file.getPath() + ".tmp");
        OutputStream out = SuFileOutputStream.open(tmpFile);
        IOUtils.write(bytes, out);
        out.close();
        tmpFile.setReadable(true, false);
        tmpFile.setExecutable(true, false);
        Shell.Result result = Shell.su("chcon u:object_r:dalvikcache_data_file:s0 " + tmpFile.getPath()).exec();
        if (result.getCode() != 0) {
            throw new IOException("chcon " + tmpFile + " return " + result.getCode());
        }
        if (file.exists()) {
            delete(file);
        }
        FileUtils.moveFile(tmpFile, file);
    }

    public static void delete(File file) throws IOException {
        if (!SuFile.open(file.getPath()).delete()) {
            throw new IOException("Can't delete " + file);
        }
    }
}
