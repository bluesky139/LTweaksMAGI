package li.lingfeng.magi.tweaks.shopping;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;

@AppLoad(packageName = PackageNames.TAOBAO, pref = "taobao_fingerprint")
public class TaobaoFingerprint extends AliBaseFingerprint {

    @Override
    protected boolean isAlipay() {
        return false;
    }
}
