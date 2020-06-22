package org.zorgblub.rikai.download.settings;

import android.content.Context;
import android.os.Environment;

import net.zorgblub.typhon.Typhon;

import org.rikai.dictionary.Dictionary;
import org.zorgblub.rikai.download.settings.ui.dialog.DictionaryConfigDialog;

import java.io.File;
import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public abstract class DictionarySettings {

    //TODO Font size per dictionary

    protected static Context context = Typhon.get().getApplicationContext();

    private static String DATA_PATH = "/dict_data";

    public DictionarySettings() {

    }

    public boolean exists() {
        if (!isExternalStorageMounted()) return false;

        File file = getFile();
        return file.exists();
    }

    protected boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public File getFile() {
        return makePath(this.getBasePath());
    }


    /**
     * concatenate the parent directory and the file name together
     *
     * @param filename the name of the file
     * @return concatenated path
     */
    protected static File makePath(String filename) {
        File parentFile = getDataPath();
        String parentDir = parentFile.getAbsolutePath();
        return appendToFile(parentDir, filename);
    }

    private static File appendToFile(String baseName, String fileName) {
        return new File(baseName.endsWith("/") ? baseName + fileName : baseName + "/" + fileName);
    }

    public static File getDataPath() {
        String dataPath = context.getExternalFilesDir(null).getAbsolutePath();
        return appendToFile(dataPath, DATA_PATH);
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

    public abstract String getName();

    public abstract void setName(String name);

    public abstract DictionaryConfigDialog getConfigDialog(Context context);

    @Override
    public String toString() {
        return getName();
    }
}
