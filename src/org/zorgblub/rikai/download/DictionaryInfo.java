/*
Copyright (C) 2013 Ray Zhou

JadeRead is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JadeRead is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JadeRead.  If not, see <http://www.gnu.org/licenses/>

Author: Ray Zhou
Date: 2013 04 26

*/
package org.zorgblub.rikai.download;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * User: ray
 * Date: 2013-01-21
 * <p>
 * A simple class that contains the url and path information of the dictionaries used
 * by this app. The path is depended on a context, hence the methods are not static.
 */
final public class DictionaryInfo {
    private static final String DICTIONARY_VERSION = "1.0.2";
    private static final String DICTIONARY_ZIP_FILE = "DictFiles-" + DICTIONARY_VERSION + ".zip";
    private static final String DICTIONARY_EDICT_FILE = "polaredict.sqlite";
    private static final String DICTIONARY_NAMES_FILE = "polarnames.sqlite";
    private static final String DICTIONARY_DEINFECT_FILE = "deinflect.dat";
    private static final String DICTIONARY_KANJI_FILE = "kanji.dat";
    private static final String DICTIONARY_FOLDER_URL = "https://www.dropbox.com/s/chquj3kxuyjps4x";
    private final Context mContext;

    public DictionaryInfo(Context context) {
        mContext = context;
    }

    public String getDataPath() {
        return mContext.getExternalFilesDir(null).toString();
    }

    public String getDownloadUrl() {
        return DICTIONARY_FOLDER_URL + "/" + DICTIONARY_ZIP_FILE + "?dl=1";
    }

    public File getZipPath() {
        return makePath(DICTIONARY_ZIP_FILE);
    }

    public File getNamesPath() {
        return makePath(DICTIONARY_NAMES_FILE);
    }

    public File getEdictPath() {
        return makePath(DICTIONARY_EDICT_FILE);
    }

    public File getDeinflectPath() {
        return makePath(DICTIONARY_DEINFECT_FILE);
    }

    public File getKanjiPath() {
        return makePath(DICTIONARY_KANJI_FILE);
    }

    public String getDictionaryVersion(){
        return DICTIONARY_VERSION;
    }

    public File[] getExtractedFiles(){
        return new File[]{getNamesPath(), getEdictPath(), getDeinflectPath(), getKanjiPath()};
    }

    public boolean exists() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        for (File file : getExtractedFiles()) {
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }


    /**
     * concatenate the parent directory and the file name together
     *
     * @param filename the name of the file
     * @return conccatenated path
     */
    public File makePath(String filename) {
        String parentDir = getDataPath();
        return new File(parentDir.endsWith("/") ? parentDir + filename : parentDir + "/" + filename);
    }
}
