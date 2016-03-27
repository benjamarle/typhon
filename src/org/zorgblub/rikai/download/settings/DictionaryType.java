package org.zorgblub.rikai.download.settings;

/**
 * Created by Benjamin on 26/03/2016.
 */
public enum DictionaryType {
    EDICT(EdictSettings.class),
    KANJIDIC(KanjidicSettings.class),
    ENAMDICT(EnamdictSettings.class);

    private Class<? extends DictionarySettings> clazz;

    DictionaryType(Class<? extends DictionarySettings> clazz) {
        this.clazz = clazz;
    }

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

    public Class<? extends DictionarySettings> getSettingsClass() {
        return this.clazz;
    }
}
