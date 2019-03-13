package com.adaptris.stax;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Streaming service that takes using STaX events to stream to and from various XMLEventWriter implementations.
 * <p>
 * The most common use case for this would be to stream XML into JSON or vice-versa making use of
 * {@code com.adaptris.core.json.streaming.JsonStreamingOutputFactory} or {@code com.adaptris.core.json.streaming.JsonStreamingInputFactory} as the
 * {@link XmlOutputFactoryBuilder} or {@link XmlInputFactoryBuilder} implementations respectively.
 * </p>
 * 
 * @config stax-streaming-service
 * @since 3.8.3
 */
@XStreamAlias("stax-streaming-service")
@ComponentProfile(summary = "Use STaX to stream from one format to another", tag = "service,stax,transform,xml,json", since="3.8.3")
@DisplayOrder(order = {"inputBuilder", "outputBuilder"})
public class StaxStreamingService extends ServiceImp {

  private static XmlInputFactoryBuilder defaultInputBuilder = new DefaultInputFactory();
  private static XmlOutputFactoryBuilder defaultOutputBuilder = new DefaultWriterFactory();

  private XmlInputFactoryBuilder inputBuilder;
  private XmlOutputFactoryBuilder outputBuilder;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try (InputStream in = new BufferedInputStream(msg.getInputStream());
        OutputStream out = new BufferedOutputStream(msg.getOutputStream());
        CloseableStaxWrapper wrapper =
            new CloseableStaxWrapper(inputBuilder().build().createXMLEventReader(in),
                outputBuilder().build().createXMLEventWriter(out))) {
      wrapper.writer().add(wrapper.reader());
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
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

  public XmlInputFactoryBuilder getInputBuilder() {
    return inputBuilder;
  }

  public void setInputBuilder(XmlInputFactoryBuilder b) {
    this.inputBuilder = b;
  }
  
  public StaxStreamingService withInputBuilder(XmlInputFactoryBuilder b) {
    setInputBuilder(b);
    return this;
  }

  private XmlInputFactoryBuilder inputBuilder() {
    return getInputBuilder() != null ? getInputBuilder() : defaultInputBuilder;
  }

  public XmlOutputFactoryBuilder getOutputBuilder() {
    return outputBuilder;
  }

  public void setOutputBuilder(XmlOutputFactoryBuilder b) {
    this.outputBuilder = b;
  }
  
  public StaxStreamingService withOutputBuilder(XmlOutputFactoryBuilder b) {
    setOutputBuilder(b);
    return this;
  }
  
  private XmlOutputFactoryBuilder outputBuilder() {
    return getOutputBuilder() != null ? getOutputBuilder() : defaultOutputBuilder;
  }

}
