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
package org.rikai.download;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import net.rikaiwhistler.pageturner.R;

import java.io.IOException;

public class SimpleDownloader extends AsyncTask<String, Integer, Boolean>
		implements OnProgressListener, DialogInterface.OnClickListener {

	private ProgressDialog mProgressDialog;
	private Context mContext;
	private boolean mTaskSuccessful;
	private OnFinishTaskListener mOnFinishTaskListener;
	private Downloader mDownloader;

	public SimpleDownloader(Context context) {
		mContext = context;
		mDownloader = new Downloader(this);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mProgressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Toast.makeText(mContext, R.string.dm_dict_success, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, R.string.dm_dict_failed, Toast.LENGTH_SHORT).show();
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
		Toast.makeText(mContext, R.string.dm_dict_failed, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPreExecute() {
		//invoked on the UI thread before the task is executed

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage(mContext.getString(R.string.dm_dict_downloading));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(100);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.msg_cancel), this);
		mProgressDialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_NEGATIVE) {
			mDownloader.interrupt();
		}
	}

	@Override
	protected Boolean doInBackground(String... params) {

		if (params.length >= 2) {

			try {
				String url = params[0];
				String output = params[1];
				mDownloader.download(url, output);
				mTaskSuccessful = true;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mTaskSuccessful;
	}

	@Override
	public void OnProgress(long pos, long length) {
		int progress = (int) ((float) pos / (float) length * 100);
		publishProgress(progress);
	}

	public void setOnFinishTaskListener(OnFinishTaskListener onFinishTaskListener) {
		mOnFinishTaskListener = onFinishTaskListener;
	}


}
