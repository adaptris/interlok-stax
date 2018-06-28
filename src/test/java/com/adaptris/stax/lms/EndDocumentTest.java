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
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class EndDocumentTest extends ServiceCase {

  public EndDocumentTest(String name) {
    super(name);
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
