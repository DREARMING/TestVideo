package com.mvcoder.testvideo.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.mvcoder.testvideo.bean.Book;
import com.mvcoder.testvideo.bean.IBookManagerInterface;

import java.util.ArrayList;
import java.util.List;

public class RemoteServices extends Service {

    private List<Book> bookList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub.asBinder();
    }


    private IBookManagerInterface.Stub stub = new IBookManagerInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void addBook(Book book) throws RemoteException {
            if(book != null)
                bookList.add(book);
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return bookList;
        }
    };

}
