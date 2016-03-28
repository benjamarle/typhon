package org.zorgblub.rikai.download.settings.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import net.zorgblub.typhon.R;

import org.rikai.dictionary.wordnet.Lang;
import org.zorgblub.rikai.download.settings.DictionarySettings;
import org.zorgblub.rikai.download.settings.DictionaryType;
import org.zorgblub.rikai.download.settings.KanjidicSettings;
import org.zorgblub.rikai.download.settings.WordnetSettings;

import java.util.ArrayList;

public class DictionaryConfigItemAdapter extends DragItemAdapter<Pair<Integer, DictionarySettings>, DictionaryConfigItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;

    public DictionaryConfigItemAdapter(ArrayList<Pair<Integer, DictionarySettings>> list, int layoutId, int grabHandleId, boolean dragOnIntegerPress) {
        super(dragOnIntegerPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position).second.toString();
        holder.textView.setText(text);
        holder.itemView.setTag(text);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).first;
    }

    public Pair<Integer, DictionarySettings> getItem(int position) {
        return mItemList.get(position);
    }

    public void addDictionary(DictionaryType settings) {
        DictionarySettings implementation = settings.getImplementation();
        int itemCount = this.getItemCount();
        Pair<Integer, DictionarySettings> newEntry = new Pair<>(itemCount, implementation);
        this.addItem(itemCount, newEntry);
    }

    public void editDictionary(DictionarySettings settings, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.dictionary_edit_title);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView;
        
        DictionaryType type = settings.getType();

        switch (type) {
            case EDICT:
                dialogView = inflater.inflate(R.layout.dictionary_edict_settings, null);
                break;
            case KANJIDIC:
                dialogView = inflater.inflate(R.layout.dictionary_kanjidic_settings, null);
                break;
            case ENAMDICT:
                dialogView = inflater.inflate(R.layout.dictionary_enamdict_settings, null);
                break;
            case WORDNET:
                dialogView = inflater.inflate(R.layout.dictionary_wordnet_settings, null);
                break;
            default:
                dialogView = inflater.inflate(R.layout.dictionary_edict_settings, null);
                break;
        }
        
        builder.setView(dialogView);


        EditText nameView = (EditText) dialogView.findViewById(R.id.dictionary_name);

        nameView.setText(settings.getName());
        TextView typeView = (TextView) dialogView.findViewById(R.id.dictionary_type);
        typeView.setText(settings.getType().getName());

        switch(type){
            case KANJIDIC:
                KanjidicSettings kanjiDictionary = (KanjidicSettings) settings;
                CheckBox heisig6 = (CheckBox) dialogView.findViewById(R.id.dictionary_edit_form_heisig6);
                heisig6.setChecked(kanjiDictionary.isHeisig6());
                heisig6.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    kanjiDictionary.setHeisig6(isChecked);
                });
            case WORDNET:
                WordnetSettings wordnetSettings = (WordnetSettings) settings;
                Spinner lang = (Spinner) dialogView.findViewById(R.id.dictionary_lang);
                ArrayAdapter<Lang> adapter = new ArrayAdapter<>(context, R.layout.dictionary_lang_item, Lang.values());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                lang.setAdapter(adapter);
                lang.setSelection(wordnetSettings.getLang().ordinal());

                lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        wordnetSettings.setLang(Lang.values()[position]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // not supposed to happen
                    }
                });
        }

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> {
            settings.setName(nameView.getText().toString());

            DictionaryConfigItemAdapter.this.notifyDataSetChanged();

        });

        dialog.show();
    }

    public class ViewHolder extends DragItemAdapter<Pair<Integer, DictionarySettings>, DictionaryConfigItemAdapter.ViewHolder>.ViewHolder implements View.OnClickListener {
        public TextView textView;

        public ImageButton imageButton;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);
            textView = (TextView) itemView.findViewById(R.id.dictionary_item_text);
            imageButton = (ImageButton) itemView.findViewById(R.id.dictionary_item_remove);
            imageButton.setOnClickListener(this);
        }

        @Override
        public void onItemClicked(View view) {
            // edit
            int position = ViewHolder.this.getAdapterPosition();
            Pair<Integer, DictionarySettings> item = getItem(position);

            editDictionary(item.second, view.getContext());
        }

        @Override
        public boolean onItemLongClicked(View view) {
            // nothing to be done
            return false;
        }

        @Override
        public void onClick(View v) {
            // remove
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext())
                    .setCancelable(true)
                    .setMessage(v.getResources().getString(R.string.dictionary_confirm_delete_msg, textView.getText()))
                    .setTitle(R.string.dictionary_confirm_delete_title)
                    .setIcon(R.drawable.cross)
                    .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DictionaryConfigItemAdapter.this.removeItem(ViewHolder.this.getAdapterPosition());

                            Toast.makeText(v.getContext(), R.string.dictionary_remove_success, Toast.LENGTH_SHORT);
                        }
                    }).setNegativeButton(R.string.action_cancel, null);
            builder.show();
        }
    }
}