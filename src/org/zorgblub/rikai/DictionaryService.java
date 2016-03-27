package org.zorgblub.rikai;

import android.content.Context;
import android.util.Pair;

import net.zorgblub.typhon.view.bookview.SelectedWord;

import org.rikai.dictionary.AbstractEntry;
import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.Entries;
import org.zorgblub.rikai.download.settings.DictionarySettings;
import org.zorgblub.rikai.download.settings.DownloadableSettings;

import java.util.List;

/**
 * Created by Benjamin on 23/03/2016.
 */
public interface DictionaryService {
    void initDictionaries(Context context);

    DictionaryServiceImpl.DictionaryStatus checkDictionaries(List<DictionarySettings> list);

    void downloadAndExtract(List<DownloadableSettings> list, Context context);

    Dictionary getDictionary(int index);

    int getNbDictionaries();

    void setDictionaryListener(DictionaryServiceImpl.DictionaryListener dictionaryListener);

    Entries query(int dicIndex, SelectedWord word);

    Pair<SelectedWord, Entries>  getLastMatch(int dicIndex);

    boolean saveInAnki(AbstractEntry abstractEntry, Context context, SelectedWord selectedWord, String bookTitle);

    void setCurrentDictionary(int index);

    void setMessageListener(DictionaryServiceImpl.MessageListener messageListener);

    List<DownloadableSettings> getDownloadableSettings();

    List<DictionarySettings> getSettings();

    void saveSettings(List<DictionarySettings> settings);
}
