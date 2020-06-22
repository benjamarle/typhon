package org.zorgblub.rikai.download.settings.ui;

import android.content.Intent;
import android.os.Bundle;

import net.zorgblub.typhon.R;
import net.zorgblub.typhon.activity.TyphonActivity;

/**
 * Created by Benjamin on 26/03/2016.
 */
public class DictionaryConfigActivity extends TyphonActivity{

    private DictionaryConfigFragment dictionaryConfigFragment;

    @Override
    protected int getMainLayoutResource() {
            return R.layout.dictionary_list_activity;
    }


    @Override
    protected void onCreateTyphonActivity(Bundle savedInstanceState) {
        super.onCreateTyphonActivity(savedInstanceState);

        dictionaryConfigFragment = (DictionaryConfigFragment) getSupportFragmentManager().findFragmentById(R.id.dictionary_list_fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dictionaryConfigFragment.onActivityResult(requestCode, resultCode, data);
    }
}
