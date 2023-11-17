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

package com.adaptris.stax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;

import org.junit.jupiter.api.Test;

public class SaxonWriterImplTest {

  @Test
  public void testCreateStreamWriter() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    assertNotNull(impl.createXMLStreamWriter(new ByteArrayOutputStream()));
    assertNotNull(impl.createXMLStreamWriter(new StringWriter()));
    assertNotNull(impl.createXMLStreamWriter(new ByteArrayOutputStream(), "UTF-8"));
    try {
      impl.createXMLStreamWriter(new StreamResult(new ByteArrayOutputStream()));
      fail();
    } catch (UnsupportedOperationException expected) {

    }
  }

  @Test
  public void testSetDummyMethods() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    assertFalse(impl.isPropertySupported("hello"));
    impl.setProperty("hello", "world");
    assertNull(impl.getProperty("hello"));
  }

  @Test
  public void testXmlEventWriter() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    assertEquals(SaxonEventWriter.class, impl.createXMLEventWriter(new ByteArrayOutputStream()).getClass());
    assertEquals(SaxonEventWriter.class, impl.createXMLEventWriter(new StringWriter()).getClass());
    assertEquals(SaxonEventWriter.class, impl.createXMLEventWriter(new ByteArrayOutputStream(), null).getClass());
    try {
      impl.createXMLEventWriter(new StreamResult(new ByteArrayOutputStream()));
      fail();
    } catch (UnsupportedOperationException expected) {

    }
  }

}
