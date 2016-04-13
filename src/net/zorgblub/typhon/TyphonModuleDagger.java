package net.zorgblub.typhon;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.zorgblub.typhon.bookmark.BookmarkDatabaseHelper;
import net.zorgblub.typhon.library.LibraryDatabaseHelper;
import net.zorgblub.typhon.library.LibraryService;
import net.zorgblub.typhon.library.SqlLiteLibraryService;
import net.zorgblub.typhon.scheduling.TaskQueue;
import net.zorgblub.typhon.ssl.EasySSLSocketFactory;
import net.zorgblub.typhon.sync.ProgressService;
import net.zorgblub.typhon.sync.TyphonWebProgressService;
import net.zorgblub.typhon.tts.TTSPlaybackQueue;
import net.zorgblub.typhon.view.bookview.EpubFontResolver;
import net.zorgblub.typhon.view.bookview.TextLoader;
import net.zorgblub.ui.ActionModeBuilder;
import net.zorgblub.ui.DialogFactory;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class TyphonModuleDagger {

    private Typhon application;

    public TyphonModuleDagger(Typhon application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Typhon providesApplication() {
        return application;
    }

    @Provides
    Context providesContext(Typhon typhon) {
        return typhon.getApplicationContext();
    }

    @Provides
    @Singleton
    Configuration providesConfiguration(Context context) {
        return new Configuration(context);
    }

    //bind( LibraryService.class ).to( SqlLiteLibraryService.class );

    @Provides
    @Singleton
    LibraryService providesLibraryService(LibraryDatabaseHelper helper, Configuration config) {
        return new SqlLiteLibraryService(helper, config);
    }

    @Provides
    @Singleton
    LibraryDatabaseHelper providesLibraryDatabaseHelper(Context context){
        return new LibraryDatabaseHelper(context);
    }

    //bind( ProgressService.class ).to( TyphonWebProgressService.class ).in( Singleton.class );
    @Provides
    @Singleton
    ProgressService providesProgressService(Context context, Configuration config, HttpClient client) {
        return new TyphonWebProgressService(context, config, client);
    }


    //bind(TTSPlaybackQueue.class).in(Singleton.class);

    @Provides
    @Singleton
    TTSPlaybackQueue provideTTSPlaybackQueue() {
        return new TTSPlaybackQueue();
    }


    //bind(TextLoader.class).in(Singleton.class);

    @Provides
    @Singleton
    TextLoader providesTextLoader(HtmlSpanner spanner, Context context) {
        return new TextLoader(spanner, context);
    }

    //bind(HighlightManager.class).in(Singleton.class);


    //bind(TaskQueue.class).in(ContextSingleton.class);


    @Provides
    @Singleton
    TaskQueue providesTaskQueue() {
        return new TaskQueue();
    }

    @Provides
    @Singleton
    ActionModeBuilder providesActionModeBuilder() {
        return new ActionModeBuilder();
    }

    @Provides
    HtmlSpanner providesHtmlSpanner() {
        return new HtmlSpanner();
    }

    @Provides
    @Singleton
    BookmarkDatabaseHelper providesBookmarkDatabaseHelper(Context context){
        return new BookmarkDatabaseHelper(context);
    }

    @Provides
    DialogFactory providesDialogFactory(){
        return new DialogFactory();
    }

    @Provides
    @Singleton
    EpubFontResolver providesEpubFontResolver(){
        return new EpubFontResolver();
    }

    // Android services

    @Provides
    NotificationManager providesNotificationManager(Context context){
        return (NotificationManager) context.getSystemService(Typhon.NOTIFICATION_SERVICE);
    }

    @Provides
    TelephonyManager providesTelephonyManager(Context context){
        return (TelephonyManager) context.getSystemService(Typhon.TELEPHONY_SERVICE);
    }

    @Provides
    PowerManager providesPowerManager(Context context){
        return (PowerManager) context.getSystemService(Typhon.POWER_SERVICE);
    }

    @Provides
    AudioManager providesAudioManager(Context context){
        return (AudioManager) context.getSystemService(Typhon.AUDIO_SERVICE);
    }

    @Provides
    DisplayMetrics providesDisplayMetrics(){
        return new DisplayMetrics();
    }




    /**
     * Binds the HttpClient interface to the DefaultHttpClient implementation.
     * <p>
     * In testing we'll use a stub.
     *
     * @return
     */
    @Provides
    public HttpClient providesHttpClient(Configuration config) {
        HttpParams httpParams = new BasicHttpParams();
        DefaultHttpClient client;

        if (config.isAcceptSelfSignedCertificates()) {
            client = new SSLHttpClient(httpParams);
        } else {
            client = new DefaultHttpClient(httpParams);
        }

        for (CustomOPDSSite site : config.getCustomOPDSSites()) {
            if (site.getUserName() != null && site.getUserName().length() > 0) {
                try {
                    URL url = new URL(site.getUrl());
                    client.getCredentialsProvider().setCredentials(
                            new AuthScope(url.getHost(), url.getPort()),
                            new UsernamePasswordCredentials(site.getUserName(), site.getPassword()));
                } catch (MalformedURLException mal) {
                    //skip to the next
                }
            }
        }

        return client;
    }

    public class SSLHttpClient extends DefaultHttpClient {


        public SSLHttpClient(HttpParams params) {
            super(params);
        }

        @Override
        protected ClientConnectionManager createClientConnectionManager() {
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(
                    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
            return new SingleClientConnManager(getParams(), registry);
        }

    }





}
