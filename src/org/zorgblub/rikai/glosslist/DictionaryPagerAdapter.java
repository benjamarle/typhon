package org.zorgblub.rikai.glosslist;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.zorgblub.typhon.view.bookview.SelectedWord;

import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.Entries;
import org.zorgblub.rikai.DictionaryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin on 22/03/2016.
 */
public class DictionaryPagerAdapter extends PagerAdapter {

    private Context context;

    private SparseArray<DictionaryListView> viewList = new SparseArray<DictionaryListView>();

    private DictionaryService dictionaryService;

    private SelectedWord selectedWord;

    private AdapterView.OnItemLongClickListener longClickListener;

    private AdapterView.OnItemClickListener clickListener;

    public DictionaryPagerAdapter(Context context, DictionaryService dictionaryService){
        this.context = context;
        this.dictionaryService = dictionaryService;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        DictionaryListView dictionaryListView = new DictionaryListView(this.context);
        dictionaryListView.setIndex(position);
        dictionaryListView.setOnItemClickListener(clickListener);
        dictionaryListView.setOnItemLongClickListener(longClickListener);
        if(selectedWord != null){
            updateView(position, dictionaryListView);
        }
        container.addView(dictionaryListView);
        viewList.put(position, dictionaryListView);
        return dictionaryListView;
    }

    private void updateView(int position, DictionaryListView dictionaryListView) {
        Entries query = dictionaryService.query(position, selectedWord);
        dictionaryListView.setResults(query);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        viewList.remove(position);
    }

    @Override
    public int getCount() {
        return dictionaryService.getNbDictionaries();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return this.dictionaryService.getDictionary(position).toString();
    }

    public SelectedWord getSelectedWord() {
        return selectedWord;
    }

    public void setSelectedWord(SelectedWord selectedWord) {
        this.selectedWord = selectedWord;

        for(int i = 0; i < viewList.size(); i++) {
            int key = viewList.keyAt(i);
            // get the object by the key.
            DictionaryListView lv = viewList.get(key);
            updateView(lv.getIndex(), lv);
        }
    }

    public DictionaryListView getView(int location) {
        return viewList.get(location);
    }

    public void setLongClickListener(AdapterView.OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}

