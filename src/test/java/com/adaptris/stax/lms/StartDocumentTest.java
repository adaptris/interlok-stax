package com.adaptris.stax.lms;

import static com.adaptris.stax.lms.StaxXmlOutput.XML_OUTPUT_WRITER_KEY;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class StartDocumentTest extends ServiceCase {

  public StartDocumentTest(String name) {
    super(name);
  }

  public void testLicense() throws Exception {
    License license = Mockito.mock(License.class);
    StaxStartDocument service = new StaxStartDocument();
    when(license.isEnabled(LicenseType.Standard)).thenReturn(true);
    assertTrue(service.isEnabled(license));

    when(license.isEnabled(LicenseType.Standard)).thenReturn(false);
    assertFalse(service.isEnabled(license));

  }

  public void testSetRootElement() throws Exception {
    StaxStartDocument service = new StaxStartDocument();
    assertNull(service.getRootElement());
    assertEquals(StaxStartDocument.DEFAULT_ROOT_ELEMENT,
        service.rootElement(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    service.setRootElement("hello");
    assertEquals("hello",
        service.rootElement(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }

  public void testPrefixNamespace() throws Exception {
    StaxStartDocument service = new StaxStartDocument();
    assertNull(service.getPrefix());
    assertNull(service.getNamespaceUri());
    service.setPrefix("hello");
    service.setNamespaceUri("hello");
    assertEquals("hello", service.getPrefix());
    assertEquals("hello", service.getNamespaceUri());
  }


  public void testMessageEncoding() throws Exception {
    StaxStartDocument service = new StaxStartDocument();
    assertNull(service.getOutputMessageEncoding());
    assertEquals("UTF-8", service.evaluateEncoding(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    service.setOutputMessageEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", service.evaluateEncoding(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.setContentEncoding("ISO-8859-2");
    service.setOutputMessageEncoding(null);
    assertEquals("ISO-8859-2", service.evaluateEncoding(msg));
  }

  public void testService_NotFileBacked() throws Exception {
    StaxStartDocument service = LifecycleHelper.initAndStart(new StaxStartDocument());
    try {
      service.doService(new DefaultMessageFactory().newMessage());
      fail();
    }
    catch (ServiceException expected) {
    }
    try {
      service.doService(new FakeFileBackedMessageFactory().newMessage());
      fail();
    }
    catch (ServiceException expected) {
    }
    LifecycleHelper.stopAndClose(service);
  }

  public void testService() throws Exception {
    StaxStartDocument service = LifecycleHelper.initAndStart(new StaxStartDocument());
    try {
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage();
      service.doService(msg);
      assertTrue(msg.getObjectHeaders().containsKey(XML_OUTPUT_WRITER_KEY));
      StaxOutputWrapper w = (StaxOutputWrapper) msg.getObjectHeaders().get(XML_OUTPUT_WRITER_KEY);
      w.finish().close();
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Override
  protected StaxStartDocument retrieveObjectForSampleConfig() {
    return new StaxStartDocument();
  }
}
