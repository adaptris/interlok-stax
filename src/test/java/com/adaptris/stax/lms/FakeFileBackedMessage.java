package com.adaptris.stax.lms;

import java.io.File;
import java.io.IOException;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.util.IdGenerator;

public class FakeFileBackedMessage extends DefaultAdaptrisMessageImp implements FileBackedMessage {

  protected FakeFileBackedMessage(IdGenerator guid, AdaptrisMessageFactory fac) throws RuntimeException {
    super(guid, fac);
  }

  @Override
  public void initialiseFrom(File sourceObject) throws IOException, RuntimeException {
    throw new UnsupportedOperationException();
  }

  @Override
  public File currentSource() {
    throw new UnsupportedOperationException();
  }

}
