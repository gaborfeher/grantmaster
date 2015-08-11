/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015 Gabor Feher <feherga@gmail.com>
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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mechanism for locking files while they are being "open"
 * from the point of view of the user. (The program closes them after reading
 * but the user still think they are open.)
 */
public class MyFileLock {
  private static final Logger logger = LoggerFactory.getLogger(MyFileLock.class);

  /**
   * Unique identifier of this lock. We write it into the lock file so that it
   * can be verified that noone else has changed it.
   * Idea: use the name of the temp file used here. (TODO(gaborfeher))
   */
  private String id;
  /**
   * The file that represents the lock. Normally this is originalFile_.lck
   */
  private File lockFile;

  private MyFileLock() {
  }

  private static File getLockFile(File original) {
    String fname = original.getName();
    fname = fname.replaceAll("\\.[^.]*$", "_.lck");
    return new File(original.getParentFile(), fname);
  }

  public static void breakLock(File file) {
    File lockFile = getLockFile(file);
    lockFile.delete();
  }

  public static MyFileLock lockFile(File file) {
    MyFileLock lock = new MyFileLock();
    lock.lockFile = getLockFile(file);
    if (lock.lockFile.exists()) {
      return null;
    }
    try (FileChannel fileChannel = FileChannel.open(
        lock.lockFile.toPath(),
        StandardOpenOption.CREATE_NEW,
        StandardOpenOption.WRITE)) {
      // Note: checking the existence and creating the file is claimed to be
      // atomic by JavaDoc. Therefore, we don't need system-level locking.
      // TODO(gaborfeher): Investigate DropBox/Google Drive
      lock.id = UUID.randomUUID().toString();
      fileChannel.write(ByteBuffer.wrap(lock.id.getBytes()));
      return lock;
    } catch (IOException ex) {
      logger.error(null, ex);
      return null;
    }
  }

  public boolean verify() {
    try (FileChannel fileChannel = FileChannel.open(lockFile.toPath(), StandardOpenOption.READ)) {
      ByteBuffer lockFileContent = ByteBuffer.allocate(100);
      int numBytesRead = fileChannel.read(lockFileContent);
      fileChannel.close();
      String fileId = new String(lockFileContent.array(), 0, numBytesRead);
      boolean result = fileId.equals(id);
      return result;
    } catch (IOException ex) {
      logger.error(null, ex);
      return false;
    }
  }

  public boolean release() {
    if (!verify()) {
      // The lock is not ours.
      return false;
    }
    lockFile.delete();
    lockFile = null;
    id = null;
    return true;
  }

}
