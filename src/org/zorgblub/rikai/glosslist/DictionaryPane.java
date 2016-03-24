package org.zorgblub.rikai.glosslist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.Toast;

import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.R;
import net.zorgblub.typhon.view.bookview.SelectedWord;

import org.rikai.dictionary.Entries;
import org.zorgblub.rikai.DictionaryService;
import org.zorgblub.rikai.DictionaryServiceImpl;
import org.zorgblub.rikai.download.DictionaryInfo;

/**
 * Created by Benjamin on 22/03/2016.
 */
public class DictionaryPane extends DraggablePane implements DictionaryServiceImpl.DictionaryListener {

    private DictionaryService dictionaryService;

    private DictionaryPagerAdapter pagerAdapter;

    private DictionaryInfo dictionaryInfo;

    private SelectedWord selectedWord;

    private ViewPager viewPager;

    public interface BookReader{
        String getBookTitle();

        void setMatch(SelectedWord word, int length);

        int getHeight();
    }

    private BookReader bookReader;

    public DictionaryPane(Context context) {
        super(context);

        init(context);
    }

    public DictionaryPane(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public DictionaryPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private class LongestMatchUpdater extends ViewPager.SimpleOnPageChangeListener {

        private DictionaryListView currentView;
        @Override
        public void onPageSelected(int position) {
            dictionaryService.setCurrentDictionary(position);
        }
    }

    private LongestMatchUpdater longestMatchUpdater = new LongestMatchUpdater();

    private void init(Context context) {
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        dictionaryInfo = new DictionaryInfo(context);
        dictionaryService = new DictionaryServiceImpl(new Configuration(context));
        dictionaryService.setDictionaryListener(this);
        dictionaryService.initDictionaries(context);

        // to update the match length when changing page
        viewPager.setOnPageChangeListener(longestMatchUpdater);
    }

    public void setBookReader(BookReader bookReader) {
        this.bookReader = bookReader;
    }

    /* TODO convert these features

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = parent.getAdapter().getItem(position);

        Entries<AbstractEntry> entries = (Entries) ssView.getTag(R.string.tag_word_list);
        AbstractEntry entry = entries.get(position);

        if (!(entry instanceof EdictEntry)) return;

        EdictEntry edictEntry = (EdictEntry) entry;
        String word = edictEntry.getWord();
        Entries<KanjiEntry> query = kanjiDictionary.query(word, false);
        if (query.isEmpty()) {
            Toast.makeText(context, R.string.no_kanji_found, Toast.LENGTH_SHORT).show();
            return;
        }
        showDefinition(query);
    }




    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AbstractEntry item = (AbstractEntry) parent.getAdapter().getItem(position);
        if(!(item instanceof EdictEntry))
            return false;


        dictionaryService.saveInAnki(item, this.getContext());
        return true;
    }*/


    public void downloadDictionaries(Context context, boolean update){
        int downloadMsg = R.string.dm_dict_message;
        int downloadTitle = R.string.dm_dict_title;
        if(update){
            downloadMsg = R.string.dm_dict_update_message;
            downloadTitle = R.string.dm_dict_update_title;
        }
        DialogInterface.OnClickListener downloadAndExtractFunction = (DialogInterface dialog, int which) -> {
            dictionaryService.downloadAndExtract(dictionaryInfo, context);
        };
        new AlertDialog.Builder(context)
                .setTitle(downloadTitle)
                .setMessage(downloadMsg)
                .setPositiveButton(R.string.dm_dict_yes, downloadAndExtractFunction)
                .setNegativeButton(R.string.dm_dict_no, null)
                .create()
                .show();
    }

    @Override
    public void onDictionaryChecked(DictionaryServiceImpl.DictionaryStatus status) {
        switch (status){
            case NOT_EXISTENT:
                downloadDictionaries(this.getContext(), false);
                break;
            case UPDATE_NEEDED:
                downloadDictionaries(this.getContext(), true);
                break;
        }
    }

    @Override
    public void onDictionaryLoaded() {
        DictionaryPagerAdapter dictionaryPagerAdapter = new DictionaryPagerAdapter(this.getContext(), this.dictionaryService);
        viewPager.setAdapter(dictionaryPagerAdapter);
        this.pagerAdapter = dictionaryPagerAdapter;
    }

    public void onWordChanged(SelectedWord word){
        if(pagerAdapter == null) {
            Toast.makeText(this.getContext(), "Dictionary not ready yet", Toast.LENGTH_SHORT);
            return;
        }
        this.selectedWord = word;
        pagerAdapter.setSelectedWord(word);
        pagerAdapter.notifyDataSetChanged();
        showPane();
    }

    @Override
    public void onCurrentDictionaryChanged(int index) {
        //
    }

    @Override
    public void onCurrentMatchChanged(SelectedWord word, Entries match) {
        bookReader.setMatch(word, match.getMaxLen());
    }

    public void showPane() {
        if (!this.isDisplaying() || this.getHeight() < 10) {
            this.setHeight((int) (bookReader.getHeight() / 2.6));
            this.reveal();
        }
    }


}
