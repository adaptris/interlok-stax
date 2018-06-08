package com.adaptris.stax.lms;

import javax.xml.stream.XMLEventReader;

public abstract class StaxSplitGeneratorConfig {
  String path;
  XMLEventReader reader;

  @SuppressWarnings("unchecked")
  <T extends StaxSplitGeneratorConfig> T withPath(String s) {
    path = s;
    return (T)this;
  }

  @SuppressWarnings("unchecked")
  <T extends StaxSplitGeneratorConfig> T withReader(XMLEventReader reader) {
    this.reader = reader;
    return (T)this;
  }
}
