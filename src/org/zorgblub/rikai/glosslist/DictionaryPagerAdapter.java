package org.zorgblub.rikai.glosslist;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.Entries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jedi.functional.FunctionalPrimitives.forEach;

/**
 * Created by Benjamin on 22/03/2016.
 */
public class DictionaryPagerAdapter extends PagerAdapter {

    private Context context;

    private List<Dictionary> dictionaries;

    private Set<DictionaryListView> viewSet = new HashSet<DictionaryListView>();

    private CharSequence currentWord;

    public DictionaryPagerAdapter(Context context, List<Dictionary> dictionaries) {
        this.context = context;
        this.dictionaries = dictionaries;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Dictionary dic = dictionaries.get(position);
        DictionaryListView dictionaryListView = new DictionaryListView(this.context, dic);
        if(currentWord != null){
            dictionaryListView.search(currentWord.toString());
        }
        container.addView(dictionaryListView);
        viewSet.add(dictionaryListView);
        return dictionaryListView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        viewSet.remove(object);
    }

    @Override
    public int getCount() {
        return dictionaries.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "View : "+position;
    }

    public Entries search(CharSequence word){
        currentWord = word;
        Entries query = new Entries();
        forEach(viewSet, (view) ->{
            Entries currentQuery = view.search(word.toString());
            query.addAll(currentQuery);
            query.setMaxLen(Math.max(currentQuery.getMaxLen(), query.getMaxLen()));
        });
        return query;
    }


}

