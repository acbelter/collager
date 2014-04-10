package com.acbelter.collager;

import android.os.Parcel;
import android.os.Parcelable;

public class InstagramImageData implements Comparable<InstagramImageData>, Parcelable {
    protected String mLink;
    protected int mLikes;
    protected boolean mChecked;

    public InstagramImageData(int likes, String link) {
        mLikes = likes;
        mLink = link;
    }

    private InstagramImageData(Parcel in) {
        mLink = in.readString();
        mLikes = in.readInt();
        mChecked = in.readInt() == 1;
    }

    public String getLink() {
        return mLink;
    }

    public int getLikes() {
        return mLikes;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    @Override
    public int compareTo(InstagramImageData another) {
        if (mLikes > another.mLikes) {
            return 1;
        }
        if (mLikes < another.mLikes) {
            return -1;
        }
        return 0;
    }

    public static final Parcelable.Creator<InstagramImageData> CREATOR =
            new Parcelable.Creator<InstagramImageData>() {
                @Override
                public InstagramImageData createFromParcel(Parcel in) {
                    return new InstagramImageData(in);
                }

                @Override
                public InstagramImageData[] newArray(int size) {
                    return new InstagramImageData[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mLink);
        out.writeInt(mLikes);
        out.writeInt(mChecked ? 1 : 0);
    }
}
