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
package net.zorgblub.typhonkai.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.zorgblub.typhonkai.Configuration;
import net.zorgblub.typhonkai.R;
import net.zorgblub.typhonkai.Typhon;

import javax.inject.Inject;

public class FileBrowseActivity extends AppCompatActivity {

	@Inject
	Configuration config;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Typhon.getComponent().inject(this);

		Typhon.changeLanguageSetting(this, config);
		setTheme( config.getTheme() );
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browse);
	}
}
