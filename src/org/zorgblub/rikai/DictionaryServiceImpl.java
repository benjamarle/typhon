package org.zorgblub.rikai;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.widget.Toast;

import com.ichi2.anki.api.AddContentApi;

import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.R;
import net.zorgblub.typhon.view.bookview.SelectedWord;

import org.rikai.deinflector.Deinflector;
import org.rikai.dictionary.AbstractEntry;
import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.DictionaryNotLoadedException;
import org.rikai.dictionary.Entries;
import org.rikai.dictionary.db.DatabaseException;
import org.rikai.dictionary.edict.EdictEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zorgblub.anki.AnkiDroidConfig;
import org.zorgblub.rikai.download.DictionaryInfo;
import org.zorgblub.rikai.download.SimpleDownloader;
import org.zorgblub.rikai.download.SimpleExtractor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static jedi.functional.FunctionalPrimitives.forEach;

/**
 * Created by Benjamin on 23/03/2016.
 */
public class DictionaryServiceImpl implements DictionaryService {

    private ArrayList<Dictionary> dictionaries = new ArrayList<>();

    private Configuration config;

    private DictionaryListener listener;

    private int currentDictionary;

    private SparseArray<Pair<SelectedWord, Entries>> matchesCaches = new SparseArray<>();

    private static final Logger LOG = LoggerFactory
            .getLogger("DictionaryService");

    public DictionaryServiceImpl(Configuration config) {
        this.config = config;
    }

    @Override
    public void initDictionaries(Context context) {
        DictionaryInfo dictionaryInfo = new DictionaryInfo(context);
        DictionaryStatus status = checkDictionaries(dictionaryInfo);
        fireDictionaryChecked(status);
        if(status.equals(DictionaryStatus.OK)) {
            loadDictionaries(dictionaryInfo, context);
        }
    }

    private void loadDictionaries(DictionaryInfo dictionaryInfo, Context context) {
        Resources resources = context.getResources();
        try {
            Deinflector deinflector = new Deinflector(dictionaryInfo.getDeinflectPath().getAbsolutePath());
            dictionaries.add(new DroidWordEdictDictionary(dictionaryInfo.getEdictPath().getAbsolutePath(), deinflector, new DroidSqliteDatabase(), resources));
        } catch (IOException e) {
            LOG.error("Could not load deinflector data", e);
        }

        try {
            dictionaries.add(new DroidKanjiDictionary(dictionaryInfo.getKanjiPath().getAbsolutePath(), Integer.MAX_VALUE, resources, config.getHeisig6()));
            dictionaries.add(new DroidNamesDictionary(dictionaryInfo.getNamesPath().getAbsolutePath(), new DroidSqliteDatabase(), resources));
        } catch(FileNotFoundException e){
            LOG.error("Could not find dictionary data", e);
        } catch (DatabaseException e) {
            LOG.error("Could not load dictionary data", e);
        }

        Runnable task = () -> {
            forEach(dictionaries, (dictionary) -> dictionary.load());
            fireDictionaryLoaded();
        };
        Thread thread = new Thread(task);
        thread.run();
    }


    public enum DictionaryStatus{
        OK,
        UPDATE_NEEDED,
        NOT_EXISTENT;
    }

    @Override
    public DictionaryStatus checkDictionaries(DictionaryInfo dictInfo){
        if (dictInfo.exists()) {
            if(config.getDictionaryVersion().compareToIgnoreCase(dictInfo.getDictionaryVersion()) < 0){
                return DictionaryStatus.UPDATE_NEEDED;
            }else {
                return DictionaryStatus.OK;
            }
        }
        return DictionaryStatus.NOT_EXISTENT;
    }


    @Override
    public void downloadAndExtract(DictionaryInfo dictInfo, Context context){
        final SimpleDownloader downloader = new SimpleDownloader(context);
        downloader.setOnFinishTaskListener((boolean success) -> {
            if (success) {
                extract(dictInfo, context);
            } else {
                dictInfo.getZipPath().delete();
                showDownloadTroubleDialog(context);
            }
        });

        downloader.execute(dictInfo.getDownloadUrl(), dictInfo.getZipPath().getAbsolutePath());
    }


    public void extract(DictionaryInfo dictInfo, Context context){
        final SimpleExtractor extractor = new SimpleExtractor(context);
        extractor.setOnFinishTasklistener((boolean success) -> {
            if (success) {
                dictInfo.getZipPath().delete();
                config.setDictionaryVersion(dictInfo.getDictionaryVersion());
                initDictionaries(context);
            } else {
                dictInfo.getEdictPath().delete();
                dictInfo.getNamesPath().delete();
                dictInfo.getKanjiPath().delete();
                dictInfo.getDeinflectPath().delete();
                showDownloadTroubleDialog(context);
            }

        });
        extractor.execute(dictInfo.getZipPath().getAbsolutePath());
    }

    private void showDownloadTroubleDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.dm_dict_alternate_download_address)
                .setPositiveButton(R.string.msg_ok, null)
                .create()
                .show();
    }

    @Override
    public Dictionary getDictionary(int index){
        return this.dictionaries.get(index);
    }

    @Override
    public int getNbDictionaries(){
        return this.dictionaries.size();
    }

    public interface DictionaryListener{

        void onDictionaryChecked(DictionaryStatus status);

        void onDictionaryLoaded();

        void onCurrentDictionaryChanged(int index);

        void onCurrentMatchChanged(SelectedWord word, Entries match);

    }

    private void fireDictionaryChecked(DictionaryStatus status){
        if(listener == null)
            return;
         listener.onDictionaryChecked(status);
    }

    private void fireDictionaryLoaded(){
        if(listener == null)
            return;
         listener.onDictionaryLoaded();
    }

    private void fireCurrentDictionaryChanged(int index){
        if(listener == null)
            return;
        listener.onCurrentDictionaryChanged(index);
    }

    private void fireCurrentMatchChanged(SelectedWord word, Entries match){
        if(listener == null)
            return;
        listener.onCurrentMatchChanged(word, match);
    }

    @Override
    public void setDictionaryListener(DictionaryListener dictionaryListener){
        this.listener = dictionaryListener;
    }

    public void setCurrentDictionary(int index){
        currentDictionary = index;
        fireCurrentDictionaryChanged(index);
        Pair<SelectedWord, Entries> cacheHit = matchesCaches.get(index);
        if(cacheHit != null){
            fireCurrentMatchChanged(cacheHit.first, cacheHit.second);
        }
    }

    public Entries query(int dicIndex, SelectedWord word){
        Dictionary dictionary = this.getDictionary(dicIndex);
        Entries entries;


        Pair<SelectedWord, Entries> cacheHit = matchesCaches.get(dicIndex);
        if(cacheHit != null && cacheHit.first.equals(word)){
            return cacheHit.second;
        }

        try {
            entries = dictionary.query(word.getText().toString());

            if(entries.size() == 0){
                entries.add(new DroidEdictEntry("No word found for this dictionary")); // TODO translate
            }
        } catch (DictionaryNotLoadedException e){
            entries = new Entries();
            entries.add(new DroidEdictEntry("Dictionary not yet loaded")); // TODO translate
        }
        if(dicIndex == currentDictionary){
            fireCurrentMatchChanged(word, entries);
        }

        matchesCaches.put(dicIndex, new Pair<SelectedWord, Entries>(word, entries));
        return entries;
    }


    @Override
    public boolean saveInAnki(AbstractEntry abstractEntry, Context context, SelectedWord selectedWord, String bookTitle){
        // Currently working only for edict
        if(!(abstractEntry instanceof EdictEntry))
            return false;

        if(AddContentApi.getAnkiDroidPackageName(context) == null){
            // AnkiDroid not installed
            Toast.makeText(context, R.string.anki_not_installed, Toast.LENGTH_LONG).show();
        }


        EdictEntry entry = (EdictEntry) abstractEntry;

        String reqPerm = AddContentApi.checkRequiredPermission(context);
        if(reqPerm != null){
            // Request permission for Android 6+
            //TODO request the permission as a resolution
            Toast.makeText(context, R.string.anki_permission_denied, Toast.LENGTH_LONG).show();
            return false;
        }


        // Get api instance
        final AddContentApi api = new AddContentApi(context);
        // Look for our deck, add a new one if it doesn't exist
        Long did = api.findDeckIdByName(AnkiDroidConfig.DECK_NAME);
        if (did == null) {
            did = api.addNewDeck(AnkiDroidConfig.DECK_NAME);
        }
        // Look for our model, add a new one if it doesn't exist
        Long mid = api.findModelIdByName(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS.length);
        if (mid == null) {
            mid = api.addNewCustomModel(AnkiDroidConfig.MODEL_NAME, AnkiDroidConfig.FIELDS,
                    AnkiDroidConfig.CARD_NAMES, AnkiDroidConfig.QFMT, AnkiDroidConfig.AFMT,
                    AnkiDroidConfig.CSS, did);
        }

        // Double-check that everything was added correctly
        String[] fieldNames = api.getFieldList(mid);
        if (mid == null || did == null || fieldNames == null) {
            Toast.makeText(context, R.string.anki_card_add_fail, Toast.LENGTH_LONG).show();
            return false;
        }


        String sentence = selectedWord.getContextSentence().toString();
        String originalWord = entry.getOriginalWord();

        sentence = sentence.replace(originalWord, "<span class=\"emph\">"+originalWord+"</span>");

        String flds[] = {originalWord, entry.getReading(), entry.getGloss(), sentence, entry.getReason(), entry.getWord()};



        // Add a new note using the current field map
        try {
            // Only add item if there aren't any duplicates
            if (!api.checkForDuplicates(mid, did, flds)) {
                String tags = AnkiDroidConfig.TAGS+","+bookTitle.replaceAll("[^A-Za-z0-9]","_");
                Uri noteUri = api.addNewNote(mid, did, flds, tags);
                if (noteUri != null) {
                    Toast.makeText(context, context.getResources().getString(R.string.anki_card_added, flds[0]), Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(context, R.string.anki_card_already_added, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("AnkiCardAdd", "Exception adding cards to AnkiDroid", e);
            Toast.makeText(context, R.string.anki_card_add_fail, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}


