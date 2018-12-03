package com.adaptris.stax.lms;

import java.io.Reader;

import javax.xml.stream.XMLEventReader;

public abstract class StaxSplitGeneratorConfig {
  private String path;
  private XMLEventReader xmlReader;
  private Reader inputReader;
  private boolean suppressPathNotFound;

  public StaxSplitGeneratorConfig(){
    suppressPathNotFound = false;
  }

  @SuppressWarnings("unchecked")
  public <T extends StaxSplitGeneratorConfig> T withPath(String s) {
    path = s;
    return (T)this;
  }

  public String getPath() {
    return path;
  }

  /**
   * 
   * @deprecated poorly named; use {@link #withXmlEventReader(XMLEventReader)} instead. Will be removed w/o warning
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public <T extends StaxSplitGeneratorConfig> T withReader(XMLEventReader reader) {
    this.xmlReader = reader;
    return (T)this;
  }

  /**
   * 
   * @deprecated poorly named; use {@link #getXmlEventReader()} instead, Will be removed w/o warning
   */
  @Deprecated
  public XMLEventReader getReader() {
    return xmlReader;
  }

  @SuppressWarnings("unchecked")
  public <T extends StaxSplitGeneratorConfig> T withXmlEventReader(XMLEventReader reader) {
    this.xmlReader = reader;
    return (T) this;
  }

  public XMLEventReader getXmlEventReader() {
    return xmlReader;
  }

  @SuppressWarnings("unchecked")
  public <T extends StaxSplitGeneratorConfig> T withInputReader(Reader reader) {
    this.inputReader = reader;
    return (T) this;
  }

  public Reader getInputReader() {
    return inputReader;
  }

  @SuppressWarnings("unchecked")
  public <T extends StaxSplitGeneratorConfig> T withSuppressPathNotFound(boolean suppressPathNotFound) {
    this.suppressPathNotFound = suppressPathNotFound;
    return (T) this;
  }

  public boolean getSuppressPathNotFound() {
    return suppressPathNotFound;
  }
}
