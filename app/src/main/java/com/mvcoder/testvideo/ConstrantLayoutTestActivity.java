package com.mvcoder.testvideo;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.mvcoder.testvideo.fragment.MenuFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConstrantLayoutTestActivity extends AppCompatActivity implements MenuFragment.OnFragmentInteractionListener {

    @BindView(R.id.button6)
    Button button6;
    @BindView(R.id.button7)
    Button button7;
    @BindView(R.id.button8)
    Button button8;
    @BindView(R.id.fragment_container)
    FrameLayout container;


    boolean showAllMenu = true;

    Fragment menuFragment;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.button9)
    Button button9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constrant_layout_test);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllMenu = !showAllMenu;
                invalidateOptionsMenu();
            }
        });
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShort("sdf");
                menuFragment = new MenuFragment();
                menuFragment.setHasOptionsMenu(true);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, menuFragment).commit();
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });
    }

    private void showPopupMenu() {
        PopupMenu menu = new PopupMenu(this, button8);
        menu.getMenuInflater().inflate(R.menu.menu_pop, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        ToastUtils.showShort("click : " + item.getTitle());
                        break;
                    case R.id.menu_add:
                        ToastUtils.showShort("click : " + item.getTitle());
                        break;
                }
                return true;
            }
        });
        menu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogUtils.d("onCreateOptionsMenu");
        if (showAllMenu) {
            getMenuInflater().inflate(R.menu.menu_test, menu);
            //更改名字
            MenuItem item = menu.findItem(R.id.menu_scan);
            item.setTitle("扫二维码");
        } else {
            getMenuInflater().inflate(R.menu.menu_test, menu);
            menu.removeItem(R.id.menu_scan);
            menu.removeItem(11);
        }

        MenuItem item = menu.findItem(R.id.menu_share);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ToastUtils.showShort("query : " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LogUtils.d("onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                ToastUtils.showShort("menu scan");
                break;
            case R.id.menu_share:
                ToastUtils.showShort("menu share");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private boolean isHide = true;

    private void hideAndShowActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(isHide)
            actionBar.hide();
        else
            actionBar.show();
        isHide = !isHide;
    }

    @OnClick(R.id.button9)
    public void onViewClicked() {
        hideAndShowActionBar();
    }
}
