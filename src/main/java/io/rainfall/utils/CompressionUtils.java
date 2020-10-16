/*
 * Copyright (c) 2014-2020 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall.utils;

import io.rainfall.reporting.HtmlReporter;
import io.rainfall.reporting.PeriodicHlogReporter;

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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/**
 * @author Aurelien Broszniowski
 */

public class CompressionUtils {

  public final static String CRLF = System.getProperty("line.separator");
  private final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

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
    location.mkdirs();

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

  public void extractResources(final String sources, final String dest) throws IOException, URISyntaxException {
    if (jarFile.isFile()) {  // Run with JAR file
      extractFromJar(sources, dest);
    } else {
      extractFromPath(new File(HtmlReporter.class.getClass().getResource(sources).toURI()), new File(dest));
    }
  }

  /**
   * extract the subdirectory from a jar on the classpath to {@code writeDirectory}
   *
   * @param sourceDirectory directory (in a jar on the classpath) to extract
   * @param writeDirectory  the location to extract to
   * @throws IOException if an IO exception occurs
   */
  private void extractFromJar(String sourceDirectory, String writeDirectory) throws IOException {
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

  private void extractFromPath(final File src, final File dst) throws IOException {
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

  public void extractReportTemplateToFile(String inputTemplate, File outputFile) throws IOException {
    InputStream in = PeriodicHlogReporter.class.getClass().getResourceAsStream(inputTemplate);
    OutputStream out = new FileOutputStream(outputFile);
    byte[] buffer = new byte[1024];
    int len = in.read(buffer);
    while (len > 0) {
      out.write(buffer, 0, len);
      len = in.read(buffer);
    }
    out.close();
  }

  public void deleteDirectory(File path) {
    if (path == null)
      return;
    if (path.exists()) {
      for (File f : path.listFiles()) {
        if (f.isDirectory()) {
          deleteDirectory(f);
          f.delete();
        } else {
          f.delete();
        }
      }
      path.delete();
    }
  }

  private final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
      17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47, '@', '.', '\'', '"', '!', '#', '$',
      '%', '^', '&', '*', '(', ')', '\\' };

  public static String cleanFilename(String filename) {
    Arrays.sort(illegalChars);
    StringBuilder cleanName = new StringBuilder();
    for (int i = 0; i < filename.length(); i++) {
      int c = (int)filename.charAt(i);
      if (Arrays.binarySearch(illegalChars, c) < 0) {
        cleanName.append((char)c);
      }
    }
    return cleanName.toString();
  }

  /**
   * take a StringBuilder and replace a marker inside a file by the content of that StringBuilder.
   *
   * @param filename path of the file to change
   * @param marker   marker String in file to be replace
   * @param sb       StringBuilder that has the content to put instead of the marker
   * @throws IOException in case of IO file issue
   */
  public void substituteInFile(final String filename, final String marker, final StringBuilder sb) throws IOException {
    final InputStream in = new FileInputStream(filename);
    Scanner scanner = new Scanner(in);
    StringBuilder fileContents = new StringBuilder();
    try {
      while (scanner.hasNextLine()) {
        fileContents.append(scanner.nextLine()).append(CRLF);
      }
    } finally {
      scanner.close();
    }
    in.close();

    // create template
    byte[] replace = fileContents.toString().replace(marker, sb.toString()).getBytes();

    OutputStream out = new FileOutputStream(new File(filename));
    out.write(replace, 0, replace.length);
    out.close();
  }



}
