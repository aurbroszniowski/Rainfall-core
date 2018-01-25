package io.rainfall.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/**
 * @author Aurelien Broszniowski
 */

public class CompressionUtils {

  public synchronized byte[] zipAsByteArray(final File dirToBeCompressed) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream out = new ZipOutputStream(baos);
    try {
      addDir(dirToBeCompressed, out, new File("."));
    } finally {
      out.close();
    }
    return baos.toByteArray();
  }

  private void addDir(File dirObj, ZipOutputStream out, File parentPath) throws IOException {
    File[] files = dirObj.listFiles();
    byte[] tmpBuf = new byte[1024];

    if (files != null) {
      for (final File file : files) {
        if (file.isDirectory()) {
          addDir(file, out, new File(parentPath.getPath() + "/" + file.getName()));
          continue;
        }
        FileInputStream in = new FileInputStream(file.getAbsolutePath());
        try {
          System.out.println(" Adding: " + file.getAbsolutePath());
          out.putNextEntry(new ZipEntry(parentPath.getPath() + "/" + file.getName()));
          int len;
          while ((len = in.read(tmpBuf)) > 0) {
            out.write(tmpBuf, 0, len);
          }
          out.closeEntry();
        } finally {
          in.close();
        }
      }
    }
  }

  public synchronized void byteArrayToPath(final File location, byte[] compressedData) throws Exception {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
    ZipInputStream in = new ZipInputStream(inputStream);
    while (true)
    {
      ZipEntry nextEntry = in.getNextEntry();
      if (nextEntry == null) {
        break;
      }
      if (nextEntry.isDirectory()) {
        new File(location, nextEntry.getName()).mkdirs();
      } else {
        new File(location, nextEntry.getName()).getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(location, nextEntry.getName()));
        try {
          extractFile(in, fileOutputStream);
        } finally {
          fileOutputStream.close();
        }
      }
    }
  }

  private void extractFile(final ZipInputStream in, final FileOutputStream fileOutputStream) throws IOException {
    byte[] buffer = new byte[4096];
    while (true) {
      int bytescount = in.read(buffer);
      if (bytescount == -1) {
        break;
      }
      fileOutputStream.write(buffer, 0, bytescount);
    }
  }


  public synchronized void byteArrayToZip(final File location, byte[] bytesArray) throws Exception {
    FileOutputStream fos = new FileOutputStream(location);
    try {
      fos.write(bytesArray);
    } finally {
      fos.close();
    }
  }
}
