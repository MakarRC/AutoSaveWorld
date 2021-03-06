/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package autosaveworld.threads.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

	private List<String> excludefolders;

	public Zip(List<String> excludefolders) {
		this.excludefolders = excludefolders;
	}

	private ExcludeManager eManager = new ExcludeManager();

	public void ZipFolder(final File srcDir, final File destFile) throws FileNotFoundException, IOException 
	{
		destFile.getParentFile().mkdirs();
		
		BufferedOutputStream bufOutStream = new BufferedOutputStream(new FileOutputStream(destFile));
		try {
			ZipOutputStream zipOutStream = new ZipOutputStream(bufOutStream);
			try {
				zipDir(zipOutStream, srcDir, "");
			} finally {
				zipOutStream.close();
			}
		} finally {
			bufOutStream.close();
		}
	}

	private void zipDir(ZipOutputStream zipOutStream, final File srcDir, String currentDir) throws IOException 
	{
		final File zipDir = new File(srcDir, currentDir);

		for (final String child : zipDir.list()) 
		{
			final File srcFile = new File(zipDir, child);

			if (srcFile.isDirectory()) 
			{
				if (!eManager.isFolderExcluded(excludefolders, srcDir.getName() + File.separator + currentDir + child)) 
				{
					zipDir(zipOutStream, srcDir, currentDir + child + File.separator);
				}
			}
			else
			{
				zipFile(zipOutStream, srcFile, srcDir.getName() + File.separator + currentDir + child);
			}
		}
	}

	private void zipFile(ZipOutputStream zipOutStream, final File srcFile, final String entry) throws IOException 
	{
		if (!srcFile.getName().endsWith(".lck"))
		{
			final InputStream inStream = new FileInputStream(srcFile);
			try {

				final ZipEntry zipEntry = new ZipEntry(entry);
				zipEntry.setTime(srcFile.lastModified());
				zipOutStream.putNextEntry(zipEntry);
				
				final byte[] buf = new byte[4096];
				
				try {
					int len;
					while ((len = inStream.read(buf)) > 0)
					{
						zipOutStream.write(buf, 0, len);
					}
				} finally {
					zipOutStream.closeEntry();
				}
				
			} finally {
				inStream.close();
			}
       		Thread.yield();
		}
	}

}
