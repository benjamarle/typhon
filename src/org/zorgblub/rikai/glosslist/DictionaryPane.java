package org.zorgblub.rikai.glosslist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import net.zorgblub.typhon.R;
import net.zorgblub.typhon.view.bookview.SelectedWord;

import org.rikai.dictionary.AbstractEntry;
import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.Entries;
import org.rikai.dictionary.edict.EdictEntry;
import org.rikai.dictionary.kanji.KanjiDictionary;
import org.zorgblub.rikai.DictionaryService;
import org.zorgblub.rikai.DictionaryServiceImpl;

/**
 * Created by Benjamin on 22/03/2016.
 */
public class DictionaryPane extends DraggablePane implements DictionaryServiceImpl.DictionaryListener {

    private DictionaryService dictionaryService;

    private DictionaryPagerAdapter pagerAdapter;

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

        dictionaryService = DictionaryServiceImpl.get();
        dictionaryService.setDictionaryListener(this);
        dictionaryService.initDictionaries(context);

        dictionaryService.setMessageListener(new DictionaryServiceImpl.MessageListener() {
            @Override
            public void onMessage(int id) {
                Toast.makeText(DictionaryPane.this.getContext(), id, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMessage(int id, Object... params) {
                Toast.makeText(DictionaryPane.this.getContext(), context.getResources().getString(id, params), Toast.LENGTH_LONG).show();
            }
        }); // simple toaster

        // to update the match length when changing page
        viewPager.setOnPageChangeListener(longestMatchUpdater);
    }

    public void setBookReader(BookReader bookReader) {
        this.bookReader = bookReader;
    }

    public void downloadDictionaries(Context context, boolean update){
        int downloadMsg = R.string.dm_dict_message;
        int downloadTitle = R.string.dm_dict_title;
        if(update){
            downloadMsg = R.string.dm_dict_update_message;
            downloadTitle = R.string.dm_dict_update_title;
        }
        DialogInterface.OnClickListener downloadAndExtractFunction = (DialogInterface dialog, int which) -> {
            dictionaryService.downloadAndExtract(dictionaryService.getDownloadableSettings(), context);
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

    private class ClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Pair<SelectedWord, Entries> lastMatch = getLastMatch(view);

            if(lastMatch == null)
                return;

            Entries entries = lastMatch.second;

            Object abstractEntry = entries.get(position);
            if(!(abstractEntry instanceof EdictEntry))
                return;

            EdictEntry edictEntry = (EdictEntry) abstractEntry;

            int kanjiDicIndex = -1;
            for(int i = 0; i < dictionaryService.getNbDictionaries(); i++){
                Dictionary dic = dictionaryService.getDictionary(i);
                if(dic instanceof KanjiDictionary){
                    kanjiDicIndex = i;
                    break;
                }
            }
            if(kanjiDicIndex < 0)
                return;
            SelectedWord word = new SelectedWord(edictEntry.getWord());
            dictionaryService.query(kanjiDicIndex, word);
            pagerAdapter.setSelectedWord(word);
            viewPager.setCurrentItem(kanjiDicIndex);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Pair<SelectedWord, Entries> lastMatch = getLastMatch(view);

            if(lastMatch == null)
                return false;

            SelectedWord selectedWord = lastMatch.first;
            Entries entries = lastMatch.second;
            AbstractEntry e = (AbstractEntry) entries.get(position);
            return dictionaryService.saveInAnki(e, DictionaryPane.this.getContext(), selectedWord,bookReader.getBookTitle());
        }

        private Pair<SelectedWord, Entries> getLastMatch(View v) {
            DictionaryListView view = (DictionaryListView) v.getParent();
            DictionaryListView dlv = view;
            int dicIndex = dlv.getIndex();

            return dictionaryService.getLastMatch(dicIndex);
        }
    }

    @Override
    public void onDictionaryLoaded() {
        DictionaryPagerAdapter dictionaryPagerAdapter = new DictionaryPagerAdapter(this.getContext(), this.dictionaryService);
        ClickListener clickListener = new ClickListener();
        dictionaryPagerAdapter.setClickListener(clickListener);
        dictionaryPagerAdapter.setLongClickListener(clickListener);
        viewPager.setAdapter(dictionaryPagerAdapter);
        this.pagerAdapter = dictionaryPagerAdapter;
    }

    public void onWordChanged(SelectedWord word){
        if(pagerAdapter == null) {
            Toast.makeText(this.getContext(), "Dictionary not ready yet", Toast.LENGTH_SHORT);
            return;
        }
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
