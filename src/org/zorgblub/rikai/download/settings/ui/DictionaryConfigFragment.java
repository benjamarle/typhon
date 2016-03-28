package org.zorgblub.rikai.download.settings.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import net.zorgblub.typhon.R;

import org.zorgblub.rikai.DictionaryService;
import org.zorgblub.rikai.DictionaryServiceImpl;
import org.zorgblub.rikai.download.settings.DictionarySettings;
import org.zorgblub.rikai.download.settings.DictionaryType;

import java.util.ArrayList;
import java.util.List;

public class DictionaryConfigFragment extends Fragment {

    private DragListView dictionaryListView;

    private ImageButton addButton;

    private DictionaryService dictionaryService;

    private ArrayList<Pair<Integer, DictionarySettings>> list;

    public static DictionaryConfigFragment newInstance() {
        return new DictionaryConfigFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dictionary_list_fragment, container, false);
        dictionaryListView = (DragListView) view.findViewById(R.id.drag_list_view);
        dictionaryListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        dictionaryListView.setDragEnabled(true);


        addButton = (ImageButton) view.findViewById(R.id.dictionary_add_button);
        addButton.setOnClickListener(v -> {
            addDictionary();
        });

        dictionaryService = DictionaryServiceImpl.get();
        initDictionaryList();
        return view;
    }

    public void addDictionary(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dictionary_choose_type)
                .setItems(DictionaryType.getNames(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        addDictionary(DictionaryType.values()[which]);
                        dialog.dismiss();
                    }
                });
         builder.create().show();
    }

    public void addDictionary(DictionaryType type){
        DictionaryConfigItemAdapter adapter = (DictionaryConfigItemAdapter)this.dictionaryListView.getAdapter();
        adapter.addDictionary(type);
        Toast.makeText(this.getContext(), getResources().getString(R.string.dictionary_add_success, type.getName()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        
        saveConfig();
    }
    
    private void saveConfig(){
        if(list == null)
            return;
        List<DictionarySettings> settings = new ArrayList<>();
        for (Pair<Integer, DictionarySettings> pair: list){
            DictionarySettings s = pair.second;
            settings.add(s);
        }
        dictionaryService.saveSettings(settings);
    }

    private void initDictionaryList() {
        List<DictionarySettings> settings = dictionaryService.getSettings();
        list = new ArrayList<>();
        for (int i = 0; i < settings.size(); i++) {
            DictionarySettings s = settings.get(i);
            list.add(new Pair<>(i, s));
        }
        dictionaryListView.setLayoutManager(new LinearLayoutManager(getContext()));
        DictionaryConfigItemAdapter listAdapter = new DictionaryConfigItemAdapter(list, R.layout.dictionary_list_item, R.id.dictionay_item_image, false);
        dictionaryListView.setAdapter(listAdapter, true);
        dictionaryListView.setCanDragHorizontally(false);
        dictionaryListView.setCustomDragItem(new MyDragItem(getContext(), R.layout.dictionary_list_item));
    }


    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.dictionary_item_text)).getText();
            ((TextView) dragView.findViewById(R.id.dictionary_item_text)).setText(text);
            dragView.setBackgroundColor(dragView.getResources().getColor(R.color.dark_grey));
        }
    }
}