package com.adaptris.stax;

import static com.adaptris.stax.StaxUtils.closeQuietly;

import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides empty methods for {@link StreamWriterFactory} lifecycle.
 * 
 */
public abstract class StreamWriterFactoryImpl implements StreamWriterFactory {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());


  @Override
  public void close(XMLStreamWriter w) {
    closeQuietly(w);
  }

}
