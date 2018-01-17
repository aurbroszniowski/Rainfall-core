package io.rainfall.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Aurelien Broszniowski
 */

public class CompressionUtils {

  public synchronized byte[] zipAsByteArray(final File dirToBeCompressed) throws IOException {
    byte[] bytesArray;

    File zipFileName = File.createTempFile("temp", Long.toString(System.nanoTime()));
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
    addDir(dirToBeCompressed, out);
    out.close();

    bytesArray = new byte[(int)zipFileName.length()];

    FileInputStream fis = new FileInputStream(zipFileName);
    fis.read(bytesArray);
    fis.close();

    return bytesArray;
  }

  private void addDir(File dirObj, ZipOutputStream out) throws IOException {
    File[] files = dirObj.listFiles();
    byte[] tmpBuf = new byte[1024];

    if (files != null) {
      for (final File file : files) {
        if (file.isDirectory()) {
          addDir(file, out);
          continue;
        }
        FileInputStream in = new FileInputStream(file.getAbsolutePath());
        System.out.println(" Adding: " + file.getAbsolutePath());
        out.putNextEntry(new ZipEntry(file.getAbsolutePath()));
        int len;
        while ((len = in.read(tmpBuf)) > 0) {
          out.write(tmpBuf, 0, len);
        }
        out.closeEntry();
        in.close();
      }
    }
  }

  public synchronized void byteArrayToZip(final File location, byte[] bytesArray) throws Exception {
    FileOutputStream fos = new FileOutputStream(location);
    fos.write(bytesArray);
    fos.close();
  }
}
