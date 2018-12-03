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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

import net.sf.saxon.lib.SaxonOutputKeys;

public class SaxonWriterImplTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}


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
    try {
      impl = new SaxonWriterImpl(
          new KeyValuePairSet(Arrays.asList(new KeyValuePair(SaxonOutputKeys.USE_CHARACTER_MAPS, "yes"),
              new KeyValuePair(OutputKeys.METHOD, "xml"))));
      impl.createXMLStreamWriter(new ByteArrayOutputStream(), "UTF-8");
      fail();
    } catch (XMLStreamException expected) {
      
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
    assertEquals(SaxonWriterImpl.DummyEventWriter.class, impl.createXMLEventWriter(new ByteArrayOutputStream()).getClass());
    assertEquals(SaxonWriterImpl.DummyEventWriter.class, impl.createXMLEventWriter(new StringWriter()).getClass());
    assertEquals(SaxonWriterImpl.DummyEventWriter.class, impl.createXMLEventWriter(new ByteArrayOutputStream(), null).getClass());
    try {
      impl.createXMLEventWriter(new StreamResult(new ByteArrayOutputStream()));
      fail();
    } catch (UnsupportedOperationException expected) {

    }
  }

  @Test
  public void testDummyEventWriter() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    XMLEventWriter writer = impl.createXMLEventWriter(new ByteArrayOutputStream());
    writer.setDefaultNamespace("xxx");
    writer.setPrefix("a", "b");
    assertNull(writer.getNamespaceContext());
    assertNull(writer.getPrefix("xxx"));
    XMLEventReader reader = null;
    try (StringReader r = new StringReader("<root/>")){
      reader = XMLInputFactory.newInstance().createXMLEventReader(r);
      writer.add(reader);
    } catch (XMLStreamException expected) {
      
    } finally {
      StaxUtils.closeQuietly(reader);
    }
    try {
      writer.add(XMLEventFactory.newInstance().createStartDocument());
    } catch (XMLStreamException expected) {

    }
    writer.flush();
    writer.close();
  }


}
