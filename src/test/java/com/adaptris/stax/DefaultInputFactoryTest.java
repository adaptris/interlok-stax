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
import static org.junit.Assert.assertNotNull;
import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.util.KeyValuePair;

public class DefaultInputFactoryTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}


  @Test
  public void testBuild() throws Exception {
    DefaultInputFactory factory = new DefaultInputFactory();
    assertNotNull(factory.build());
  }

  @Test
  public void testBuild_WithProperties() throws Exception {
    DefaultInputFactory factory = new DefaultInputFactory()
        .withInputFactoryProperties(new KeyValuePair(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE.toString()));
    assertNotNull(factory.build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuild_WithIllegal() throws Exception {
    DefaultInputFactory factory = new DefaultInputFactory()
        .withInputFactoryProperties(new KeyValuePair("this-is-my-property", Boolean.TRUE.toString()));
    factory.build();
  }

  @Test
  public void testConfigure() throws Exception {
    assertNotNull(DefaultInputFactory.configure(StaxUtils.createInputFactory(), null));
    DefaultInputFactory factory = new DefaultInputFactory().withInputFactoryProperties(
        new KeyValuePair("this-is-my-property", Boolean.TRUE.toString()),
        new KeyValuePair(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE.toString()));
    XMLInputFactory xmlF = DefaultInputFactory.configure(new DummyXmlInputFactory(),
        factory.getInputFactoryProperties());
    assertEquals(DummyXmlInputFactory.class, xmlF.getClass());
  }


  private class DummyXmlInputFactory extends XMLInputFactory {

    @Override
    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, Reader reader)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream stream, String encoding)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, InputStream stream)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter)
        throws XMLStreamException {
      return null;
    }

    @Override
    public XMLResolver getXMLResolver() {
      return null;
    }

    @Override
    public void setXMLResolver(XMLResolver resolver) {
    }

    @Override
    public XMLReporter getXMLReporter() {
      return null;
    }

    @Override
    public void setXMLReporter(XMLReporter reporter) {
    }

    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException {
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
      return null;
    }

    @Override
    public boolean isPropertySupported(String name) {
      return false;
    }

    @Override
    public void setEventAllocator(XMLEventAllocator allocator) {
    }

    @Override
    public XMLEventAllocator getEventAllocator() {
      return null;
    }

  }
}
