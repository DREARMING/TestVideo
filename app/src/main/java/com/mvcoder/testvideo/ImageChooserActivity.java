package com.mvcoder.testvideo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageChooserActivity extends AppCompatActivity {

    private final int PICK_CODE = 100;
    private final int CODE_REQUEST_STORAGE = 101;
    private final int CODE_REQUEST_CONTACT = 102;

    private final String CONTENT_URI = "content://com.android.contacts/contacts/lookup/0r1-2D3731433B3B/1";

    @BindView(R.id.iv_select_img)
    ImageView ivSelectImg;
    @BindView(R.id.bt_select_thumbs)
    Button bt;
    @BindView(R.id.bt_select_contract)
    Button btContract;
    @BindView(R.id.bt_test)
    Button btTest;

    @BindView(R.id.bt_saf)
    Button btSaf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_chooser);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //joinToImgChooser();
                requestPermission();
            }
        });

        btContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestContactPermission();
            }
        });

        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGetContactWitoutPermission();
            }
        });

        btSaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                if(getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    startActivityForResult(intent, 103);
                }else{
                    LogUtils.d("没有匹配的intent");
                }
            }
        });
    }

    private void testGetContactWitoutPermission() {
        Cursor cursor = getContentResolver().query(Uri.parse(CONTENT_URI), null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                LogUtils.d("name : " + displayName + " , phone : " + hasPhone);
            }
            cursor.close();
        }
    }

    private void joinToContactActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 102);
    }

    private void requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CODE_REQUEST_CONTACT);
        } else {
            joinToContactActivity();
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_REQUEST_STORAGE);
        } else {
            joinToImgChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQUEST_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinToImgChooser();
            } else {
                ToastUtils.showShort("权限被限制");
            }
        } else if (requestCode == CODE_REQUEST_CONTACT && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinToContactActivity();
            } else {
                ToastUtils.showShort("权限被限制");
            }
        }
    }

    private void joinToImgChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooser = Intent.createChooser(intent, "选择图片");
        startActivityForResult(chooser, PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case PICK_CODE:
                selectPic(data);
                break;
            case 101:
                selectPic(data);
                break;
            case 102:
                selectContact(data);
                break;
            case 103:
                Uri uri = data.getData();
                if(uri != null){
                    LogUtils.d("uri : " + uri.toString());
                }
                break;
        }
    }

    private void selectContact(Intent intent) {
        Uri contactUri = intent.getData();
        if (contactUri != null) {
            LogUtils.d("ContactUri : " + contactUri.toString());
        }
        String phoneNumber = "";
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (hasPhone == 1) {
                    //需要权限
                    Cursor phones = getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = " + id, null, null);
                    while (phones.moveToNext()) {
                        phoneNumber = phones
                                .getString(phones
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }
                LogUtils.d("name : " + displayName + " , phone : " + phoneNumber);
            }
            cursor.close();
        }

    }

    private void selectPic(Intent intent) {
        Uri selectImageUri = intent.getData();
        Log.e("seg", selectImageUri.toString());
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Log.e("seg", picturePath);
        ivSelectImg.setImageBitmap(BitmapFactory.decodeFile(picturePath));
    }
}
