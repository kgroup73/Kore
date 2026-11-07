package com.kore;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface Storage {
  long getSpace() throws IOException, GeneralSecurityException;
}
