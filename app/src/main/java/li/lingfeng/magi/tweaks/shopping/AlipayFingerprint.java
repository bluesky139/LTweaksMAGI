package li.lingfeng.magi.tweaks.shopping;

import li.lingfeng.lib.AppLoad;
import li.lingfeng.magi.prefs.PackageNames;

@AppLoad(packageName = PackageNames.ALIPAY, pref = "alipay_fingerprint")
public class AlipayFingerprint extends AliBaseFingerprint {

    @Override
    protected boolean isAlipay() {
        return true;
    }
}
