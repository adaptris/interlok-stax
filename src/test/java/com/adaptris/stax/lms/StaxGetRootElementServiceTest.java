package com.adaptris.stax.lms;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;

/**
 * @author mwarman
 */
public class StaxGetRootElementServiceTest extends ServiceCase {

  @Test
  public void testDoService() throws Exception {
    StaxGetRootElementService service = new StaxGetRootElementService();
    service.setMetadataKey("rootElement");
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("<root/>");
    service.doService(message);
    assertEquals(message.getMetadataValue("rootElement"), "root");
  }

  @Test
  public void testDoServiceOtherElements() throws Exception {
    StaxGetRootElementService service = new StaxGetRootElementService();
    service.setMetadataKey("rootElement");
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("<root xmlns=\"http://development.adaptris.net\"><other>a</other></root>");
    service.doService(message);
    assertEquals(message.getMetadataValue("rootElement"), "root");
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    StaxGetRootElementService service = new StaxGetRootElementService().withInputFactoryBuilder(null);
    service.setMetadataKey("rootElement");
    return service;
  }
}