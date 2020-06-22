/*
 * Copyright (C) 2013 Alex Kuiper
 *
 * This file is part of PageTurner
 *
 * PageTurner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PageTurner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PageTurner.  If not, see <http://www.gnu.org/licenses/>.*
 */

package net.zorgblub.typhon.view.bookview;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;

import net.nightwhistler.htmlspanner.FontFamily;
import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.TagNodeHandler;
import net.nightwhistler.htmlspanner.css.CSSCompiler;
import net.nightwhistler.htmlspanner.css.CompiledRule;
import net.zorgblub.typhon.Configuration;
import net.zorgblub.typhon.view.FastBitmapDrawable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import jedi.option.Option;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.util.IOUtil;

import static jedi.functional.FunctionalPrimitives.isEmpty;
import static jedi.option.Options.none;
import static jedi.option.Options.option;

/**
 * Singleton storage for opened book and rendered text.
 *
 * Optimization in case of rotation of the screen.
 */
public class TextLoader implements LinkTagHandler.LinkCallBack {

    /**
     * We start clearing the cache if memory usage exceeds 75%.
     */
    private static final double CACHE_CLEAR_THRESHOLD = 0.75;

    private String currentFile;
    private Book currentBook;
    private Map<String, Spannable> renderedText = new HashMap<>();
    private Map<String, List<CompiledRule>> cssRules = new HashMap<>();

    private Map<String, FastBitmapDrawable> imageCache = new HashMap<>();


    private Map<String, Map<String, Integer>> anchors = new HashMap<>();
    private List<AnchorHandler> anchorHandlers = new ArrayList<>();

    private static final Logger LOG = LoggerFactory.getLogger("TextLoader");

    private HtmlSpanner htmlSpanner;
    private EpubFontResolver fontResolver;

    private LinkTagHandler.LinkCallBack linkCallBack;

    @Inject
    public TextLoader(HtmlSpanner spanner, Context context){
        this.fontResolver = new EpubFontResolver(this, context);
        setHtmlSpanner(spanner);
    }

    public void setHtmlSpanner(HtmlSpanner spanner) {
        this.htmlSpanner = spanner;
        this.htmlSpanner.setFontResolver(fontResolver);

        spanner.registerHandler("a", registerAnchorHandler(new LinkTagHandler(this)));

        spanner.registerHandler("h1",
                registerAnchorHandler(spanner.getHandlerFor("h1")));
        spanner.registerHandler("h2",
                registerAnchorHandler(spanner.getHandlerFor("h2")));
        spanner.registerHandler("h3",
                registerAnchorHandler(spanner.getHandlerFor("h3")));
        spanner.registerHandler("h4",
                registerAnchorHandler(spanner.getHandlerFor("h4")));
        spanner.registerHandler("h5",
                registerAnchorHandler(spanner.getHandlerFor("h5")));
        spanner.registerHandler("h6",
                registerAnchorHandler(spanner.getHandlerFor("h6")));

        spanner.registerHandler("p",
                registerAnchorHandler(spanner.getHandlerFor("p")));


        spanner.registerHandler("link", new CSSLinkHandler(this));


    }

    public void setFontResolver( EpubFontResolver resolver ) {
        this.fontResolver = resolver;
        this.htmlSpanner.setFontResolver( fontResolver );
    }

    public void registerCustomFont( String name, String href ) {

        LOG.debug( "Registering custom font " + name + " with href " + href );

        this.fontResolver.loadEmbeddedFont(name, href);
    }

    public List<CompiledRule> getCSSRules( String href ) {

        if ( this.cssRules.containsKey(href) ) {
            return Collections.unmodifiableList(cssRules.get(href));
        }

        List<CompiledRule> result = new ArrayList<>();

        if ( currentBook == null ) {
            return result;
        }

        String strippedHref = href.substring( href.lastIndexOf('/') + 1);

        Resource res = null;

        for ( Resource resource: this.currentBook.getResources().getAll() ) {
            if ( resource.getHref().endsWith(strippedHref) ) {
                res = resource;
                break;
            }
        }

        if ( res == null ) {
            LOG.error("Could not find CSS resource " + strippedHref );
            return new ArrayList<>();
        }

        StringWriter writer = new StringWriter();
        try {
            IOUtil.copy(res.getReader(), writer);

            List<Rule> rules = CSSParser.parse(writer.toString());
            LOG.debug("Parsed " + rules.size() + " raw rules.");

            for ( Rule rule: rules ) {

                if ( rule.getSelectors().size() == 1 && rule.getSelectors().get(0).toString().equals("@font-face")) {
                    handleFontLoadingRule(rule);
                } else {
                    result.add(CSSCompiler.compile(rule, htmlSpanner));
                }
            }

        } catch (IOException io) {
            LOG.error("Error while reading resource", io);
            return new ArrayList<>();
        } catch (Exception e) {
            LOG.error("Error reading CSS file", e);
        } finally {
            res.close();
        }

        cssRules.put(href, result);

        LOG.debug("Compiled " + result.size() + " CSS rules.");

        return result;
    }

    public void invalidateCachedText() {
        this.renderedText.clear();
    }

    private void handleFontLoadingRule(Rule rule) {

        String href = null;
        String fontName= null;

        for (PropertyValue prop: rule.getPropertyValues() ) {
            if ( prop.getProperty().equals("font-family") ) {
                fontName = prop.getValue();
            }

            if ( prop.getProperty().equals("src") ) {
                href = prop.getValue();
            }
        }

        if ( fontName.startsWith("\"") && fontName.endsWith("\"")) {
            fontName = fontName.substring(1, fontName.length() -1 );
        }

        if ( fontName.startsWith("\'") && fontName.endsWith("\'")) {
            fontName = fontName.substring(1, fontName.length() -1 );
        }

        if ( href.startsWith("url(") ) {
            href = href.substring( 4, href.length() -1 );
        }

        registerCustomFont(fontName, href);

    }

    private AnchorHandler registerAnchorHandler( TagNodeHandler wrapThis ) {
        AnchorHandler handler = new AnchorHandler(wrapThis);
        anchorHandlers.add(handler);
        return handler;
    }

    @Override
    public void linkClicked(String href) {
        if ( linkCallBack != null ) {
            linkCallBack.linkClicked(href);
        }
    }

    public void setLinkCallBack( LinkTagHandler.LinkCallBack callBack ) {
        this.linkCallBack = callBack;
    }

    public void registerTagNodeHandler( String tag, TagNodeHandler handler ) {
        this.htmlSpanner.registerHandler(tag, handler);
    }

    public boolean hasCachedBook( String fileName ) {
        return fileName != null && fileName.equals( currentFile );
    }


    public Book initBook(String fileName) throws IOException {

        if (fileName == null) {
            throw new IOException("No file-name specified.");
        }

        if ( hasCachedBook( fileName ) ) {
            LOG.debug("Returning cached Book for fileName " + currentFile );
            return currentBook;
        }

        closeCurrentBook();

        this.anchors = new HashMap<>();

        // read epub file
        EpubReader epubReader = new EpubReader();

        Book newBook = epubReader.readEpubLazy(fileName, "UTF-8");

        this.currentBook = newBook;
        this.currentFile = fileName;

        return newBook;

    }

    public Option<Integer> getAnchor( String href, String anchor ) {
        if ( this.anchors.containsKey(href) ) {
            Map<String, Integer> nestedMap = this.anchors.get( href );
            return option(nestedMap.get(anchor));
        }

        return none();
    }

    public Book getCurrentBook() {
        return this.currentBook;
    }

    public void setFontFamily(FontFamily family) {
        this.fontResolver.setDefaultFont(family);
    }

    public void setSerifFontFamily(FontFamily family) {
        this.fontResolver.setSerifFont(family);
    }

    public void setSansSerifFontFamily(FontFamily family) {
        this.fontResolver.setSansSerifFont(family);
    }

    public void setStripWhiteSpace(boolean stripWhiteSpace) {
        this.htmlSpanner.setStripExtraWhiteSpace(stripWhiteSpace);
    }

    public void setAllowStyling(boolean allowStyling) {
        this.htmlSpanner.setAllowStyling(allowStyling);
    }

    public void setUseColoursFromCSS( boolean useColours ) {
        this.htmlSpanner.setUseColoursFromStyle(useColours);
    }

    public FastBitmapDrawable getCachedImage( String href ) {
        return imageCache.get( href );
    }

    public boolean hasCachedImage( String href ) {
        return imageCache.containsKey(href);
    }

    public void storeImageInChache( String href, FastBitmapDrawable drawable ) {
        this.imageCache.put(href, drawable);
    }

    private void registerNewAnchor(String href, String anchor, int position ) {
        if ( ! anchors.containsKey(href)) {
            anchors.put(href, new HashMap<>());
        }

        anchors.get(href).put(anchor, position);
    }

    public Option<Spannable> getCachedTextForResource( Resource resource ) {

        LOG.debug( "Checking for cached resource: " + resource );

        return option(renderedText.get(resource.getHref()));
    }

    public Spannable getText( final Resource resource,
                              HtmlSpanner.CancellationCallback cancellationCallback ) throws IOException {

        Option<Spannable> cached = getCachedTextForResource( resource );

        if ( ! isEmpty(cached) ) {
            return cached.unsafeGet();
        }

        for ( AnchorHandler handler: this.anchorHandlers ) {
            handler.setCallback((anchor, position) ->
                    registerNewAnchor(resource.getHref(), anchor, position));
        }

        double memoryUsage = Configuration.getMemoryUsage();
        double bitmapUsage = Configuration.getBitmapMemoryUsage();

        LOG.debug("Current memory usage is " +  (int) (memoryUsage * 100) + "%" );
        LOG.debug("Current bitmap memory usage is " +  (int) (bitmapUsage * 100) + "%" );

        //If memory usage gets over the threshold, try to free up memory
        if ( memoryUsage > CACHE_CLEAR_THRESHOLD || bitmapUsage > CACHE_CLEAR_THRESHOLD) {

            LOG.debug("Clearing cached resources.");

            clearCachedText();
            closeLazyLoadedResources();
        }

        boolean shouldClose = false;
        Resource res = resource;

        //If it's already in memory, use that. If not, create a copy
        //that we can safely close after using it
        if ( ! resource.isInitialized() ) {
            res = new Resource( this.currentFile, res.getSize(), res.getOriginalHref() );
            shouldClose = true;
        }

        Spannable result = new SpannableString("");

        try {
            result = htmlSpanner.fromHtml(res.getReader(), cancellationCallback);
            renderedText.put(res.getHref(), result);
        } catch (Exception e) {
            LOG.error("Caught exception while rendering text", e);
            result = new SpannableString( e.getClass().getSimpleName() + ": " + e.getMessage() );
        }
        finally {
            if ( shouldClose ) {
                //We have the rendered version, so it's safe to close the resource
                resource.close();
            }
        }

        return result;
    }

    private void closeLazyLoadedResources() {
        if ( currentBook != null ) {
            for ( Resource res: currentBook.getResources().getAll() ) {
                res.close();
            }
        }
    }

    public void clearCachedText() {
        clearImageCache();
        anchors.clear();

        renderedText.clear();
        cssRules.clear();
    }

    public void closeCurrentBook() {

        if ( currentBook != null ) {
            for ( Resource res: currentBook.getResources().getAll() ) {
                res.setData(null); //Release the byte[] data.
            }
        }

        currentBook = null;
        currentFile = null;
        renderedText.clear();
        clearImageCache();
        anchors.clear();
    }

    public void clearImageCache() {
        for (Map.Entry<String, FastBitmapDrawable> draw : imageCache.entrySet()) {
            draw.getValue().destroy();
        }

        imageCache.clear();
    }




}
