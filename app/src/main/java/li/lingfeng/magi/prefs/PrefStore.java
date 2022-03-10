package li.lingfeng.magi.prefs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;
import li.lingfeng.magi.L;
import li.lingfeng.magi.Loader;
import li.lingfeng.magi.utils.FileWriter;
import li.lingfeng.magi.utils.Logger;

public class PrefStore extends PreferenceDataStore {

    public static final String FOLDER = "/data/local/LTweaksMAGI";
    public static PrefStore instance = new PrefStore();
    private JSONObject mContent;
    private boolean mKeepInMemory = Loader.getApplication() == null;

    private PrefStore() {
    }

    private JSONObject load(String key) {
        JSONObject jContent = mContent;
        if (jContent != null) {
            return jContent;
        }
        File file = getFile(key);
        if (file != null && file.exists()) {
            try {
                String content = FileUtils.readFileToString(file);
                jContent = JSON.parseObject(content);
            } catch (Throwable e) {
                Logger.e("Failed to load exist " + file);
            }
        }
        if (jContent == null) {
            jContent = new JSONObject();
        }
        if (mKeepInMemory) {
            mContent = jContent;
        }
        return jContent;
    }

    private void save(String key, Object value) {
        JSONObject jPref = load(key);
        jPref.put(key, value);
        doSave(key, jPref);
    }

    private void delete(String key) {
        JSONObject jPref = load(key);
        jPref.remove(key);
        doSave(key, jPref);
    }

    private void doSave(String key, JSONObject jPref) {
        File file = getFile(key);
        if (file == null) {
            return;
        }
        try {
            if (jPref.isEmpty()) {
                if (file.exists()) {
                    FileWriter.delete(file);
                }
            } else {
                FileWriter.write(file, jPref.toString());
            }
        } catch (Throwable e) {
            Logger.e("Failed to write " + file, e);
        }
    }

    private File getFile(String key) {
        String packageName = L.keyToPackage(key);
        return packageName != null ? new File(FOLDER + "/" + packageName + ".pref") : null;
    }

    @Override
    public void putString(String key, @Nullable String value) {
        if (!StringUtils.isEmpty(value)) {
            save(key, value);
        } else {
            delete(key);
        }
    }

    @Override
    public void putStringSet(String key, @Nullable Set<String> values) {
        if (values != null && !values.isEmpty()) {
            save(key, values);
        } else {
            delete(key);
        }
    }

    @Override
    public void putInt(String key, int value) {
        save(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        save(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        save(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        if (value) {
            save(key, value);
        } else {
            delete(key);
        }
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key) ? jPref.getString(key) : defValue;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key) ? jPref.getJSONArray(key).stream().map(o -> (String) o).collect(Collectors.toSet()) : defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key) ? jPref.getIntValue(key) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key) ? jPref.getLongValue(key) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key) ? jPref.getFloatValue(key) : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key) ? jPref.getBooleanValue(key) : defValue;
    }

    public boolean contains(String key) {
        JSONObject jPref = load(key);
        return jPref.containsKey(key);
    }
}
