package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

abstract class BaseParceledListSlice<T> implements Parcelable {

    public List<T> getList() {
        return null;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }
}
