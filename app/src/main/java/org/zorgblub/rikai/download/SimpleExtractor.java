/*
Copyright (C) 2013 Ray Zhou

JadeRead is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JadeRead is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JadeRead.  If not, see <http://www.gnu.org/licenses/>

Author: Ray Zhou
Date: 2013 04 26

*/
package org.zorgblub.rikai.download;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import net.zorgblub.typhonkai.R;

import java.io.IOException;
import java.util.Set;

/**
 * User: ray
 * Date: 2013-02-05
 * To change this template use File | Settings | File Templates.
 */
public class SimpleExtractor extends AsyncTask<String, Integer, Boolean> implements DialogInterface.OnClickListener {

	private ProgressDialog mProgressDialog;
	private Context mContext;
	private boolean mTaskSuccessful;
	private OnFinishTaskListener mOnFinishTaskListener;
	private Unzipper mUnzipper;


	public SimpleExtractor(Context context, Set<String> filenames) {
		mContext = context;
		mUnzipper = new Unzipper(filenames);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Toast.makeText(mContext, R.string.dm_dict_unzip_success, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, R.string.dm_dict_unzip_failed, Toast.LENGTH_SHORT).show();
		}
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			// must dismiss otherwise you'll get an error in the log
			// Activity com.zyz.mobile.jade.JTextActivity has leaked window com.android.internal.policy.impl.PhoneWindow$DecorView
			// which happens when you try to show a dialog after you exited the activity
		}
		if (mOnFinishTaskListener != null) {
			mOnFinishTaskListener.onFinishTask(mTaskSuccessful);
		}
	}

	@Override
	protected void onCancelled() {
		Toast.makeText(mContext, R.string.dm_dict_unzip_failed, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPreExecute() {
		//invoked on the UI thread before the task is executed

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage(mContext.getString(R.string.dm_dict_unzipping));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.msg_cancel), this);
		mProgressDialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_NEGATIVE) {
			mUnzipper.interrupt();
		}
	}

	@Override
	protected Boolean doInBackground(String... params) {

		if (params.length > 0) {
			try {
				String input = params[0];
				mTaskSuccessful = mUnzipper.unzip(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mTaskSuccessful;
	}


	public void setOnFinishTasklistener(OnFinishTaskListener onFinishTaskListener) {
		mOnFinishTaskListener = onFinishTaskListener;
	}

}
