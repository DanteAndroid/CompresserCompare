package com.dante.rxdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by yons on 16/11/25.
 */

public class Image implements Parcelable {
    public File image;
    public String type;
    public String size;

    public Image(File image, String imageType, String size) {
        this.image = image;
        this.type = imageType;
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.image);
        dest.writeString(this.type);
        dest.writeString(this.size);
    }

    protected Image(Parcel in) {
        this.image = (File) in.readSerializable();
        this.type = in.readString();
        this.size = in.readString();
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
