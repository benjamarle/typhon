package org.zorgblub.rikai.download.settings;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Benjamin on 25/03/2016.
 */
public abstract class DownloadableSettings extends DictionarySettings {

    private static final String DICTIONARY_FOLDER_URL = "https://www.dropbox.com/s/n4ukf6avborapn4";
    private static final String DICTIONARY_VERSION = "1.0.4";
    private static final String DICTIONARY_ZIP_FILE = "DictFiles-" + DICTIONARY_VERSION + ".zip";

    public DownloadableSettings() {
        super();
    }

    public static String getDownloadUrl() {
        return DICTIONARY_FOLDER_URL + "/" + DICTIONARY_ZIP_FILE + "?dl=1";
    }


    public static String getDictionaryVersion() {
        return DICTIONARY_VERSION;
    }

    @Override
    public boolean isDownloadable() {
        return true;
    }

    public void delete() {
        if (this.exists())
            this.getFile().delete();
    }

    public File[] getFiles(){
        return new File[]{this.getFile()};
    }

    @Override
    public boolean exists() {
        if(!isExternalStorageMounted()) return false;
        for (File file:
             getFiles()) {
            if(!file.exists())
                return false;
        }
        return true;
    }

    public static File getZipFile() {
        return makePath(DICTIONARY_ZIP_FILE);
    }

    public static boolean existsZip() {
        return getZipFile().exists();
    }

    public static void deleteZip() {
        if (existsZip())
            getZipFile().delete();
    }

    public static void deleteAll() throws IOException{
        if(getDataPath().exists())
            FileUtils.deleteDirectory(getDataPath());
    }

}
