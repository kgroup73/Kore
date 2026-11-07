package com.kore;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

public class LocalStorage implements Storage {

  @Override
  public long getSpace() throws IOException, GeneralSecurityException {
    FileStore store = Files.getFileStore(Path.of("."));
    return store.getTotalSpace();
  }

}
