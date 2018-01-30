package io.rainfall.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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

  /**
   * extract the subdirectory from a jar on the classpath to {@code writeDirectory}
   *
   * @param sourceDirectory directory (in a jar on the classpath) to extract
   * @param writeDirectory  the location to extract to
   * @throws IOException if an IO exception occurs
   */
  public void extractFromJar(String sourceDirectory, String writeDirectory) throws IOException {
    final URL dirURL = getClass().getResource(sourceDirectory);
    final String path = sourceDirectory.substring(1);

    if ((dirURL != null) && dirURL.getProtocol().equals("jar")) {
      final JarURLConnection jarConnection = (JarURLConnection)dirURL.openConnection();
      System.out.println("jarConnection is " + jarConnection);

      final ZipFile jar = jarConnection.getJarFile();

      final Enumeration<? extends ZipEntry> entries = jar.entries(); // gives ALL entries in jar

      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final String name = entry.getName();
        // System.out.println( name );
        if (!name.startsWith(path)) {
          // entry in wrong subdir -- don't copy
          continue;
        }
        final String entryTail = name.substring(path.length());

        final File f = new File(writeDirectory + File.separator + entryTail);
        if (entry.isDirectory()) {
          // if its a directory, create it -- REVIEW @yzhang and also any intermediate dirs
          final boolean bMade = f.mkdirs();
          System.out.println((bMade ? "  creating " : "  unable to create ") + f.getCanonicalPath());
        } else {
          System.out.println("  writing  " + f.getCanonicalPath());
          final InputStream is = jar.getInputStream(entry);
          final OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
          final byte buffer[] = new byte[4096];
          int readCount;
          // write contents of 'is' to 'os'
          while ((readCount = is.read(buffer)) > 0) {
            os.write(buffer, 0, readCount);
          }
          os.close();
          is.close();
        }
      }

    } else if (dirURL == null) {
      throw new IllegalStateException("can't find " + sourceDirectory + " on the classpath");
    } else {
      // not a "jar" protocol URL
      throw new IllegalStateException("don't know how to handle extracting from " + dirURL);
    }
  }

  public void extractFromPath(final File src, final File dst) throws IOException {
    if (src.isDirectory()) {
      dst.mkdirs();

      String files[] = src.list();

      for (String file : files) {
        File srcFile = new File(src, file);
        File destFile = new File(dst, file);
        extractFromPath(srcFile, destFile);
      }

    } else {
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);

      byte[] buffer = new byte[1024];

      int length;
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }

      in.close();
      out.close();
    }
  }
}
