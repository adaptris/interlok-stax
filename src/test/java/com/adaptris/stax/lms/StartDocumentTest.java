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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class StartDocumentTest extends ServiceCase {

  public StartDocumentTest(String name) {
    super(name);
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
