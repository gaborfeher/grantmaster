package com.github.gaborfeher.grantmaster.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Packs and unpacks a zip file into a temporary directory. The files of
 * a HSQLDB database will be stored in the zip file.
 */
public class DatabaseArchive {
  /**
   * Directory where the database files are stored while the database is open.
   */
  private final File tempDir;
  
  private DatabaseArchive(File tempDir) {
    this.tempDir = tempDir;
  }

  public void close() {
    if (tempDir != null) {
      simpleRecursiveDelete(tempDir);
    }
  }

  public static DatabaseArchive createNew() {
    return new DatabaseArchive(createTempDir());
  }
  
  public static DatabaseArchive open(File path) {
    File tempDir = createTempDir();
    if (tempDir == null) {
      return null;
    }
    try (
        FileInputStream fileInputStream = new FileInputStream(path);
        ZipInputStream zipInputStrem = new ZipInputStream(fileInputStream)) {
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStrem.getNextEntry()) != null) {
        File fileToWrite = new File(tempDir, zipEntry.getName());
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToWrite)) {
          while (zipInputStrem.available() > 0) {
            byte[] buffer = new byte[1024 * 32];
            int len = zipInputStrem.read(buffer);
            if (len > 0) {
              fileOutputStream.write(buffer, 0, len);
            }
          }
        } catch (IOException e) {
          return null;
        }
      }
    } catch (IOException e) {
      return null;
    }
    return new DatabaseArchive(tempDir);
  }

  private static void simpleRecursiveDelete(File main) {
    // No simlink handling.
    if (main.isDirectory()) {
      for (File sub : main.listFiles()) {
        simpleRecursiveDelete(sub);
      }
    }
    if (!main.delete()) {
      System.out.println("failed to delete " + main);
    }
  }
  
  private static File createTempDir() {
    try {
      return Files.createTempDirectory("gmtmp").toFile();
    } catch (IOException ex) {
      Logger.getLogger(DatabaseSingleton.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public File getFile() {
    return tempDir;
  }

  public void saveTo(File path) throws IOException {
    Path tempFile = Files.createTempFile("gmsave", ".hdb");
    try (
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile());
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
      for (File item : tempDir.listFiles()) {
        ZipEntry entry = new ZipEntry(item.getName());
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(Files.readAllBytes(item.toPath()));
        zipOutputStream.closeEntry();
      }
    }
    Files.move(tempFile, path.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
}
