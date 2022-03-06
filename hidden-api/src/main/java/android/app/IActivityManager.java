package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IActivityManager extends IInterface {

    void attachApplication(IApplicationThread app, long startSeq) throws RemoteException;

    ContentProviderHolder getContentProvider(IApplicationThread caller, String callingPackage,
                                             String name, int userId, boolean stable) throws RemoteException;

    public static abstract class Stub extends Binder implements IActivityManager {

        public static IActivityManager asInterface(IBinder binder) {
            return null;
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
