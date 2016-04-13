# RetroLambda
-dontwarn java.lang.invoke.*

# Butterknife rules
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#

-dontwarn org.htmlcleaner.HtmlCleanerForAnt
-dontwarn javax.xml.*
-dontwarn org.jdom.xpath.JaxenXPath*
-dontwarn nl.siegmann.epublib.utilities.StreamWriter*
-dontwarn org.jdom.**
-dontwarn org.rikai.dictionary.db.JdbcSqliteDatabase

-keep class net.nightwhistler.htmlspanner.** {*;}
-dontwarn net.nightwhistler.htmlspanner.**

-dontwarn android.support.**
-keep class org.htmlcleaner.** {*;}

-keep class org.zorgblub.** {*;}