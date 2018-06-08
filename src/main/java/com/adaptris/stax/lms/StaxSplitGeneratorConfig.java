package com.adaptris.stax.lms;

import javax.xml.stream.XMLEventReader;

public abstract class StaxSplitGeneratorConfig {
  private String path;
  private XMLEventReader reader;

  @SuppressWarnings("unchecked")
  public <T extends StaxSplitGeneratorConfig> T withPath(String s) {
    path = s;
    return (T)this;
  }

  public String getPath() {
    return path;
  }

  @SuppressWarnings("unchecked")
  public <T extends StaxSplitGeneratorConfig> T withReader(XMLEventReader reader) {
    this.reader = reader;
    return (T)this;
  }

  public XMLEventReader getReader() {
    return reader;
  }
}
