package org.zorgblub.rikai.download.settings.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import net.zorgblub.typhon.R;
import net.zorgblub.typhon.activity.FileBrowseActivity;

import org.zorgblub.rikai.download.settings.EpwingSettings;

/**
 * Created by Benjamin on 03/04/2016.
 */
public class EpwingConfigDialog extends DictionaryConfigDialog<EpwingSettings> {

    protected EditText pathEditText;

    private IntentCallBack intentCallBack;

    public EpwingConfigDialog(Context context, EpwingSettings settings) {
        super(context, settings);
    }

    @Override
    protected int getViewId() {
        return R.layout.dictionary_epwing_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pathEditText = (EditText) findViewById(R.id.epwing_folder);
        Button browseButton = (Button) findViewById(R.id.browse_epwing_button);

        browseButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FileBrowseActivity.class);
            intent.setData(Uri.parse(pathEditText.getText().toString()));
            Activity activity = getOwnerActivity();
            activity.startActivityForResult(intent, 0);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == Activity.RESULT_OK && data != null ) {
            pathEditText.setText(data.getData().getPath());
        }
    }

    @Override
    protected boolean saveSettings() {
        super.saveSettings();
        settings.setBasePath(pathEditText.getText().toString());
        return true;
    }

    public interface IntentCallBack {
        void onResult(int resultCode, Intent data);
    }
}
