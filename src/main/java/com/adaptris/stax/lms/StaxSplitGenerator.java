package com.adaptris.stax.lms;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.io.IOUtils;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.CloseableIterable;

public abstract class StaxSplitGenerator<S extends StaxSplitGeneratorConfig,T> implements CloseableIterable<T>, Iterator<T> {

  private transient S config;
  private transient String elementToSplitOn = null;
  private transient T nextMessage;
  private boolean iteratorInvoked = false;

  public StaxSplitGenerator(S cfg) throws Exception {
    this.config = cfg;
    init(config);
    String thePath = config.getPath();
    if (thePath.startsWith("/")) {
      thePath = thePath.substring(1);
    }
    String[] elements = thePath.split("/");
    XMLEvent found = null;
    for (String s : elements) {
      found = nextMatching(s);
    }
    if (found == null) {
      if(!config.getSuppressPathNotFound()) {
        throw new CoreException("Could not traverse to " + config.getPath());
      }
    }
    if(found != null){
      elementToSplitOn = ((StartElement) found).getName().getLocalPart();
    }
    nextMessage = generateNextMessage(found, elementToSplitOn);
  }

  public void init(S cfg) throws Exception {

  }

  public abstract T generateNextMessage(XMLEvent evt, String elementName) throws Exception;

  @Override
  public Iterator<T> iterator() {
    if (iteratorInvoked) {
      throw new IllegalStateException("iterator already invoked");
    }
    iteratorInvoked = true;
    return this;
  }

  @Override
  public boolean hasNext() {
    if (nextMessage == null) {
      try {
        nextMessage = generateNextMessage(nextMatching(elementToSplitOn), elementToSplitOn);
      }
      catch (Exception e) {
        throw new RuntimeException("Could not construct next AdaptrisMessage", e);
      }
    }
    return nextMessage != null;
  }

  @Override
  public T next() {
    T ret = nextMessage;
    nextMessage = null;
    return ret;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void close() throws IOException {
    try {
      config.getXmlEventReader().close();
      IOUtils.closeQuietly(config.getInputReader());
    }
    catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public XMLEvent nextMatching(String elementName) throws Exception {
    // iterate over the read until event == XmlEvent.START_ELEMENT
    // return it.
    while (config.getXmlEventReader().hasNext()) {
      XMLEvent evt = config.getXmlEventReader().nextEvent();
      if (evt.getEventType() == XMLEvent.START_ELEMENT) {
        if (((StartElement) evt).getName().getLocalPart().equals(elementName)) {
          return evt;
        }
      }
    }
    return null;
  }

  /**
   * @deprecated since 3.9.3.1 use {@link #isNotEndElement(XMLEvent, String, Consumer)} instead.
   */
  @Deprecated
  // Can't remove because json-streaming uses it.
  public boolean isNotEndElement(XMLEvent evt, String elementName) throws Exception {
    return isNotEndElement(evt, elementName, (end) -> {
    });
  }

  public boolean isNotEndElement(XMLEvent evt, String elementName, Consumer<XMLEvent> endEventCallback)
      throws Exception {
    if (evt.getEventType() == XMLEvent.END_ELEMENT) {
      if (((EndElement) evt).getName().getLocalPart().equals(elementName)) {
        endEventCallback.accept(evt);
        return false;
      }
    }
    return true;
  }

  public S getConfig() {
    return config;
  }
}