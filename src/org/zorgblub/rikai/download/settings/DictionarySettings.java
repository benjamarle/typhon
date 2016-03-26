package org.zorgblub.rikai.download.settings;

import android.content.Context;
import android.os.Environment;

import net.zorgblub.typhon.Typhon;

import org.rikai.dictionary.Dictionary;

import java.io.File;
import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public abstract class DictionarySettings {

    protected static Context context = Typhon.get().getApplicationContext();

    public DictionarySettings() {

    }

    public boolean exists() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        File file = getFile();
        return file.exists();
    }

    public File getFile() {
        return makePath(this.getBasePath());
    }


    /**
     * concatenate the parent directory and the file name together
     *
     * @param filename the name of the file
     * @return conccatenated path
     */
    protected static File makePath(String filename) {
        String parentDir = getDataPath();
        return new File(parentDir.endsWith("/") ? parentDir + filename : parentDir + "/" + filename);
    }

    protected static String getDataPath() {
        return context.getExternalFilesDir(null).toString();
    }

    public boolean isDownloadable(){
        return false;
    }

    public boolean isHelper(){
        return false;
    }

    public abstract String getBasePath();

    public abstract Dictionary newInstance() throws IOException;

    public abstract DictionaryType getType();


}
