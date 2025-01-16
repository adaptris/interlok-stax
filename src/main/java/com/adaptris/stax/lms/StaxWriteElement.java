/*
    Copyright Adaptris Ltd

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.stax.lms;

import java.io.InputStream;
import jakarta.validation.Valid;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.stax.StaxUtils;
import com.adaptris.stax.XmlInputFactoryBuilder;
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
 *
 * @see StaxStartDocument
 * @see StaxEndDocument
 */
@XStreamAlias("stax-xml-write-element")
@ComponentProfile(summary = "Write the current message as XML output via STaX", tag = "service,transform,xml", since = "3.6.6")
public class StaxWriteElement extends StaxXmlOutput {

  @AdvancedConfig
  @Valid
  private XmlInputFactoryBuilder inputFactoryBuilder;

  public StaxWriteElement() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    XMLEventReader reader = null;
    StaxOutputWrapper wrapper = null;
    try {
      wrapper = unwrap(msg);
      XMLEventWriter eventWriter = wrapper.acquireEventWriter();
      try (InputStream in = msg.getInputStream()) {
        reader = XmlInputFactoryBuilder.defaultIfNull(getInputFactoryBuilder()).build().createXMLEventReader(in);
        while (reader.hasNext()) {
          XMLEvent evt = reader.nextEvent();
          if (emit(evt)) {
            eventWriter.add(evt);
          }
        }
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      StaxUtils.closeQuietly(reader);
      releaseLock(wrapper);
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

  public XmlInputFactoryBuilder getInputFactoryBuilder() {
    return inputFactoryBuilder;
  }

  public void setInputFactoryBuilder(XmlInputFactoryBuilder inputFactoryBuilder) {
    this.inputFactoryBuilder = inputFactoryBuilder;
  }

  public StaxWriteElement withInputFactoryBuilder(XmlInputFactoryBuilder b) {
    setInputFactoryBuilder(b);
    return this;
  }


  protected static void releaseLock(StaxOutputWrapper w) {
    if (w != null) {
      w.releaseLock();
    }
  }
}
