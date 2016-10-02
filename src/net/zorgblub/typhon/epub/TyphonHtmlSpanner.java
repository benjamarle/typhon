package net.zorgblub.typhon.epub;

import android.content.SharedPreferences;
import android.text.SpannableStringBuilder;

import net.nightwhistler.htmlspanner.FontResolver;
import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.SpanStack;
import net.nightwhistler.htmlspanner.TagNodeHandler;
import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.Typhon;
import net.zorgblub.ui.FuriganaSpan;

import org.htmlcleaner.BaseToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Benjamin on 5/7/2016.
 */
public class TyphonHtmlSpanner extends HtmlSpanner implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Inject
    Configuration configuration;

    public TyphonHtmlSpanner() {
        super();
        init();
    }

    public TyphonHtmlSpanner(HtmlCleaner cleaner, FontResolver fontResolver) {
        super(cleaner, fontResolver);
        init();
    }

    private void init() {
        Typhon.getComponent().inject(this);
        registerHandler("rp", new DeleteNodeHandler());
        registerHandler("rt", new DeleteNodeHandler());
        initFurigana();
        configuration.registerOnSharedPreferenceChangeListener(this);
    }

    private void initFurigana() {
        if (configuration.isFuriganaEnabled()) {
            registerHandler("ruby", new FuriganaHandler());
        }else{
            unregisterHandler("ruby");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Configuration.KEY_FURIGANA)){
            initFurigana();
        }
    }

    private static class DeleteNodeHandler extends TagNodeHandler {
        @Override
        public boolean rendersContent() {
            return true;
        }

        @Override
        public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, SpanStack spanStack) {
            // Delete
        }
    }

    private static class FuriganaHandler extends TagNodeHandler {

        @Override
        public void handleTagNode(TagNode node, SpannableStringBuilder builder, int start, int end, SpanStack spanStack) {
            List<? extends BaseToken> allChildren = node.getAllChildren();
            if (allChildren.isEmpty()) {
                return;
            }
            String text = allChildren.get(0).toString();

            TagNode rt = node.findElementByName("rt", false);
            if (rt != null) {
                spanStack.pushSpan(new FuriganaSpan(rt.getText().toString(), text), start, end);
            }
        }
    }



}
