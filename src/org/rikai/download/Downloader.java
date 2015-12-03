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

import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * User: ray
 * Date: 2013-01-13
 */
public class Downloader implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private OnProgressListener mOnProgressListener;

	private transient volatile boolean mInterrupt;

	private Context mContext;

	public Downloader(OnProgressListener p, Context context) {
		mOnProgressListener = p;
		mInterrupt = false;
		this.mContext = context;
	}

	@Override
	public void onConnected(Bundle bundle) {

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	/**
	 * saveURL the content from the specified URL and store in the specified output path
	 * *** Changed signature to private as it's not being used. this method is kept for
	 * future reference only ****
	 *
	 * @param address the url of the content to be downloaded
	 * @param output  the location of the output
	 * @throws IOException
	 */
	private void saveURL(String address, String output) throws IOException {
		URL url = new URL(address);
		URLConnection connection = url.openConnection();
		connection.connect();
		int file_size = connection.getContentLength();

		// will fail when size of file greater than Integer.MAX_VALUE
		// can be fixed by calling the following instead
		// final String contentLengthStr=ucon.getHeaderField("content-length");

		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(output);

		int chunk_size = 8192;
		long pos = 0;
		long count;

		while ((count = fos.getChannel().transferFrom(rbc, pos, chunk_size)) != 0) {
			pos += count;
			if (mOnProgressListener != null) {
				mOnProgressListener.OnProgress(pos, file_size);
			}

		}
		rbc.close();
		fos.close();
	}

	/**
	 * downloads the content from the specified url and save it to the specified output
	 *
	 * @param urlString      the URL of the file to be downloaded
	 * @param outputFilename the local path to save the file to
	 * @throws IOException
	 */
	public void download(String urlString, String outputFilename) throws IOException {
		GoogleApiClient client =  new GoogleApiClient.Builder(mContext)
				.addApi(Drive.API)
				.addScope(Drive.SCOPE_FILE)
				.addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		client.connect();
		DriveFile file = Drive.DriveApi.getFile(client, DriveId.decodeFromString("0B7RvDCZvFbTbTWxBT2lEbVFENGM"));
		file.open(client, DriveFile.MODE_READ_ONLY, null).setResultCallback(driveContentsResult -> {

		});

		BufferedInputStream in = null;
		FileOutputStream fout = null;
		URL url = new URL(urlString);
		try {
			in = new BufferedInputStream(url.openStream());
			fout = new FileOutputStream(outputFilename);

			int bytesread;
			long pos = 0;
			URLConnection c = url.openConnection();
			int size = c.getContentLength(); // The length of the request body in octets (8-bit bytes)

			int chunk_size = 8192;
			byte data[] = new byte[chunk_size];

			while ((bytesread = in.read(data, 0, chunk_size)) != -1) {
				pos += bytesread;
				fout.write(data, 0, bytesread);

				if (mOnProgressListener != null) {
					mOnProgressListener.OnProgress(pos, size);
				}

				if (mInterrupt) {
					throw new IOException("user interrupt");
				}
			}

		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	public void interrupt() {
		mInterrupt = true;
	}

	/**
	 * a method that simulate a download by setting the OnPgoress listener preiodically
	 * this method does not download any content from the given url string
	 * @param urlString ignored
	 * @param output ignored
	 * @throws IOException
	 */
	public void fakedownload(String urlString, String output) throws IOException {
		if (urlString == null || output == null) {
			throw new IOException("");
		}

		for (int i = 0; i < 100; i++) {
			if (mOnProgressListener != null) {
				mOnProgressListener.OnProgress(i, 99);

				if (mInterrupt) {
					throw new IOException("user interrupt");
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
