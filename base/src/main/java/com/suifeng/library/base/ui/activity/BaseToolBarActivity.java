package com.suifeng.library.base.ui.activity;

import android.support.v7.widget.Toolbar;

/**
 * Created by suifeng_e on 17/3/4.
 */
public abstract class BaseToolBarActivity extends BaseAppCompatActivity {

    public abstract boolean providesActivityToolbar();


    public void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
