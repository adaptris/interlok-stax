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

import static com.adaptris.stax.lms.StaxXmlOutput.XML_OUTPUT_WRITER_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.HeartbeatEvent;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamMarshaller;
import com.adaptris.core.event.StandardAdapterStartUpEvent;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.text.xml.XPath;

public class WriteElementTest extends ExampleServiceCase {

  @Test
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

  @Test
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

  @Test
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
    return new StaxWriteElement().withInputFactoryBuilder(null);
  }
}
