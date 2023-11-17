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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;

public class SaxonEventWriterTest {
  
  private static final String XML =
      "<root>\n"
          + "<test att='1'>one</test>\n"
          + "<test att='2'>two</test>\n"
          + "<!-- comment -->\n"
          + "<node><child>child</child>\n"
          + "</node>\n"
          + "</root>\n";

  private final static String XML_WITH_ENCODING = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
      + XML;

  private final static String XML_NO_ENCODING = "<?xml version=\"1.0\" ?>\n"
      + XML;

  @Test
  public void testCopyDocument_ViaEvent() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    StringReader xmlBuf = new StringReader(XML_WITH_ENCODING);
    XMLEventReader reader = XmlInputFactoryBuilder.defaultIfNull(new DefaultInputFactory()).build().createXMLEventReader(xmlBuf);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (SaxonEventWriter write = (SaxonEventWriter) impl.createXMLEventWriter(out);
        AutoCloseable c1 = wrap(reader);
        AutoCloseable c2 = out) {
      while (reader.peek() != null) {
        write.add(reader.nextEvent());
      }
    }
    try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
      XPath xpath = new XPath();
      Document d = XmlHelper.createDocument(in, DocumentBuilderFactoryBuilder.newInstance());
      assertEquals("one", xpath.selectSingleTextItem(d, "/root/test"));
    }
  }

  @Test
  public void testCopyDocument_ViaReader() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    StringReader xmlBuf = new StringReader(XML_NO_ENCODING);
    XMLEventReader reader = StaxUtils.createInputFactory().createXMLEventReader(xmlBuf);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (SaxonEventWriter writer = (SaxonEventWriter) impl.createXMLEventWriter(out);
        AutoCloseable c1 = wrap(reader);
        AutoCloseable c2 = out) {
      writer.add(reader);
    }
    try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
      XPath xpath = new XPath();
      Document d = XmlHelper.createDocument(in, DocumentBuilderFactoryBuilder.newInstance());
      assertEquals("one", xpath.selectSingleTextItem(d, "/root/test"));
    }
  }

  @Test
  public void testPrefix() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    Mockito.when(mocker.getPrefix(anyString())).thenReturn("prefix");
    Mockito.when(mocker.getNamespaceContext()).thenReturn(null);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.setPrefix("prefix", "uri");
      assertEquals("prefix", writer.getPrefix("uri"));
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).setPrefix(anyString(), anyString());
    Mockito.verify(mocker, Mockito.atLeast(1)).close();
  }

  @Test
  public void testNamespace() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);

    Mockito.when(mocker.getNamespaceContext()).thenReturn(SimpleNamespaceContext.create(createNamespaceEntries()));
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.setDefaultNamespace("xs");
      writer.setNamespaceContext(null);
      assertNotNull(writer.getNamespaceContext());
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).setDefaultNamespace(anyString());
    Mockito.verify(mocker, Mockito.atLeast(1)).getNamespaceContext();
    Mockito.verify(mocker, Mockito.atLeast(1)).setNamespaceContext(any());
    Mockito.verify(mocker, Mockito.atLeast(1)).close();
  }

  @Test
  public void testFlush() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.flush();
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).flush();
    Mockito.verify(mocker, Mockito.atLeast(1)).close();
  }

  @Test
  public void testUnknownEventType() throws Exception {
    SaxonWriterImpl impl = new SaxonWriterImpl(null);
    XMLEvent mock = Mockito.mock(XMLEvent.class);
    Mockito.when(mock.getEventType()).thenReturn(255);
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        SaxonEventWriter writer = (SaxonEventWriter) impl.createXMLEventWriter(out)) {
      try {
        writer.add(mock);
        fail();
      } catch (XMLStreamException expected) {
        assertTrue(expected.getMessage().contains("Cannot write event type: "));
      }
    }
  }

  @Test
  public void testWriteAttribute() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    Attribute mockEvent = createMockAttribute();
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeAttribute(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  public void testWriteCData() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    XMLEvent mockEvent = Mockito.mock(XMLEvent.class);
    Characters mockChars = Mockito.mock(Characters.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.CDATA);
    Mockito.when(mockChars.getData()).thenReturn("data");
    Mockito.when(mockEvent.asCharacters()).thenReturn(mockChars);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeCData(anyString());
  }

  @Test
  public void testWriteCharacters() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    XMLEvent mockEvent = Mockito.mock(XMLEvent.class);
    Characters mockChars = Mockito.mock(Characters.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.CHARACTERS);
    Mockito.when(mockChars.getData()).thenReturn("data");
    Mockito.when(mockEvent.asCharacters()).thenReturn(mockChars);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeCharacters(anyString());
  }

  @Test
  public void testWriteComment() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    Comment mockEvent = Mockito.mock(Comment.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.COMMENT);
    Mockito.when(mockEvent.getText()).thenReturn("comment");
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeComment(anyString());
  }

  @Test
  public void testWriteDTD() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    DTD mockEvent = Mockito.mock(DTD.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.DTD);
    Mockito.when(mockEvent.getDocumentTypeDeclaration()).thenReturn("dtd-decl");
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeDTD(anyString());
  }

  @Test
  public void testWriteEndDocument() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    EndDocument mockEvent = Mockito.mock(EndDocument.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.END_DOCUMENT);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeEndDocument();
  }

  @Test
  public void testWriteEndElement() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    EndElement mockEvent = Mockito.mock(EndElement.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.END_ELEMENT);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeEndElement();
  }

  @Test
  public void testWriteEntityDeclaration() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    EntityDeclaration mockEvent = Mockito.mock(EntityDeclaration.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.ENTITY_DECLARATION);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
      fail();
    } catch (XMLStreamException expected) {
      assertTrue(expected.getMessage().contains("ENTITY_DECLARATION not supported"));
    }
  }

  @Test
  public void testWriteEntityReference() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    EntityReference mockEvent = Mockito.mock(EntityReference.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.ENTITY_REFERENCE);
    Mockito.when(mockEvent.getName()).thenReturn("name");
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeEntityRef(anyString());
  }

  @Test
  public void testWriteNamespace() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    Namespace mockEvent = createMockNamespace();
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeNamespace(anyString(), anyString());
  }

  @Test
  public void testWriteNotationDeclaration() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    NotationDeclaration mockEvent = Mockito.mock(NotationDeclaration.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.NOTATION_DECLARATION);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
      fail();
    } catch (XMLStreamException expected) {
      expected.printStackTrace();
      assertTrue(expected.getMessage().contains("NOTATION_DECLARATION not supported"));
    }
  }

  @Test
  public void testWriteProcessingInstruction() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    ProcessingInstruction mockEvent = Mockito.mock(ProcessingInstruction.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.PROCESSING_INSTRUCTION);
    Mockito.when(mockEvent.getTarget()).thenReturn("target");
    Mockito.when(mockEvent.getData()).thenReturn("data");
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeProcessingInstruction(anyString(), anyString());
  }

  @Test
  public void testWriteSpace() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    XMLEvent mockEvent = Mockito.mock(XMLEvent.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.SPACE);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
  }

  @Test
  public void testWriteStartDocument_NoEncoding() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    StartDocument mockEvent = Mockito.mock(StartDocument.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.START_DOCUMENT);
    Mockito.when(mockEvent.getVersion()).thenReturn("1.0");
    Mockito.when(mockEvent.encodingSet()).thenReturn(false);
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.times(0)).writeStartDocument(anyString(), anyString());
    Mockito.verify(mocker, Mockito.atLeast(1)).writeStartDocument(anyString());
  }

  @Test
  public void testWriteStartDocument_Encoding() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    StartDocument mockEvent = Mockito.mock(StartDocument.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.START_DOCUMENT);
    Mockito.when(mockEvent.getVersion()).thenReturn("1.0");
    Mockito.when(mockEvent.encodingSet()).thenReturn(true);
    Mockito.when(mockEvent.getCharacterEncodingScheme()).thenReturn("UTF-8");
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeStartDocument(anyString(), anyString());
    Mockito.verify(mocker, Mockito.times(0)).writeStartDocument(anyString());
  }

  @Test
  public void testWriteStartElement() throws Exception {
    XMLStreamWriter mocker = Mockito.mock(XMLStreamWriter.class);
    StartElement mockEvent = Mockito.mock(StartElement.class);
    List<Namespace> namespaceList = new ArrayList<>(Arrays.asList(createMockNamespace()));
    List<Attribute> attributeList = new ArrayList<>(Arrays.asList(createMockAttribute()));

    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
    QName mockQName = createMockQName();
    Mockito.when(mockEvent.getName()).thenReturn(mockQName);
    Mockito.when(mockEvent.getNamespaces()).thenReturn(namespaceList.iterator());
    Mockito.when(mockEvent.getAttributes()).thenReturn(attributeList.iterator());
    try (SaxonEventWriter writer = new SaxonEventWriter(mocker)) {
      writer.add(mockEvent);
    }
    Mockito.verify(mocker, Mockito.atLeast(1)).writeStartElement(anyString(), anyString(), anyString());
    Mockito.verify(mocker, Mockito.atLeast(1)).writeNamespace(anyString(), anyString());
    Mockito.verify(mocker, Mockito.atLeast(1)).writeAttribute(anyString(), anyString(), anyString(), anyString());
  }

  private Namespace createMockNamespace() {
    Namespace mockEvent = Mockito.mock(Namespace.class);
    Mockito.when(mockEvent.getEventType()).thenReturn(XMLStreamConstants.NAMESPACE);
    Mockito.when(mockEvent.getPrefix()).thenReturn("prefix");
    Mockito.when(mockEvent.getNamespaceURI()).thenReturn("uri");
    return mockEvent;
  }

  private Attribute createMockAttribute() {
    Attribute mockAttr = Mockito.mock(Attribute.class);
    Mockito.when(mockAttr.getEventType()).thenReturn(XMLStreamConstants.ATTRIBUTE);
    Mockito.when(mockAttr.getValue()).thenReturn("value");
    QName mockQname = createMockQName();
    Mockito.when(mockAttr.getName()).thenReturn(mockQname);
    return mockAttr;
  }

  private QName createMockQName() {
    QName mockQname = Mockito.mock(QName.class);
    Mockito.when(mockQname.getLocalPart()).thenReturn("local-part");
    Mockito.when(mockQname.getPrefix()).thenReturn("prefix");
    Mockito.when(mockQname.getNamespaceURI()).thenReturn("namespace-uri");
    return mockQname;
  }

  public static AutoCloseable wrap(XMLEventReader r) {
    if (r instanceof AutoCloseable) {
      return (AutoCloseable) r;
    }
    return () -> {
      r.close();
    };
  }

  private static KeyValuePairSet createNamespaceEntries() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("xsd", "http://www.w3.org/2001/XMLSchema"));
    result.add(new KeyValuePair("xs", "http://www.w3.org/2001/XMLSchema"));
    return result;
  }

  @FunctionalInterface
  protected interface AutoCloseableWrapper extends AutoCloseable {
    @Override
    void close() throws Exception;
  }

}
