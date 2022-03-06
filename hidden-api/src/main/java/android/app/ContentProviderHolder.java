package android.app;

import android.os.Parcel;
import android.os.Parcelable;

public class ContentProviderHolder implements Parcelable {

    protected ContentProviderHolder(Parcel in) {
    }

    public static final Creator<ContentProviderHolder> CREATOR = new Creator<ContentProviderHolder>() {
        @Override
        public ContentProviderHolder createFromParcel(Parcel in) {
            return new ContentProviderHolder(in);
        }

        @Override
        public ContentProviderHolder[] newArray(int size) {
            return new ContentProviderHolder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
