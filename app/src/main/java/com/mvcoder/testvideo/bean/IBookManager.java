package com.mvcoder.testvideo.bean;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;


public interface IBookManager extends IInterface {

    static abstract class Stub extends Binder implements IBookManagerInterface{

        private static final String DESCRIPTOR = "com.mvcoder.testvideo.bean.IBookManager";

        private static final int TRANSACTION_ADD_BOOK = IBinder.FIRST_CALL_TRANSACTION + 0;
        private static final int TRANSACTION_GET_BOOKS = IBinder.FIRST_CALL_TRANSACTION + 1;

        public Stub(){
            this.attachInterface(this, DESCRIPTOR);
        }

        public IBookManagerInterface asInterface(IBinder iBinder){
            if(iBinder == null) return null;
            if(iBinder.queryLocalInterface(DESCRIPTOR) != null){
                return new Proxy(iBinder);
            }
            return this;
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            switch (code){
                case TRANSACTION_ADD_BOOK: {
                    data.enforceInterface(DESCRIPTOR);
                    Book book;
                    if (data.readInt() != 0) {
                        book = Book.CREATOR.createFromParcel(data);
                        this.addBook(book);
                    } else {
                        book = null;
                    }
                    this.addBook(book);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_GET_BOOKS: {
                    data.enforceInterface(DESCRIPTOR);
                    List<Book> bookList = this.getBookList();
                    reply.writeNoException();
                    reply.writeTypedList(bookList);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        public static class Proxy implements IBookManagerInterface{

            private IBinder serviceBinder;

            protected Proxy(IBinder remoteBinder){
                serviceBinder = remoteBinder;
            }

            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }

            @Override
            public void addBook(Book book) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.enforceInterface(DESCRIPTOR);
                    if (book != null) {
                        _data.writeInt(1);
                        _data.writeParcelable(book, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    serviceBinder.transact(TRANSACTION_ADD_BOOK, _data, _reply, 0);
                    _reply.readException();
                }finally {
                    _data.recycle();
                    _reply.recycle();
                }
            }

            @Override
            public List<Book> getBookList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                List<Book> books;
                try {
                    _data.enforceInterface(DESCRIPTOR);
                    serviceBinder.transact(TRANSACTION_GET_BOOKS,_data,_reply,0);
                    _reply.readException();
                    books = _reply.createTypedArrayList(Book.CREATOR);
                    _data.recycle();
                    _reply.recycle();
                }finally {
                    _data.recycle();
                    _reply.recycle();
                }
                return books;
            }

            @Override
            public IBinder asBinder() {
                return serviceBinder;
            }
        }
    }

}
