package com.adaptris.stax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.text.xml.XPath;

public class StaxStreamingServiceTest extends ExampleServiceCase {
  private static final String XML_MESSAGE = "<?xml version=\"1.0\" "
      + "encoding=\"UTF-8\"?>" + System.lineSeparator() + "<envelope>" + System.lineSeparator()
      + "<document><nested>1</nested></document>" + System.lineSeparator() + "<document><nested>2</nested></document>"
      + System.lineSeparator() + "<document><nested>3</nested></document>"
      + System.lineSeparator() + "</envelope>";


  @Override
  protected StaxStreamingService retrieveObjectForSampleConfig() {
    return new StaxStreamingService().withInputBuilder(new DefaultInputFactory()).withOutputBuilder(new DefaultWriterFactory());
  }

  @Test
  public void testDefaultDoService() throws Exception{
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    StaxStreamingService service = new StaxStreamingService();
    execute(service, msg);
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertEquals(3, xpath.selectNodeList(d, "/envelope/document").getLength());
  }

  @Test
  public void testDoService() throws Exception{
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    StaxStreamingService service =
        new StaxStreamingService().withInputBuilder(new DefaultInputFactory())
            .withOutputBuilder(new SaxonStreamWriterFactory());
    execute(service, msg);
    XPath xpath = new XPath();
    Document d = XmlHelper.createDocument(msg, DocumentBuilderFactoryBuilder.newInstance());
    assertEquals(3, xpath.selectNodeList(d, "/envelope/document").getLength());
  }

  @Test
  public void testDoService_Exception() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage(XML_MESSAGE);
    StaxStreamingService service = new StaxStreamingService().withInputBuilder(new DefaultInputFactory()).withOutputBuilder(new DefaultWriterFactory());
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {
    }
  }

}
