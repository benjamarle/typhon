/*
 * Copyright (C) 2012 Alex Kuiper
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

package net.zorgblub.typhon;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;

//@ReportsCrashes(formKey = "", // will not be used
//        formUri = "http://acra.pageturner-reader.org/crash",
//        customReportContent = { REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME, ANDROID_VERSION, BRAND, PHONE_MODEL, BUILD, PRODUCT, STACK_TRACE, LOGCAT, PACKAGE_NAME }
//)
// TODO disabled for now so that it does not report crashes to the original dev
// we don't wanna bother him :)
public class Typhon extends Application {

    private static boolean acraInitDone;

    private static Typhon instance;

    public static Typhon get() {
        return instance;
    }


    @Override
    public void onCreate() {
        if (Configuration.IS_EINK_DEVICE) { // e-ink looks better with dark-on-light (esp. Nook Touch where theming breaks light-on-dark
            setTheme(R.style.Theme_AppCompat_Light);

            //This is a work-around because unit-tests call ACRA more than once.
            if (!acraInitDone) {
                ACRA.init(this);
                acraInitDone = true;
            }

        }

        super.onCreate();
        instance = this;
    }

    public static void changeLanguageSetting(Context context, Configuration typhonConfig) {
        android.content.res.Configuration config = new android.content.res.Configuration(
                context.getResources().getConfiguration());

        config.locale = typhonConfig.getLocale();
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

}
