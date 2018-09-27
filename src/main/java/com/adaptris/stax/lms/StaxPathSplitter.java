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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.util.Iterator;

import javax.validation.Valid;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
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
@DisplayOrder(order = {"path", "encoding", "bufferSize", "xmlDocumentFactoryConfig"})
public class StaxPathSplitter extends MessageSplitterImp {
  private transient static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final String DEFAULT_XML_ENCODING = "UTF-8";
  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  
  // Transformer is quite expensive, so re-use a thread-local copy.
  private static ThreadLocal<Transformer> transformer = ThreadLocal.withInitial(() -> {
    return newTransformer();
  });

  @NotBlank
  private String path;

  @AdvancedConfig
  private Integer bufferSize;

  @AdvancedConfig
  @InputFieldDefault(value = "UTF-8")
  private String encoding = null;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig;

  @AdvancedConfig
  @Valid
  private KeyValuePairSet namespaceContext;

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
      NamespaceContext nsCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactoryBuilder.newInstance(getXmlDocumentFactoryConfig(), nsCtx).build();
      return new DocumentStaxSplitGenerator(
          new AdaptrisMessageStaxSplitGeneratorConfig().withOriginalMessage(msg)
              .withDocumentBuilderFactory(dbFactory).withXmlEventReader(reader).withPath(thePath)
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

  protected int bufferSize() {
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

  public StaxPathSplitter withEncoding(String enc) {
    setEncoding(enc);
    return this;
  }

  protected String evaluateEncoding(AdaptrisMessage msg) {
    return XmlHelper.getXmlEncoding(msg, getEncoding());
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }

  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }
  
  public StaxPathSplitter withXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder builder) {
    setXmlDocumentFactoryConfig(builder);
    return this;
  }

  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }

  public StaxPathSplitter withNamespaceContext(KeyValuePairSet namespaceContext) {
    setNamespaceContext(namespaceContext);
    return this;
  }

  private static Transformer newTransformer() {
    try {
      return TransformerFactory.newInstance().newTransformer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private class AdaptrisMessageStaxSplitGeneratorConfig extends StaxSplitGeneratorConfig {
    AdaptrisMessage originalMessage;
    DocumentBuilderFactory builder;

    AdaptrisMessageStaxSplitGeneratorConfig withOriginalMessage(AdaptrisMessage msg) {
      originalMessage = msg;
      return this;
    }

    AdaptrisMessageStaxSplitGeneratorConfig withDocumentBuilderFactory(DocumentBuilderFactory builder) {
      this.builder = builder;
      return this;
    }
  }

  
  private class DocumentStaxSplitGenerator extends StaxSplitGenerator<AdaptrisMessageStaxSplitGeneratorConfig, AdaptrisMessage> {

    private transient DocumentBuilder docBuilder;
    private transient AdaptrisMessageFactory factory;
    private transient String encoding;

    DocumentStaxSplitGenerator(AdaptrisMessageStaxSplitGeneratorConfig cfg) throws Exception {
      super(cfg);
    }

    @Override
    public void init(AdaptrisMessageStaxSplitGeneratorConfig cfg) throws ParserConfigurationException {
      docBuilder = cfg.builder.newDocumentBuilder();
      this.factory = selectFactory(cfg.originalMessage);
      this.encoding = evaluateEncoding(cfg.originalMessage);
    }

    @Override
    public AdaptrisMessage generateNextMessage(XMLEvent event, String elementName) throws Exception {
      if (event == null) {
        return null;
      }
      Document document = docBuilder.newDocument();
      createDocument(event, elementName, document, document);
      AdaptrisMessage splitMsg = factory.newMessage();
      try (OutputStream out = splitMsg.getOutputStream()) {
        serialize(new DOMSource(document), new StreamResult(out), encoding);
      }
      return splitMsg;
    }

    private void createDocument(XMLEvent event, String elementName, Document document, Node parentElement) throws Exception {
      Element currentElement = null;
      while (isNotEndElement(event, elementName) && getConfig().getXmlEventReader().hasNext()) {
        switch (event.getEventType()) {
          case XMLStreamConstants.START_ELEMENT:
            StartElement se = event.asStartElement();
            if(currentElement != null){
              createDocument(event, se.getName().getLocalPart(), document, currentElement);
            } else {
              currentElement = createElement(document, se);
              parentElement.appendChild(currentElement);
            }
            break;
          case XMLStreamConstants.CHARACTERS:
            if(!event.asCharacters().isWhiteSpace() && currentElement != null){
              currentElement.setTextContent(event.asCharacters().getData());
            }
            break;
          case XMLStreamConstants.END_ELEMENT:
            currentElement = null;
            break;
        }
        event = getConfig().getXmlEventReader().nextEvent();
      }
    }

    private Element createElement(Document document, StartElement se){
      Element element = document.createElementNS(se.getName().getNamespaceURI(), se.getName().getLocalPart());
      Iterator attributes = se.getAttributes();
      while(attributes.hasNext()){
        Attribute a = (Attribute)attributes.next();
        element.setAttribute(a.getName().getLocalPart(), a.getValue());
      }
      return element;
    }
    
    private void serialize(DOMSource doc, StreamResult result, String encoding)
        throws TransformerFactoryConfigurationError, TransformerException {
      Transformer serializer = transformer.get();
      serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      serializer.transform(doc, result);
    }

  }
}
