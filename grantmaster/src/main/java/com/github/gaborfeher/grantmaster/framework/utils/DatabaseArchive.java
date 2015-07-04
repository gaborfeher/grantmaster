/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.framework.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packs and unpacks a zip file into a temporary directory. The files of
 * a HSQLDB database will be stored in the zip file. This class does not
 * care about the contents of the zipped files.
 */
public class DatabaseArchive {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseArchive.class);
  
  /**
   * Directory where the database files are stored while the database is open.
   */
  private final File tempDir;
  
  private DatabaseArchive(File tempDir) {
    this.tempDir = tempDir;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }

  public void close() {
    if (tempDir != null) {
      simpleRecursiveDelete(tempDir);
    }
  }

  public static DatabaseArchive createNew() {
    return new DatabaseArchive(createTempDir());
  }
  
  public static DatabaseArchive open(File archiveFile) {
    File tempDir = createTempDir();
    if (tempDir == null) {
      return null;
    }
    try (ZipInputStream zipInputStrem = new ZipInputStream(
             new BufferedInputStream(
                 new FileInputStream(archiveFile)))) {
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
      logger.error("failed to delete file {}", main);
    }
  }
  
  private static File createTempDir() {
    try {
      return Files.createTempDirectory("gmtmp").toFile();
    } catch (IOException ex) {
      logger.error(null, ex);
      return null;
    }
  }

  File getDirectory() {
    return tempDir;
  }

  public void saveToArchiveFile(File archiveFile) throws IOException {
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
    Files.move(tempFile, archiveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
}
