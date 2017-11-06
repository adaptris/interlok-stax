package com.adaptris.stax.lms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

final class StaxOutputWrapper {
  protected File outputFile;
  protected OutputStreamWriter output;
  protected XMLEventWriter xmlWriter;
  protected String root;
  protected String encoding;
  private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

  protected StaxOutputWrapper(File f, String enc, String startElement) throws Exception {
    this.outputFile = f;
    this.encoding = enc;
    this.root = startElement;
    this.output = new OutputStreamWriter(new FileOutputStream(f), enc);
    this.xmlWriter = XMLOutputFactory.newInstance().createXMLEventWriter(output);
    xmlWriter.add(eventFactory.createStartDocument(enc, "1.0"));
    xmlWriter.add(eventFactory.createStartElement("", "", root));
  }

  protected StaxOutputWrapper finish() throws Exception {
    xmlWriter.add(eventFactory.createEndElement("", "", root));
    xmlWriter.add(eventFactory.createEndDocument());
    return this;
  }

  void close() {
    closeQuietly(xmlWriter);
    IOUtils.closeQuietly(output);
  }

  void closeQuietly(XMLEventWriter f) {
    try {
      if (f != null) {
        f.close();
      }
    }
    catch (Exception e) {

    }
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("outputFile", outputFile).append("rootElement", root)
        .append("encoding", encoding).append("xmlWriter", xmlWriter).toString();
  }
}
