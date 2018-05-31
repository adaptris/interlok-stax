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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.stax.StaxUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Splitter implementation that splits based on STaX events.
 * <p>
 * This allows you to split via simple element traversal, so the path cannot be assumed to be an XPath.
 * {@code /path/to/repeating/element} would be fine, but {@code //repeating/element} would not. It works based on
 * {@link XMLEventReader} and navigates based on {@link StartElement} events only.
 * </p>
 * 
 * @config stax-path-splitter
 */
@XStreamAlias("stax-path-splitter")
public class StaxPathSplitter extends MessageSplitterImp {
  private transient static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final String DEFAULT_XML_ENCODING = "UTF-8";
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotBlank
  private String path;

  @AdvancedConfig
  private Integer bufferSize;

  @AdvancedConfig
  @InputFieldDefault(value = "UTF-8")
  private String encoding = null;

  public StaxPathSplitter() {

  }

  public StaxPathSplitter(String path) {
    this();
    setPath(path);
  }

  @Override
  public Iterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    try {
      String thePath = msg.resolve(getPath());
      BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize());
      XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(buf);
      return new StaxSplitGenerator(new GeneratorConfig().withReader(reader).withPath(thePath).withOriginalMessage(msg));
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public Integer getBufferSize() {
    return bufferSize;
  }

  /**
   * Set the internal buffer size.
   * <p>
   * This is used when; the default buffer size matches the default buffer size in {@link BufferedReader} and {@link BufferedWriter}
   * , changes to the buffersize will impact performance and memory usage depending on the underlying operating system/disk.
   * </p>
   * 
   * @param b the buffer size (default is 8192).
   */
  public void setBufferSize(Integer b) {
    this.bufferSize = b;
  }

  int bufferSize() {
    return getBufferSize() != null ? getBufferSize().intValue() : DEFAULT_BUFFER_SIZE;
  }

  public String getPath() {
    return path;
  }

  /**
   * Set the xpath-alike path to the element on which you want to split.
   * <p>
   * Note that this is only a pseudo-xpath evaluator as it only allows simple element traversal and not any XPath functions.
   * {@code /path/to/repeating/element} would be fine, but {@code //repeating/element} would not. It works based on
   * {@link XMLEventReader} and navigates based on {@link StartElement} events only.
   * </p>
   * 
   * @param path the path.
   */
  public void setPath(String path) {
    this.path = Args.notBlank(path, "path");
  }

  public String getEncoding() {
    return encoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   * <p>
   * If not specified the following rules will be applied:
   * </p>
   * <ol>
   * <li>If the {@link AdaptrisMessage#getCharEncoding()} is non-null then that will be used.</li>
   * <li>UTF-8</li>
   * </ol>
   * <p>
   * As a result; the character encoding on the message is always set using {@link AdaptrisMessage#setContentEncoding(String)}.
   * </p>
   * 
   * @param enc the character encoding
   */
  public void setEncoding(String enc) {
    this.encoding = enc;
  }

  String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = DEFAULT_XML_ENCODING;
    if (!isEmpty(getEncoding())) {
      encoding = getEncoding();
    }
    else if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
  }

  private class GeneratorConfig {
    AdaptrisMessage originalMessage;
    String path;
    XMLEventReader reader;

    GeneratorConfig withOriginalMessage(AdaptrisMessage msg) {
      originalMessage = msg;
      return this;
    }

    GeneratorConfig withPath(String s) {
      path = s;
      return this;
    }

    GeneratorConfig withReader(XMLEventReader reader) {
      this.reader = reader;
      return this;
    }
  }

  private class StaxSplitGenerator implements CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage> {
    private transient GeneratorConfig config;
    private transient AdaptrisMessageFactory factory;
    private transient AdaptrisMessage nextMessage;
    private transient String elementToSplitOn;
    private transient XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    protected StaxSplitGenerator(GeneratorConfig cfg) throws Exception {
      this.config = cfg;
      this.factory = selectFactory(config.originalMessage);
      String thePath = config.path;
      if (thePath.startsWith("/")) {
        thePath = thePath.substring(1);
      }
      String[] elements = thePath.split("/");
      XMLEvent found = null;
      for (String s : elements) {
        found = nextMatching(s);
      }
      if (found == null) {
        throw new CoreException("Could not traverse to " + path);
      }
      elementToSplitOn = ((StartElement) found).getName().getLocalPart();
      nextMessage = generateNextMessage(found, elementToSplitOn);
      logR.trace("Using message factory: {}", factory.getClass());
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
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
    public AdaptrisMessage next() {
      AdaptrisMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

    private AdaptrisMessage generateNextMessage(XMLEvent evt, String elementName) throws Exception {
      XMLEvent event = evt;
      if (event == null) return null;
      AdaptrisMessage splitMsg = factory.newMessage();
      XMLEventWriter xmlWriter = null;
      String encoding = evaluateEncoding(config.originalMessage);
      try (Writer w = splitMsg.getWriter(encoding)) {
        xmlWriter = XMLOutputFactory.newInstance().createXMLEventWriter(w);
        xmlWriter.add(eventFactory.createStartDocument(encoding, "1.0"));
        xmlWriter.add(event);
        while (isNotEndElement(event, elementName) && config.reader.hasNext()) {
          event = config.reader.nextEvent();
          xmlWriter.add(event);
        }
        xmlWriter.add(eventFactory.createEndDocument());
      }
      finally {
        StaxUtils.closeQuietly(xmlWriter);
      }
      copyMetadata(config.originalMessage, splitMsg);
      return splitMsg;
    }

    @Override
    public void close() throws IOException {
      try {
        config.reader.close();
      }
      catch (XMLStreamException e) {
        throw new IOException(e);
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private XMLEvent nextMatching(String elementName) throws Exception {
      // iterate over the read until event == XmlEvent.START_ELEMENT
      // return it.
      while (config.reader.hasNext()) {
        XMLEvent evt = config.reader.nextEvent();
        if (evt.getEventType() == XMLEvent.START_ELEMENT) {
          if (((StartElement) evt).getName().getLocalPart().equals(elementName)) {
            return evt;
          }
        }
      }
      return null;
    }

    private boolean isNotEndElement(XMLEvent evt, String elementName) throws Exception {
      if (evt.getEventType() == XMLEvent.END_ELEMENT) {
        if (((EndElement) evt).getName().getLocalPart().equals(elementName)) {
          return false;
        }
      }
      return true;
    }

  };

}
