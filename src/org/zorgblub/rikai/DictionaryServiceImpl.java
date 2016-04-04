package org.zorgblub.rikai;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.ichi2.anki.api.AddContentApi;

import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.R;
import net.zorgblub.typhon.Typhon;
import net.zorgblub.typhon.view.bookview.SelectedWord;

import org.rikai.dictionary.AbstractEntry;
import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.DictionaryNotLoadedException;
import org.rikai.dictionary.Entries;
import org.rikai.dictionary.db.DatabaseException;
import org.rikai.dictionary.edict.EdictEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zorgblub.anki.AnkiDroidConfig;
import org.zorgblub.rikai.download.SimpleDownloader;
import org.zorgblub.rikai.download.SimpleExtractor;
import org.zorgblub.rikai.download.settings.DictionarySettings;
import org.zorgblub.rikai.download.settings.DictionaryType;
import org.zorgblub.rikai.download.settings.DownloadableSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jedi.functional.FunctionalPrimitives.forEach;

/**
 * Created by Benjamin on 23/03/2016.
 */
public class DictionaryServiceImpl implements DictionaryService {

    private ArrayList<Dictionary> dictionaries = new ArrayList<>();

    private Context context;

    private Configuration config;

    private DictionaryListener dictionaryListener;

    private MessageListener messageListener;

    private int currentDictionary;

    private SparseArray<Pair<SelectedWord, Entries>> matchesCaches = new SparseArray<>();

    private boolean initialized;

    private long lastUpdate = System.currentTimeMillis();

    private static final Logger LOG = LoggerFactory
            .getLogger("DictionaryService");

    private DictionaryServiceImpl() {
        Typhon typhon = Typhon.get();
        this.context = typhon.getApplicationContext();
        this.config = new Configuration(this.context);
    }

    @Override
    public void initDictionaries(Context context) {
        if (initialized) {
            fireDictionaryLoaded();
            return;
        }


        List<DictionarySettings> dicSettings = getSettings();

        DictionaryStatus status = checkDictionaries(dicSettings);
        fireDictionaryChecked(status);
        if (status.equals(DictionaryStatus.OK)) {
            loadDictionaries(dicSettings, context);
        }
    }

    @Override
    public List<DictionarySettings> getSettings() {
        List<DictionarySettings> dicSettings;
        String dictionarySettingsStr = config.getDictionarySettings();
        if (dictionarySettingsStr == null || dictionarySettingsStr.length() == 0)
            return getDefaultDictionaries();

        return deserializeSettings(dictionarySettingsStr);
    }

    @Override
    public void saveSettings(List<DictionarySettings> settings) {
        String settingsStr = serializeSettings(settings);
        config.setDictionarySettings(settingsStr);
        this.lastUpdate = System.currentTimeMillis();
    }

    @Override
    public long getLastUpdateTimestamp() {
        return lastUpdate;
    }

    protected String serializeSettings(List<DictionarySettings> settings) {
        Type targetClassType = new TypeToken<ArrayList<DictionarySettings>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(settings, targetClassType);
    }

    protected List<DictionarySettings> deserializeSettings(String settingsStr) {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(DictionarySettings.class,
                new JsonDeserializer<DictionarySettings>() {
                    @Override
                    public DictionarySettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        JsonObject asJsonObject = json.getAsJsonObject();
                        JsonElement typeOfSettings = asJsonObject.get("type");
                        if (typeOfSettings == null)
                            return context.deserialize(json, typeOfT);
                        String type = typeOfSettings.getAsString();

                        DictionaryType dictionaryType = DictionaryType.valueOf(type);
                        DictionarySettings deserialize = context.deserialize(json, dictionaryType.getSettingsClass());
                        return deserialize;
                    }
                });
        Gson gson = builder.create();
        Type targetClassType = new TypeToken<ArrayList<DictionarySettings>>() {
        }.getType();
        return gson.fromJson(settingsStr, targetClassType);
    }

    protected List<DictionarySettings> getDefaultDictionaries() {
        List<DictionarySettings> list = new ArrayList<>();
        for (DictionaryType type : DictionaryType.values()) {
            DictionarySettings implementation = type.getImplementation();
            if(!implementation.isDownloadable())
                continue;
            list.add(implementation);
        }
        return list;
    }

    private void loadDictionaries(List<DictionarySettings> list, Context context) {
        for (DictionarySettings settings : list) {
            try {
                Dictionary dictionary = settings.newInstance();
                if(dictionary == null) {
                    continue;
                }
                dictionaries.add(dictionary);
            } catch (FileNotFoundException e) {
                LOG.error("Could not find dictionary data", e);
            } catch (IOException e) {
                LOG.error("Could not read dictionary data", e);
            } catch (DatabaseException e) {
                LOG.error("Could not load dictionary data", e);
            }
        }

        Runnable task = () -> {
            forEach(dictionaries, (dictionary) -> dictionary.load());
            fireDictionaryLoaded();
            initialized = true;
        };
        Thread thread = new Thread(task);
        thread.run();
    }


    public enum DictionaryStatus {
        OK,
        UPDATE_NEEDED,
        NOT_EXISTENT
    }

    @Override
    public DictionaryStatus checkDictionaries(List<DictionarySettings> list) {
        boolean allExistent = true;
        for (DictionarySettings settings : list) {
            if (settings.isDownloadable() && !settings.exists()) {
                allExistent = false;
                break;
            }
        }
        if (allExistent) {
            if (config.getDictionaryVersion().compareToIgnoreCase(DownloadableSettings.getDictionaryVersion()) < 0) {
                return DictionaryStatus.UPDATE_NEEDED;
            } else {
                return DictionaryStatus.OK;
            }
        }
        return DictionaryStatus.NOT_EXISTENT;
    }

    @Override
    public List<DownloadableSettings> getDownloadableSettings() {
        List<DownloadableSettings> list = new ArrayList<>();
        for (DictionarySettings settings : this.getSettings()) {
            if (settings instanceof DownloadableSettings) {
                list.add((DownloadableSettings) settings);
            }
        }
        return list;
    }

    @Override
    public void downloadAndExtract(List<DownloadableSettings> list, Context context) {
        // TODO Separate the UI dialogs with the download business
        // TODO trouble dialog with more details would be good
        try {
            DownloadableSettings.deleteAll();
        } catch (IOException e) {
            showDownloadTroubleDialog(context);
        }
        File dataPath = DictionarySettings.getDataPath();

        if (!dataPath.exists()) {
            if (!dataPath.mkdirs()) {
                showDownloadTroubleDialog(context);
                return;
            }
        }
        final SimpleDownloader downloader = new SimpleDownloader(context);
        downloader.setOnFinishTaskListener((boolean success) -> {
            if (success) {
                extract(list, context);
            } else {
                DownloadableSettings.getZipFile().delete();
                showDownloadTroubleDialog(context);
            }
        });

        downloader.execute(DownloadableSettings.getDownloadUrl(), DownloadableSettings.getZipFile().getAbsolutePath());
    }


    public void extract(List<DownloadableSettings> dictInfo, Context context) {

        Set<String> filenames = new HashSet<>();

        for (DownloadableSettings settings : dictInfo) {
            File files[] = settings.getFiles();
            for (File file :
                    files) {
                filenames.add(file.getName());
            }
        }
        final SimpleExtractor extractor = new SimpleExtractor(context, filenames);
        extractor.setOnFinishTasklistener((boolean success) -> {
            if (success) {
                DownloadableSettings.deleteZip();
                config.setDictionaryVersion(DownloadableSettings.getDictionaryVersion());
                initDictionaries(context);
            } else {
                for (DownloadableSettings settings : dictInfo) {
                    settings.delete();
                }
                showDownloadTroubleDialog(context);
            }

        });
        extractor.execute(DownloadableSettings.getZipFile().getAbsolutePath());
    }

    private void showDownloadTroubleDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.dm_dict_alternate_download_address)
                .setPositiveButton(R.string.msg_ok, null)
                .create()
                .show();

        // TODO Add retry button
        // TODO Give details about the error that happened to
    }

    @Override
    public Dictionary getDictionary(int index) {
        return this.dictionaries.get(index);
    }

    @Override
    public int getNbDictionaries() {
        return this.dictionaries.size();
    }

    public interface DictionaryListener {

        void onDictionaryChecked(DictionaryStatus status);

        void onDictionaryLoaded();

        void onCurrentDictionaryChanged(int index);

        void onCurrentMatchChanged(SelectedWord word, Entries match);

    }

    private void fireDictionaryChecked(DictionaryStatus status) {
        if (dictionaryListener == null)
            return;
        dictionaryListener.onDictionaryChecked(status);
    }

    private void fireDictionaryLoaded() {
        if (dictionaryListener == null)
            return;
        dictionaryListener.onDictionaryLoaded();
    }

    private void fireCurrentDictionaryChanged(int index) {
        if (dictionaryListener == null)
            return;
        dictionaryListener.onCurrentDictionaryChanged(index);
    }

    private void fireCurrentMatchChanged(SelectedWord word, Entries match) {
        if (dictionaryListener == null)
            return;
        dictionaryListener.onCurrentMatchChanged(word, match);
    }

    @Override
    public void setDictionaryListener(DictionaryListener dictionaryListener) {
        this.dictionaryListener = dictionaryListener;
    }

    public interface MessageListener {
        void onMessage(int id);

        void onMessage(int id, Object... params);
    }

    public void fireOnMessage(int id) {
        if (this.messageListener != null)
            this.messageListener.onMessage(id);
    }


    public void fireOnMessage(int id, Object... params) {
        if (this.messageListener != null)
            this.messageListener.onMessage(id, params);
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setCurrentDictionary(int index) {
        currentDictionary = index;
        fireCurrentDictionaryChanged(index);
        Pair<SelectedWord, Entries> cacheHit = matchesCaches.get(index);
        if (cacheHit != null) {
            fireCurrentMatchChanged(cacheHit.first, cacheHit.second);
        }
    }

    public Entries query(int dicIndex, SelectedWord word) {
        Dictionary dictionary = this.getDictionary(dicIndex);
        Entries entries;


        Pair<SelectedWord, Entries> cacheHit = matchesCaches.get(dicIndex);
        if (cacheHit != null && cacheHit.first.equals(word)) {
            return cacheHit.second;
        }

        try {
            entries = dictionary.query(word.getText().toString());

            if (entries.size() == 0) {
                entries.add(new DroidEdictEntry("No word found for this dictionary")); // TODO translate
            }
        } catch (DictionaryNotLoadedException e) {
            entries = new Entries();
            entries.add(new DroidEdictEntry("Dictionary not yet loaded")); // TODO translate
        }
        if (dicIndex == currentDictionary) {
            fireCurrentMatchChanged(word, entries);
        }

        matchesCaches.put(dicIndex, new Pair<SelectedWord, Entries>(word, entries));
        return entries;
    }

    @Override
    public Pair<SelectedWord, Entries> getLastMatch(int dicIndex) {
        return matchesCaches.get(dicIndex);
    }

    @Override
    public boolean saveInAnki(AbstractEntry abstractEntry, Context context, SelectedWord selectedWord, String bookTitle) {
        // Currently working only for edict
        if (!(abstractEntry instanceof EdictEntry))
            return false;

        if (AddContentApi.getAnkiDroidPackageName(context) == null) {
            // AnkiDroid not installed
            fireOnMessage(R.string.anki_not_installed);
            return false;
        }


        EdictEntry entry = (EdictEntry) abstractEntry;

        String reqPerm = AddContentApi.checkRequiredPermission(context);
        if (reqPerm != null) {
            // Request permission for Android 6+
            //TODO request the permission as a resolution
            fireOnMessage(R.string.anki_permission_denied);
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
            fireOnMessage(R.string.anki_card_add_fail);
            return false;
        }


        String sentence = selectedWord.getContextSentence().toString();
        String originalWord = entry.getOriginalWord();

        sentence = sentence.replace(originalWord, "<span class=\"emph\">" + originalWord + "</span>");

        String flds[] = {originalWord, entry.getReading(), entry.getGloss(), sentence, entry.getReason(), entry.getWord()};


        // Add a new note using the current field map
        try {
            // Only add item if there aren't any duplicates
            if (!api.checkForDuplicates(mid, did, flds)) {
                String tags = AnkiDroidConfig.TAGS + "," + bookTitle.replaceAll("[^A-Za-z0-9]", "_");
                Uri noteUri = api.addNewNote(mid, did, flds, tags);
                if (noteUri != null) {
                    fireOnMessage(R.string.anki_card_added, flds[0]);
                }
            } else {
                fireOnMessage(R.string.anki_card_already_added);
            }
        } catch (Exception e) {
            Log.e("AnkiCardAdd", "Exception adding cards to AnkiDroid", e);
            fireOnMessage(R.string.anki_card_add_fail);
            return false;
        }
        return true;
    }

    /* Singleton pattern */

    private static DictionaryService instance;

    public static DictionaryService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized DictionaryService getSync() {
        if (instance == null) instance = new DictionaryServiceImpl();
        return instance;
    }

    public static synchronized void reset(){
        instance = null;
    }

}