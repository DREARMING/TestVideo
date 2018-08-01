package com.mvcoder.testvideo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable{

    private int bookId;
    private String bookName;
    private boolean borrow;

    protected Book(Parcel in) {
        bookId = in.readInt();
        bookName = in.readString();
        borrow = in.readByte() != 0;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(bookName);
        dest.writeByte((byte) (borrow ? 1 : 0));
    }

    public static void main(String[] args) {
        
    }
}
