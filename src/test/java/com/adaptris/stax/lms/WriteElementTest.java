package com.adaptris.stax.lms;

import static com.adaptris.stax.lms.StaxXmlOutput.XML_OUTPUT_WRITER_KEY;

import java.io.File;

import org.w3c.dom.Document;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.HeartbeatEvent;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.event.StandardAdapterStartUpEvent;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

public class WriteElementTest extends ServiceCase {

  public WriteElementTest(String name) {
    super(name);
  }

  public void testService_NoObject() throws Exception {
    StaxWriteElement service = LifecycleHelper.initAndStart(new StaxWriteElement());
    try {
      AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
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
    StaxStartDocument starter = LifecycleHelper.initAndStart(new StaxStartDocument("my"));
    StaxEndDocument finish = LifecycleHelper.initAndStart(new StaxEndDocument());
    StaxWriteElement service = LifecycleHelper.initAndStart(new StaxWriteElement());
    try {
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage();
      starter.doService(msg);
      String xml = new XStreamMarshaller().marshal(new Adapter());
      msg.setContent(xml, "UTF-8");
      service.doService(msg);
      finish.doService(msg);
      XPath xpath = new XPath();
      Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
      assertEquals(StandardAdapterStartUpEvent.class.getCanonicalName(),
          xpath.selectSingleTextItem(doc, "/my/adapter/start-up-event-imp"));
      assertEquals(HeartbeatEvent.class.getCanonicalName(), xpath.selectSingleTextItem(doc, "/my/adapter/heartbeat-event-imp"));
    }
    finally {
      LifecycleHelper.stopAndClose(starter);
      LifecycleHelper.stopAndClose(finish);
      LifecycleHelper.stopAndClose(service);
    }
  }

  public void testService_BrokenInput() throws Exception {
    StaxWriteElement service = LifecycleHelper.initAndStart(new StaxWriteElement());
    try {
      File tempFile = new FileBackedMessageFactory().createTempFile(this);
      StaxOutputWrapper wrapper = new StaxOutputWrapper(tempFile).withEncoding("UTF-8").withRootElement("root");
      AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.INPUT).newMessage();
      msg.addObjectHeader(XML_OUTPUT_WRITER_KEY, wrapper);
      service.doService(msg);
      fail();
    }
    catch (ServiceException expected) {

    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Override
  protected StaxWriteElement retrieveObjectForSampleConfig() {
    return new StaxWriteElement();
  }
}
