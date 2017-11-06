package com.adaptris.stax.lms;

import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Write the contents of the current message to the output created by {@link StaxStartDocument}.
 * <p>
 * For this service to work, the underlying {@link AdaptrisMessageFactory} associated with the {@link AdaptrisMessage} instance must
 * be a {@link FileBackedMessageFactory} and {@link StaxStartDocument} must have precede this service and subsequent processing must
 * include {@link StaxEndDocument} to commit the output; so effectively your processing chain should be <pre>
 * {@code
 *   <stax-xml-start-document/>
 *     ... 0 or more instances of <stax-xml-write-element/>
 *   <stax-xml-end-document/>
 * }
 * </pre>
 * </p>
 * 
 * @config stax-xml-write-element
 * @license STANDARD
 *
 * @see StaxStartDocument
 * @see StaxEndDocument
 */
@XStreamAlias("stax-xml-write-element")
@ComponentProfile(summary = "Write the current message as XML output via STaX", tag = "service,transform,xml", since = "3.6.6")
public class StaxWriteElement extends StaxXmlOutput {

  public StaxWriteElement() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    XMLEventReader reader = null;
    try {
      StaxOutputWrapper wrapper = unwrap(msg);
      try (InputStream in = msg.getInputStream()) {
        reader = XMLInputFactory.newInstance().createXMLEventReader(in);
        long count = 0;
        while (reader.hasNext()) {
          XMLEvent evt = reader.nextEvent();
          if (emit(evt)) {
            count++;
            wrapper.xmlWriter.add(evt);
          }
        }
        log.trace("Added {} XMLEvents to {}", count, wrapper);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      closeQuietly(reader);
    }
  }

  private boolean emit(XMLEvent e) {
    switch (e.getEventType()) {
    case XMLEvent.START_DOCUMENT:
    case XMLEvent.END_DOCUMENT: {
      return false;
    }
    }
    return true;
  }

  private void closeQuietly(XMLEventReader r) {
    try {
      if (r != null) {
        r.close();
      }
    }
    catch (Exception ignored) {

    }
  }
}
