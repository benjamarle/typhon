package org.zorgblub.rikai.download.settings.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.woxthebox.draglistview.DragItemAdapter;

import net.zorgblub.typhon.R;

import org.zorgblub.rikai.download.settings.DictionarySettings;
import org.zorgblub.rikai.download.settings.DictionaryType;
import org.zorgblub.rikai.download.settings.ui.dialog.DictionaryConfigDialog;

import java.util.ArrayList;

public class DictionaryConfigItemAdapter extends DragItemAdapter<Pair<Integer, DictionarySettings>, DictionaryConfigItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private Activity activity;


    public DictionaryConfigItemAdapter(ArrayList<Pair<Integer, DictionarySettings>> list, int layoutId, int grabHandleId, boolean dragOnIntegerPress, Activity activity) {
        super(dragOnIntegerPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        this.activity = activity;
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

    private IntentCallBack intentCallBack;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( this.intentCallBack != null ) {
            this.intentCallBack.onResult(requestCode, resultCode, data);
        }
    }

    public void editDictionary(DictionarySettings settings, Context context) {
        DictionaryConfigDialog dialog = settings.getConfigDialog(context);
        dialog.setOwnerActivity(this.activity);

        intentCallBack = (requestCode, resultCode, data) -> {
            if(dialog.isShowing())
                dialog.onActivityResult(requestCode, resultCode, data);
        };

        dialog.setOnDismissListener(d -> {
            DictionaryConfigItemAdapter.this.notifyDataSetChanged();
            this.intentCallBack = null;
        });

        dialog.show();
    }

    public class ViewHolder extends DragItemAdapter<Pair<Integer, DictionarySettings>, ViewHolder>.ViewHolder implements View.OnClickListener {
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

            editDictionary(item.second, activity);
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

    public interface IntentCallBack {
        void onResult(int requestCode, int resultCode, Intent data);
    }
}