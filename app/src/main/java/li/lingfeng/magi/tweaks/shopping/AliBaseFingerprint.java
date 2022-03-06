package li.lingfeng.magi.tweaks.shopping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import li.lingfeng.magi.tweaks.base.TweakBase;
import li.lingfeng.magi.utils.AESUtils;
import li.lingfeng.magi.utils.ContextUtils;
import li.lingfeng.magi.utils.Logger;
import li.lingfeng.magi.utils.ViewUtils;

public abstract class AliBaseFingerprint extends TweakBase {

    private static final String PAY_PWD_DIALOG_ACTIVITY = "com.alipay.mobile.verifyidentity.module.password.pay.ui.PayPwdDialogActivity";
    private static final String MSP_CONTAINER_ACTIVITY = "com.alipay.android.msp.ui.views.MspContainerActivity";
    private static final String FLY_BIRD_WINDOW_ACTIVITY = "com.alipay.android.app.flybird.ui.window.FlyBirdWindowActivity";
    private static final String PAY_PWD_HALF_ACTIVITY = "com.alipay.mobile.verifyidentity.module.password.pay.ui.PayPwdHalfActivity";
    private CancellationSignal mCancellationSignal;

    @Override
    protected boolean shouldRegisterActivityLifecycle() {
        return true;
    }

    // https://github.com/eritpchy/Xposed-Fingerprint-pay/blob/master/app/src/main/java/com/yyxx/wechatfp/xposed/plugin/XposedAlipayPlugin.java
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String clsName = activity.getClass().getName();
        if (clsName.equals(PAY_PWD_DIALOG_ACTIVITY) || clsName.equals(MSP_CONTAINER_ACTIVITY)
                || clsName.equals(FLY_BIRD_WINDOW_ACTIVITY) || clsName.equals(PAY_PWD_HALF_ACTIVITY)) {
            handleMspContainerActivity(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
            Logger.i("Cancel fingerprint scan.");
            mCancellationSignal.cancel();
        }
        mCancellationSignal = null;
    }

    private void handleMspContainerActivity(Activity activity) {
        activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    Logger.d("In MSP_CONTAINER_ACTIVITY");
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        return;
                    }
                    if (getPasswordView(activity) == null) {
                        Logger.w("getPasswordView null.");
                        ViewUtils.printChilds(activity);
                        return;
                    }

                    String password = getPassword(activity);
                    if (password == null) { // Save password at first time.
                        savePassword(activity);
                    } else {
                        authWithFingerprint(activity, password);
                    }
                    activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Throwable e) {
                    Logger.e("onGlobalLayout exception.", e);
                }
            }
        });
    }

    protected abstract boolean isAlipay();

    private void savePassword(Activity activity) throws Throwable {
        EditText passwordEditText = getPasswordView(activity);
        if (passwordEditText == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null password edit text.");
        }
        Logger.d("passwordEditText " + passwordEditText);

        View payButton = getPayButton(activity);
        if (payButton == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null pay button.");
        }
        Logger.d("payButton " + payButton);

        View.OnClickListener originalListener = ViewUtils.getViewClickListener(payButton);
        payButton.setOnClickListener((v) -> {
            Logger.i("Save password.");
            try {
                putPassword(activity, passwordEditText.getText().toString());
                Toast.makeText(activity, "Scan fingerprint at next time.", Toast.LENGTH_SHORT).show();
            } catch (Throwable e) {
                Logger.e("Put password exception.", e);
            }
            originalListener.onClick(v);
        });
        Toast.makeText(activity, "Preparing fingerprint scan for next time.", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void authWithFingerprint(Activity activity, String password) {
        String msg = "Fingerprint scan ready.";
        Logger.i(msg);
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(null, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                String msg = "Fingerprint error " + errorCode + ", " + errString;
                Logger.e(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                String msg = "Fingerprint help " + helpCode + ", " + helpString;
                Logger.w(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                String msg = "Fingerprint ok.";
                Logger.i(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                try {
                    inputGenericPassword(activity, password);
                } catch (Throwable e) {
                    Logger.e("inputGenericPassword error.", e);
                    Toast.makeText(activity, "Input generic password error.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                String msg = "Fingerprint failed.";
                Logger.e(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void inputGenericPassword(Activity activity, String password) throws Throwable {
        EditText passwordEditText = getPasswordView(activity);
        if (passwordEditText == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null password edit text.");
        }
        Logger.d("passwordEditText " + passwordEditText);

        View payButton = getPayButton(activity);
        if (payButton == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null pay button.");
        }
        Logger.d("payButton " + payButton);

        Logger.i("Input generic password and pay.");
        passwordEditText.setText(password);
        payButton.performClick();
    }

    private EditText getPasswordView(Activity activity) {
        int id = ContextUtils.getIdId("input_et_password",
                isAlipay() ? "com.alipay.android.phone.mobilecommon.verifyidentity" : "com.taobao.taobao");
        EditText passwordEditText = null;
        if (id > 0) {
            View view = activity.findViewById(id);
            if (view instanceof EditText && view.isShown()) {
                passwordEditText = (EditText) view;
            }
        }
        if (passwordEditText == null && isAlipay()) {
            id = ContextUtils.getIdId("input_et_password", "com.alipay.android.phone.safepaybase");
            if (id > 0) {
                View view = activity.findViewById(id);
                if (view instanceof EditText && view.isShown()) {
                    passwordEditText = (EditText) view;
                }
            }
        }
        if (passwordEditText == null) {
            List<EditText> editTexts = ViewUtils.findAllViewByType((ViewGroup) activity.getWindow().getDecorView(), EditText.class);
            for (EditText editText : editTexts) {
                if (editText.getId() == -1 && editText.isShown()) {
                    passwordEditText = editText;
                    break;
                }
            }
        }
        return passwordEditText;
    }

    private View getPayButton(Activity activity) {
        int id = ContextUtils.getIdId("button_ok",
                isAlipay() ? "com.alipay.android.phone.mobilecommon.verifyidentity" : "com.taobao.taobao");
        View payButton = null;
        if (id > 0) {
            View view = activity.findViewById(id);
            if (view != null && view.isShown()) {
                payButton = view;
            }
        }
        if (payButton == null && isAlipay()) {
            id = ContextUtils.getIdId("button_ok", "com.alipay.android.phone.safepaybase");
            if (id > 0) {
                View view = activity.findViewById(id);
                if (view != null && view.isShown()) {
                    payButton = view;
                }
            }
        }
        if (payButton == null) {
            Optional<TextView> textView = ViewUtils.findAllViewByType((ViewGroup) activity.getWindow().getDecorView(), TextView.class)
                    .stream()
                    .filter(v -> StringUtils.equalsAny(v.getText().toString(), "付款", "Pay", "确定", "确认"))
                    .findFirst();
            if (textView.isPresent()) {
                payButton = textView.get();
            }
        }
        return payButton;
    }

    private String getPassword(Activity activity) throws Throwable {
        String str = activity.getSharedPreferences("ltweaks_alipay", 0).getString("payment_input", null);
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        String androidId = Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
        return AESUtils.decrypt(str, androidId);
    }

    private void putPassword(Activity activity, String password) throws Throwable {
        String androidId = Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
        String str = AESUtils.encrypt(password, androidId);
        activity.getSharedPreferences("ltweaks_alipay", 0).edit().putString("payment_input", str).commit();
    }
}
