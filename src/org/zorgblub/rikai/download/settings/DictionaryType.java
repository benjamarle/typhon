package org.zorgblub.rikai.download.settings;

/**
 * Created by Benjamin on 26/03/2016.
 */
public enum DictionaryType {
    EDICT,
    KANJIDIC,
    ENAMDICT;


    public DictionarySettings getImplementation() {
        switch (this){
            case EDICT:
                return new EdictSettings();
            case KANJIDIC:
                return new KanjidicSettings();
            case ENAMDICT:
                return new EnamdictSettings();
        }
        return null;
    }
}
