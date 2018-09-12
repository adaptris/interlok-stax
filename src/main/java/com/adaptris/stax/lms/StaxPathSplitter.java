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
import java.io.Writer;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
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
      return new AdaptrisMessageStaxSplitGenerator(
          new AdaptrisMessageStaxSplitGeneratorConfig().withOriginalMessage(msg).withXmlEventReader(reader).withPath(thePath)
              .withInputReader(buf));
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

  private class AdaptrisMessageStaxSplitGeneratorConfig extends StaxSplitGeneratorConfig {
    AdaptrisMessage originalMessage;
    AdaptrisMessageStaxSplitGeneratorConfig withOriginalMessage(AdaptrisMessage msg) {
      originalMessage = msg;
      return this;
    }
  }


  private class AdaptrisMessageStaxSplitGenerator extends StaxSplitGenerator<AdaptrisMessageStaxSplitGeneratorConfig, AdaptrisMessage> {

    private transient AdaptrisMessageFactory factory;
    private transient XMLEventFactory eventFactory;

    AdaptrisMessageStaxSplitGenerator(AdaptrisMessageStaxSplitGeneratorConfig cfg) throws Exception {
      super(cfg);
      logR.trace("Using message factory: {}", factory.getClass());
    }

    @Override
    public void init(AdaptrisMessageStaxSplitGeneratorConfig cfg) {
      this.factory = selectFactory(cfg.originalMessage);
      this.eventFactory = XMLEventFactory.newInstance();
    }

    @Override
    public AdaptrisMessage generateNextMessage(XMLEvent evt, String elementName) throws Exception {
      XMLEvent event = evt;
      if (event == null) return null;
      AdaptrisMessage splitMsg = factory.newMessage();
      XMLEventWriter xmlWriter = null;
      String encoding = evaluateEncoding(getConfig().originalMessage);
      try (Writer w = splitMsg.getWriter(encoding)) {
        xmlWriter = XMLOutputFactory.newInstance().createXMLEventWriter(w);
        xmlWriter.add(eventFactory.createStartDocument(encoding, "1.0"));
        xmlWriter.add(event);
        while (isNotEndElement(event, elementName) && getConfig().getXmlEventReader().hasNext()) {
          event = getConfig().getXmlEventReader().nextEvent();
          xmlWriter.add(event);
        }
        xmlWriter.add(eventFactory.createEndDocument());
      }
      finally {
        StaxUtils.closeQuietly(xmlWriter);
      }
      copyMetadata(getConfig().originalMessage, splitMsg);
      return splitMsg;
    }
  }
}
