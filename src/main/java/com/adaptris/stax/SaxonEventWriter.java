/*
 * Copyright Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adaptris.stax;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import com.adaptris.core.util.Args;

// Pretty stupid event writer that just delegates to the XMLStreamWriter.
// can't support Notations & interesting entity declarations (but it can support entity refs).
class SaxonEventWriter implements XMLEventWriter, AutoCloseable {

  private static final Map<Integer, EventTypeHandler> EVENT_TYPE_MAP;
  private transient final XMLStreamWriter writer;

  private enum EventTypeHandler implements EventTypeWriter {
    Attribute(XMLStreamConstants.ATTRIBUTE) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        Attribute attr = (Attribute) event;
        QName qname = attr.getName();
        writer.writeAttribute(qname.getPrefix(), qname.getNamespaceURI(), qname.getLocalPart(),
            attr.getValue());
      }
    },

    CDATA(XMLStreamConstants.CDATA) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCData(event.asCharacters().getData());
      }
    },
    Characters(XMLStreamConstants.CHARACTERS) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeCharacters(event.asCharacters().getData());
      }
    },
    Comment(XMLStreamConstants.COMMENT) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeComment(((Comment) event).getText());
      }
    },
    DTD(XMLStreamConstants.DTD) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeDTD(((DTD) event).getDocumentTypeDeclaration());
      }
    },
    EndDocument(XMLStreamConstants.END_DOCUMENT) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndDocument();
      }
    },
    EndElement(XMLStreamConstants.END_ELEMENT) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndElement();
      }
    },
    EntityDeclaration(XMLStreamConstants.ENTITY_DECLARATION) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        // no equivalent write method on XMLStreamWriter
        throw new XMLStreamException("ENTITY_DECLARATION not supported");
      }
    },
    EntityReference(XMLStreamConstants.ENTITY_REFERENCE) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEntityRef(((EntityReference) event).getName());
      }
    },
    Namespace(XMLStreamConstants.NAMESPACE) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        Namespace namespace = (Namespace) event;
        writer.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
      }
    },
    NotationDeclaration(XMLStreamConstants.NOTATION_DECLARATION) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        // no equivalent write method on XMLStreamWriter
        throw new XMLStreamException("NOTATION_DECLARATION not supported");
      }
    },
    ProcessingInstruction(XMLStreamConstants.PROCESSING_INSTRUCTION) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        ProcessingInstruction instr = (ProcessingInstruction) event;
        writer.writeProcessingInstruction(instr.getTarget(), instr.getData());
      }
    },
    Space(XMLStreamConstants.SPACE) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        // nothing to do...
      }
    },
    StartDocument(XMLStreamConstants.START_DOCUMENT) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        StartDocument startDocument = (StartDocument) event;
        if (startDocument.encodingSet()) {
          writer.writeStartDocument(startDocument.getCharacterEncodingScheme(),
              startDocument.getVersion());
        } else {
          writer.writeStartDocument(startDocument.getVersion());
        }
      }
    },
    StartElement(XMLStreamConstants.START_ELEMENT) {
      @Override
      public void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException {
        StartElement element = (StartElement) event;
        QName qname = element.getName();
        writer.writeStartElement(qname.getPrefix(), qname.getLocalPart(), qname.getNamespaceURI());
        // element.getNamespaces().forEachRemaining(e-> {
        // Attribute.write((Namespace) e, writer);
        // });
        Iterator itr = element.getNamespaces();
        while (itr.hasNext()) {
          Namespace.write((Namespace) itr.next(), writer);
        }
        itr = element.getAttributes();
        while (itr.hasNext()) {
          Attribute.write((Attribute) itr.next(), writer);
        }
      }
    };
    private int eventType;
    EventTypeHandler(int i) {
      eventType = i;
    }
  }

  static {
    Map<Integer, EventTypeHandler> map = new HashMap<>();
    for (EventTypeHandler e : EventTypeHandler.values()) {
      map.put(e.eventType, e);
    }
    EVENT_TYPE_MAP = Collections.unmodifiableMap(map);
  }

  public SaxonEventWriter(XMLStreamWriter writer) {
    this.writer = writer;
  }

  @Override
  public void add(XMLEvent e) throws XMLStreamException {
    Args.notNull(e, "xml-event");
    EventTypeHandler handler = EVENT_TYPE_MAP.get(e.getEventType());
    if (handler != null) {
      handler.write(e, writer);
    } else {
      throw new XMLStreamException("Cannot write event type: " + e.getEventType());
    }
  }

  @Override
  public void add(XMLEventReader reader) throws XMLStreamException {
    Args.notNull(reader, "xml-reader");
    while (reader.peek() != null) {
      add(reader.nextEvent());
    }
  }

  @Override
  public void close() throws XMLStreamException {
    writer.close();
  }

  @Override
  public void flush() throws XMLStreamException {
    writer.flush();
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return writer.getNamespaceContext();
  }

  @Override
  public String getPrefix(String uri) throws XMLStreamException {
    return writer.getPrefix(uri);
  }

  @Override
  public void setDefaultNamespace(String uri) throws XMLStreamException {
    writer.setDefaultNamespace(uri);
  }

  @Override
  public void setNamespaceContext(NamespaceContext ctx) throws XMLStreamException {
    writer.setNamespaceContext(ctx);
  }

  @Override
  public void setPrefix(String prefix, String uri) throws XMLStreamException {
    writer.setPrefix(prefix, uri);
  }

  @FunctionalInterface
  protected interface EventTypeWriter {
    void write(XMLEvent event, XMLStreamWriter writer) throws XMLStreamException;
  }
}
