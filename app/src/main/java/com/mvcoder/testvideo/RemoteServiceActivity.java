package com.mvcoder.testvideo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;

import com.mvcoder.testvideo.bean.IBookManagerInterface;
import com.mvcoder.testvideo.services.RemoteServices;

public class RemoteServiceActivity extends AppCompatActivity {

    private IBookManagerInterface iBookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_service);
    }

    private void startService(){
        Intent intent = new Intent(this, RemoteServices.class);
        startService(intent);
        bindService(intent, connection ,0);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBookManager = IBookManagerInterface.Stub.asInterface(service);
            try {
                service.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(iBookManager != null) iBookManager.asBinder().unlinkToDeath(deathRecipient, 0);
            iBookManager = null;
        }
    };

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if(iBookManager == null) return;
            iBookManager.asBinder().unlinkToDeath(this,0);
            iBookManager = null;
        }
    };

}
