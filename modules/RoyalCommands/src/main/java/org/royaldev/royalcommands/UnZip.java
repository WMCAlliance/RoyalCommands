/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

    private static final int BUFFER = 2048;

    /**
     * Decompresses a zipped file - respects directories
     *
     * @param fileName          File name of the zipped file
     * @param destinationFolder Folder to unzip in
     */
    public static void decompress(String fileName, String destinationFolder) {
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fileName);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    new File(destinationFolder + File.separator + entry.getName()).mkdir();
                    continue;
                }
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                File f = new File(destinationFolder + File.separator + entry.getName());
                if (!f.exists()) f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(destinationFolder + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) dest.write(data, 0, count);
                dest.flush();
                dest.close();
                is.close();
            }
            zipfile.close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            try {
                if (dest != null) dest.close();
                if (is != null) is.close();
            } catch (IOException ignore) {
            }
        }
    }
}
