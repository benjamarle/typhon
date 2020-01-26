package org.zorgblub.rikai.glosslist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import net.zorgblub.typhonkai.R;
import net.zorgblub.typhonkai.view.bookview.SelectedWord;

import org.rikai.dictionary.Dictionary;
import org.rikai.dictionary.Entries;
import org.zorgblub.rikai.DictionaryService;
import org.zorgblub.rikai.DroidEpwingDictionary;
import org.zorgblub.rikai.SpannableHook;

/**
 * Created by Benjamin on 22/03/2016.
 */
public class DictionaryPagerAdapter extends PagerAdapter {

    private Context context;

    private SparseArray<ViewHolder> viewList = new SparseArray<ViewHolder>();

    private DictionaryService dictionaryService;

    private SelectedWord selectedWord;

    private AdapterView.OnItemLongClickListener longClickListener;

    private AdapterView.OnItemClickListener clickListener;

    public DictionaryPagerAdapter(Context context, DictionaryService dictionaryService) {
        this.context = context;
        this.dictionaryService = dictionaryService;
    }

    public class ViewHolder {

        public View parentView;

        public DictionaryListView dictionaryListView;

        public ProgressBar progressBar;

        void startProgress(){
            dictionaryListView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        void stopProgress(){
            progressBar.setVisibility(View.GONE);
            dictionaryListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dictionary_loading, null);

        DictionaryListView dictionaryListView = (DictionaryListView) view.findViewById(R.id.dictionary_list_view);
        dictionaryListView.setIndex(position);
        dictionaryListView.setOnItemClickListener(clickListener);
        dictionaryListView.setOnItemLongClickListener(longClickListener);

        Dictionary dictionary = dictionaryService.getDictionary(position);
        if(dictionary instanceof DroidEpwingDictionary){
            DroidEpwingDictionary epwingDictionary = (DroidEpwingDictionary) dictionary;
            SpannableHook hook = (SpannableHook) epwingDictionary.getHook();
            dictionaryListView.setSizeChangeListener(hook);
        }

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.dictionary_loading_progress);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.dictionaryListView = dictionaryListView;
        viewHolder.progressBar = progressBar;
        viewHolder.parentView = view;

        if (selectedWord != null) {
            updateView(position, viewHolder);
        }

        container.addView(view);
        viewList.put(position, viewHolder);
        return viewHolder;
    }


    private class QueryTask extends AsyncTask<ViewHolder, Void, Entries> {

        private ViewHolder v;

        private int position;

        public QueryTask(ViewHolder v, int position) {
            this.v = v;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            v.startProgress();
        }

        @Override
        protected void onPostExecute(Entries entries) {
            super.onPostExecute(entries);

            v.dictionaryListView.setResults(entries);
            v.stopProgress();

        }

        @Override
        protected void onCancelled(Entries entries) {
            super.onCancelled(entries);
        }

        @Override
        protected Entries doInBackground(ViewHolder... params) {
            Entries query = dictionaryService.query(position, selectedWord);

            return query;
        }
    }

    private void updateView(int position, ViewHolder viewHolder) {
        QueryTask queryTask = new QueryTask(viewHolder, position);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
            queryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            queryTask.execute();
        }

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewHolder viewHolder = (ViewHolder) object;
        container.removeView(viewHolder.parentView);
        viewList.remove(position);
    }

    @Override
    public int getCount() {
        return dictionaryService.getNbDictionaries();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewHolder viewHolder = (ViewHolder) object;
        return view == viewHolder.parentView;
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

        for (int i = 0; i < viewList.size(); i++) {
            int key = viewList.keyAt(i);
            // get the object by the key.
            ViewHolder lv = viewList.get(key);
            updateView(key, lv);
        }
    }

    public DictionaryListView getView(int location) {
        return viewList.get(location).dictionaryListView;
    }

    public void setLongClickListener(AdapterView.OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setClickListener(AdapterView.OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}

