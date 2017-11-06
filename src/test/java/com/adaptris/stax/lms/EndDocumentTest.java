package com.adaptris.stax.lms;

import static com.adaptris.stax.lms.StaxXmlOutput.XML_OUTPUT_WRITER_KEY;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class EndDocumentTest extends ServiceCase {

  public EndDocumentTest(String name) {
    super(name);
  }

  public void testLicense() throws Exception {
    License license = Mockito.mock(License.class);
    StaxEndDocument service = new StaxEndDocument();
    when(license.isEnabled(LicenseType.Standard)).thenReturn(true);
    assertTrue(service.isEnabled(license));

    when(license.isEnabled(LicenseType.Standard)).thenReturn(false);
    assertFalse(service.isEnabled(license));

  }

  public void testService_NotFileBacked() throws Exception {
    StaxEndDocument service = LifecycleHelper.initAndStart(new StaxEndDocument());
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

  public void testService_NoObject() throws Exception {
    StaxEndDocument service = LifecycleHelper.initAndStart(new StaxEndDocument());
    try {
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage();
      service.doService(msg);
      fail();
    }
    catch (ServiceException expected) {
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  public void testService() throws Exception {
    StaxStartDocument starter = LifecycleHelper.initAndStart(new StaxStartDocument());
    StaxEndDocument service = LifecycleHelper.initAndStart(new StaxEndDocument());
    try {
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage();
      starter.doService(msg);
      assertTrue(msg.getObjectHeaders().containsKey(XML_OUTPUT_WRITER_KEY));
      service.doService(msg);
      System.err.println(msg.getContent());
      assertFalse(msg.getObjectHeaders().containsKey(XML_OUTPUT_WRITER_KEY));
    }
    finally {
      LifecycleHelper.stopAndClose(starter);
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Override
  protected StaxEndDocument retrieveObjectForSampleConfig() {
    return new StaxEndDocument();
  }
}
