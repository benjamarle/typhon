package org.zorgblub.rikai.download.settings;

import java.io.File;

/**
 * Created by Benjamin on 25/03/2016.
 */
public abstract class DownloadableSettings extends DictionarySettings {

    private static final String DICTIONARY_FOLDER_URL = "https://www.dropbox.com/s/chquj3kxuyjps4x";
    private static final String DICTIONARY_VERSION = "1.0.2";
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

}
