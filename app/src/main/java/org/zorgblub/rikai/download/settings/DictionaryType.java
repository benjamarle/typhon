package org.zorgblub.rikai.download.settings;

/**
 * Created by Benjamin on 26/03/2016.
 */
public enum DictionaryType {
    EDICT(EdictSettings.class, "Edict"),
    KANJIDIC(KanjidicSettings.class, "Kanjidic"),
    ENAMDICT(EnamdictSettings.class, "Enamdict"),
    WORDNET(WordnetSettings.class, "Wordnet"),
    EPWING(EpwingSettings.class, "Epwing");

    private Class<? extends DictionarySettings> clazz;

    private String name;

    DictionaryType(Class<? extends DictionarySettings> clazz, String name) {
        this.clazz = clazz;
        this.name = name;
    }

    public DictionarySettings getImplementation() {
        switch (this){
            case EDICT:
                return new EdictSettings();
            case KANJIDIC:
                return new KanjidicSettings();
            case ENAMDICT:
                return new EnamdictSettings();
            case WORDNET:
                return new WordnetSettings();
            case EPWING:
                return new EpwingSettings();
        }
        return null;
    }

    public Class<? extends DictionarySettings> getSettingsClass() {
        return this.clazz;
    }

    public String getName() {
        return name;
    }

    public static String[] getNames(){
        DictionaryType[] values = values();
        String names[] = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }
}
