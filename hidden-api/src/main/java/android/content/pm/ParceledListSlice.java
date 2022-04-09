package android.content.pm;

import android.os.Parcelable;

public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR = null;
}
