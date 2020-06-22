package net.zorgblub.typhon.view.bookview;

import android.text.Spannable;

import net.zorgblub.typhon.epub.TyphonSpine;
import net.zorgblub.typhon.scheduling.QueueableAsyncTask;

import jedi.functional.Command;
import jedi.option.Option;
import nl.siegmann.epublib.domain.Resource;

import static jedi.functional.FunctionalPrimitives.isEmpty;
import static jedi.option.Options.none;

/**
 * Created by alex on 10/14/14.
 */
public class PreLoadTask extends
        QueueableAsyncTask<Void, Void, Void> {

    private TyphonSpine spine;
    private TextLoader textLoader;

    public PreLoadTask(TyphonSpine spine, TextLoader textLoader ) {
        this.spine = spine;
        this.textLoader = textLoader;
    }

    @Override
    public Option<Void> doInBackground(Void... voids) {
        doInBackground();

        return none();
    }

    private void doInBackground() {
        if ( spine == null ) {
            return;
        }

        Option<Resource> resource = spine.getNextResource();

        resource.forEach((Command<? super Resource>) res -> {
            Option<Spannable> cachedText = textLoader.getCachedTextForResource( res );

            if ( isEmpty(cachedText) ) {
                try {
                    textLoader.getText( res, PreLoadTask.this::isCancelled );
                } catch ( Exception | OutOfMemoryError e ) {
                    //Ignore
                }
            }
        });
    }
}


