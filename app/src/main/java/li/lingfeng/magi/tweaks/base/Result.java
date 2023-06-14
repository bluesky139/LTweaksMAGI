package li.lingfeng.magi.tweaks.base;

import java.util.Arrays;

import li.lingfeng.magi.utils.Logger;

public class Result {

    public interface Before {
        void before(Result r) throws Throwable;
    }

    public interface After {
        void after(Result r) throws Throwable;
    }

    public Object[] args;
    private Object result;
    private boolean _hasResult = false;
    private Before before;
    private After after;

    public Result before(Before before) {
        this.before = before;
        return this;
    }

    public Result after(After after) {
        this.after = after;
        return this;
    }

    public boolean hasBefore() {
        return before != null;
    }

    public boolean hasAfter() {
        return after != null;
    }

    public void hookBefore() {
        if (before != null) {
            try {
                before.before(this);
            } catch (Throwable e) {
                Logger.e("Hook before exception.", e);
            }
        }
    }

    public void hookAfter() {
        if (after == null) {
            return;
        }
        if (hasResult()) {
            Logger.e("Already has result, why execute after?");
            return;
        }
        try {
            after.after(this);
        } catch (Throwable e) {
            Logger.e("Hook after execute exception.", e);
        }
    }

    public void setArg(int i, Object arg) {
        if (args == null) {
            args = new Object[i + 1];
        } else if (args.length <= i) {
            args = Arrays.copyOf(args, i + 1);
        }
        args[i] = arg;
    }

    public void setResult(Object result) {
        this.result = result;
        _hasResult = true;
    }

    public void setResultSilently(Object result) {
        this.result = result;
    }

    public boolean hasResult() {
        return _hasResult;
    }

    public Object getResult() {
        return result;
    }

    public int getIntResult() {
        return (int) result;
    }
}
