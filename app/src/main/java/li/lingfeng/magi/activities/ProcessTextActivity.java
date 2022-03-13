package li.lingfeng.magi.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

import androidx.annotation.Nullable;
import li.lingfeng.magi.R;
import li.lingfeng.magi.prefs.PackageNames;
import li.lingfeng.magi.utils.ComponentUtils;
import li.lingfeng.magi.utils.Logger;

/**
 * Created by lilingfeng on 2017/6/30.
 */

public class ProcessTextActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().getAction().equals(Intent.ACTION_PROCESS_TEXT)
                || !getIntent().getType().equals("text/plain")
                || !ComponentUtils.isAlias(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String text = getIntent().getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        if (text == null || text.isEmpty()) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = ComponentUtils.getAlias(this);
        Logger.i("ProcessText " + text + " with " + name);
        try {
            Method method = ProcessTextActivity.class.getDeclaredMethod(StringUtils.uncapitalize(name), String.class);
            method.invoke(this, text);
        } catch (Exception e) {
            Logger.e("ProcessTextActivity invoke error, " + e);
            Logger.stackTrace(e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Nullable
    @Override
    public Uri getReferrer() {
        int clipUid = getIntent().getIntExtra("ltweaks_clip_uid", 0);
        if (clipUid > 0) {
            return Uri.parse("android-app://" + getPackageManager().getNameForUid(clipUid));
        }
        return super.getReferrer();
    }

    private void bilibili(String text) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(PackageNames.BILIBILI);
        intent.setData(Uri.parse("bilibili://search/" + text));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
