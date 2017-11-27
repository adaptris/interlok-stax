package com.adaptris.stax.lms;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Prepare ourselves for writing some large XML output via STaX.
 * <p>
 * For this service to work, the underlying {@link AdaptrisMessageFactory} associated with the {@link AdaptrisMessage} instance must
 * be a {@link FileBackedMessageFactory} and subsequent processing should include multiple instances of {@link StaxWriteElement} to
 * write XML elements and finally {@link StaxEndDocument} to commit the output; so effectively your processing chain should be <pre>
 * {@code
 *   <stax-xml-start-document/>
 *     ... 0 or more instances of <stax-xml-write-element/>
 *   <stax-xml-end-document/>
 * }
 * </pre>
 * </p>
 * 
 * @config stax-xml-start-document
 * @license STANDARD
 * @see StaxEndDocument
 * @see StaxWriteElement
 */
@XStreamAlias("stax-xml-start-document")
@ComponentProfile(summary = "Prepare for writing large XML output via STaX", tag = "service,transform,xml", since = "3.6.6")
@DisplayOrder(order = {"outputMessageEncoding", "rootElement", "prefix", "namespace"})
public class StaxStartDocument extends StaxXmlOutput {

  public static final String DEFAULT_ROOT_ELEMENT = "root";

  @AdvancedConfig
  private String outputMessageEncoding = null;
  @AdvancedConfig
  @InputFieldDefault(value = "root")
  @InputFieldHint(expression = true)
  private String rootElement = null;

  @AdvancedConfig
  @InputFieldDefault(value = "")
  @InputFieldHint(expression = true, style = "BLANKABLE")
  private String prefix;
  @AdvancedConfig
  @InputFieldDefault(value = "")
  @InputFieldHint(expression = true, style = "BLANKABLE")
  private String namespaceUri;


  public StaxStartDocument() {

  }

  public StaxStartDocument(String rootElement) {
    this();
    setRootElement(rootElement);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    StaxOutputWrapper wrapper = null;
    String encoding = evaluateEncoding(msg);
    try {
      FileBackedMessageFactory factory = messageFactory(msg);
      File tempFile = factory.createTempFile(msg);
      log.trace("Created {} for XML output", tempFile.getCanonicalPath());
      wrapper = new StaxOutputWrapper(tempFile).withEncoding(encoding).withRootElement(rootElement(msg))
          .withPrefix(msg.resolve(getPrefix())).withNamespaceURI(msg.resolve(getNamespaceUri())).start();
      msg.addObjectHeader(XML_OUTPUT_WRITER_KEY, wrapper);
      log.trace("Added [{}] as object metadata", wrapper);
    } catch (Exception e) {
      closeQuietly(wrapper);
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  public String getOutputMessageEncoding() {
    return outputMessageEncoding;
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
   * As a result; the character encoding on the message is always set using {@link AdaptrisMessage#setCharEncoding(String)}.
   * </p>
   * 
   * @param encoding the character
   */
  public void setOutputMessageEncoding(String encoding) {
    outputMessageEncoding = encoding;
  }

  public String getRootElement() {
    return rootElement;
  }

  /**
   * Set the root element for the XML document.
   * 
   * @param s the root element, default is {@value #DEFAULT_ROOT_ELEMENT} if not specified.
   */
  public void setRootElement(String s) {
    this.rootElement = s;
  }

  String rootElement(AdaptrisMessage msg) {
    return defaultIfEmpty(msg.resolve(getRootElement()), DEFAULT_ROOT_ELEMENT);
  }

  String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(getOutputMessageEncoding())) {
      encoding = defaultIfEmpty(msg.resolve(getOutputMessageEncoding()), "UTF-8");
    }
    else if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
  }

  /**
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @param prefix the prefix to set
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * @return the namespaceUri
   */
  public String getNamespaceUri() {
    return namespaceUri;
  }

  /**
   * @param s the namespaceUri to set
   */
  public void setNamespaceUri(String s) {
    this.namespaceUri = s;
  }


}
