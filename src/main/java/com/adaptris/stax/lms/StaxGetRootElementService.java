package com.adaptris.stax.lms;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.stax.StaxUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.hibernate.validator.constraints.NotBlank;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * @config stax-get-root-element
 * @author mwarman
 */
@ComponentProfile(summary = "Gets the root element of Xml via STaX and sets it as metadata", tag = "service,xml")
@XStreamAlias("stax-get-root-element")
public class StaxGetRootElementService extends ServiceImp {

  private transient static final int DEFAULT_BUFFER_SIZE = 8192;

  @NotBlank
  @AffectsMetadata
  private String metadataKey;

  @AdvancedConfig
  private Integer bufferSize;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    XMLEventReader reader = null;
    try (BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize())) {
      reader = XMLInputFactory.newInstance().createXMLEventReader(buf);
      String value = getRootValue(reader);
      if (value != null) {
        msg.addMetadata(getMetadataKey(), value);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      StaxUtils.closeQuietly(reader);
    }
  }

  private String getRootValue(XMLEventReader reader) throws Exception {
    while (reader.hasNext()) {
      XMLEvent evt = reader.nextEvent();
      if (evt.getEventType() == XMLEvent.START_ELEMENT) {
        return evt.asStartElement().getName().getLocalPart();
      }
    }
    return null;
  }

  @Override
  public void prepare() throws CoreException {

  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

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

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }
}
