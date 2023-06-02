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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.stax.lms.StaxOutputWrapper.StaxConfig;

public class StaxOutputWrapperTest {

  @Test
  public void testDefaults() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    assertEquals("UTF-8", wrapper.getStaxConfig(StaxConfig.Encoding.name()));
    assertEquals("root", wrapper.getStaxConfig(StaxConfig.RootElement.name()));
    assertEquals("", wrapper.getStaxConfig(StaxConfig.Prefix.name()));
    assertEquals("", wrapper.getStaxConfig(StaxConfig.NamespaceURI.name()));
    System.out.println(wrapper);
    assertTrue(wrapper.toString().contains("UTF-8"));
  }

  @Test
  public void testBuilder() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    try (StaxOutputWrapper wrapper = new StaxOutputWrapper(f)) {
      assertEquals("ISO-8859-1", wrapper.withEncoding("ISO-8859-1").getStaxConfig(StaxConfig.Encoding));
      assertEquals("hello", wrapper.withRootElement("hello").getStaxConfig(StaxConfig.RootElement));
      assertEquals("hello", wrapper.withPrefix("hello").getStaxConfig(StaxConfig.Prefix));
      assertEquals("hello", wrapper.withNamespaceURI("hello").getStaxConfig(StaxConfig.NamespaceURI));
    }
  }

  @Test
  public void testEventWriter() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    try {
      wrapper.acquireEventWriter();
      fail();
    } catch (IllegalArgumentException expected) {
      wrapper.releaseLock();
    }
    assertNotNull(wrapper.start().acquireEventWriter());
    wrapper.releaseLock();
    wrapper.close();
  }

  @Test
  public void testClose() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    wrapper.close();
    wrapper.start().close();
  }

}
