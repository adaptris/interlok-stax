package com.adaptris.stax.lms;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class FakeFileBackedMessageFactory extends DefaultMessageFactory {

  public FakeFileBackedMessageFactory() {
    super();
  }

  @Override
  public AdaptrisMessage newMessage() {
    return new FakeFileBackedMessage(uniqueIdGenerator(), this);
  }
}
