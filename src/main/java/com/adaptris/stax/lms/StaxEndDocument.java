package com.adaptris.stax.lms;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Commit and finish the large XML output started by {@link StaxStartDocument}.
 * <p>
 * For this service to work, the underlying {@link AdaptrisMessageFactory} associated with the {@link AdaptrisMessage} instance must
 * be a {@link FileBackedMessageFactory} and {@link StaxStartDocument} must have precede this service so effectively your processing
 * chain should be <pre>
 * {@code
 *   <stax-xml-start-document/>
 *     ... 0 or more instances of <stax-xml-write-element/>
 *   <stax-xml-end-document/>
 * }
 * </pre>
 * </p>
 * 
 * @config stax-xml-end-document
 * @license STANDARD
 * @see StaxStartDocument
 * @see StaxWriteElement
 */
@XStreamAlias("stax-xml-end-document")
@ComponentProfile(summary = "Commit any output written via STaX", tag = "service,transform,xml", since = "3.6.6")
public class StaxEndDocument extends StaxXmlOutput {

  public StaxEndDocument() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    StaxOutputWrapper wrapper = null;
    try {
      wrapper = unwrap(msg);
      finishAndClose(wrapper);
      log.trace("Using [{}] for contents of message", wrapper.outputFile.getCanonicalPath());
      ((FileBackedMessage) msg).initialiseFrom(wrapper.outputFile);
      log.trace("Encoding set to [{}]", wrapper.encoding);
      msg.setContentEncoding(wrapper.encoding);
      msg.getObjectHeaders().remove(XML_OUTPUT_WRITER_KEY);
    }
    catch (Exception e) {
      closeQuietly(wrapper);
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  private void finishAndClose(StaxOutputWrapper f) throws Exception {
    try {
      f.finish();
    }
    finally {
      closeQuietly(f);
    }
  }

}
