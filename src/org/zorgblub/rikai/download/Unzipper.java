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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: ray
 * Date: 2013-01-14
 */
public class Unzipper {

	private transient boolean mInterrupt;

	private Set<String> filenames;

	public Unzipper(Set<String> filenames) {
		this.filenames = filenames;
		mInterrupt = false;
	}

	/**
	 * checks whether the zip file has multiple files on the first level
	 * @param input the path of the zip file to be checked
	 * @return true if the zip file has multiple files, false otherwise
	 * @throws IOException
	 */
	public boolean hasMultipleFiles(String input) throws IOException {

		InputStream is = new FileInputStream(input);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

		try {
			ZipEntry entry;
			int count = 0;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory() &&
						  entry.getName().indexOf('/') == entry.getName().length() - 1) {
					// a top level directory will end with a single slash at the end
					// this check if the first and only slash is at the end of the name
					count++;
				}
				if (count > 1) {
					return true;
				}
				zis.closeEntry();
			}
		} finally {
			zis.close();
		}

		return false;
	}

	/**
	 * unzip the specified zip file in the same directory as the zip file
	 *
	 * @param input the zip file to be unzipped
	 * @return true if unzip sucessfully, false otherwise
	 * @throws IOException
	 */
	public boolean unzip(String input) throws IOException {
		//TODO implementes OnProgressListener

		File inputFile = new File(input);
		if (!inputFile.exists() ||  !inputFile.isFile()) {
			return false;
		}

		String outputDir = inputFile.getParentFile().getAbsoluteFile().toString();
		outputDir += outputDir.endsWith("/") ? "" : "/";

		InputStream is = new FileInputStream(input);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

		boolean result = false;

		try {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {

 				String name = ze.getName();
				if (ze.isDirectory()) {
					File dir = new File(outputDir + name);
					dir.mkdir();
					continue;
				}

				if(!filenames.contains(name))
					continue;

				FileOutputStream fos = new FileOutputStream(outputDir + name);
				byte[] buffer = new byte[8192];
				int readcount;

				while ((readcount = zis.read(buffer)) != -1) {
					fos.write(buffer, 0, readcount);
					if (mInterrupt) {
						throw new IOException("User interruption");
					}
				}

				fos.close();
				zis.closeEntry();
			}
			result = true;
		} catch (Exception e) {

		} finally {
			zis.close();
		}
		return result;
	}

	public void interrupt() {
		mInterrupt = true;
	}
}