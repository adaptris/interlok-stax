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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.SerializerFactory;

// to avoid possible NoClassDef issues in the event there is no saxon available (but you're just using the default).
// Of course if you actually configure a SaxonStreamWriterFactory, then there will be a NoClassDef
class SaxonWriterImpl extends XMLOutputFactory {

  private transient static InheritableThreadLocal<SerializerFactory> mySerializer = new InheritableThreadLocal<>() {
    @Override
    protected SerializerFactory initialValue() {
      return new SerializerFactory(Configuration.newConfiguration());
    }
  };

  private transient KeyValuePairSet outputProperties;

  protected SaxonWriterImpl(KeyValuePairSet conf) {
    outputProperties = conf;
  }

  protected XMLStreamWriter create(StreamResult result) throws XMLStreamException {
    try {
      return mySerializer.get().getXMLStreamWriter(result, KeyValuePairBag.asProperties(outputProperties));
    } catch (Exception e) {
      throw wrapException(e);
    }
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
    return create(new StreamResult(stream));
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
    return create(new StreamResult(stream));
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding) throws XMLStreamException {
    try {
      return create(new StreamResult(new OutputStreamWriter(stream, StringUtils.defaultIfBlank(encoding, "UTF-8"))));
    } catch (Exception e) {
      throw wrapException(e);
    }
  }

  @Override
  public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
    throw new UnsupportedOperationException();
  }

  @Override
  public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
    throw new UnsupportedOperationException();
  }

  @Override
  public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
    return new SaxonEventWriter(createXMLStreamWriter(stream));
  }

  @Override
  public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding) throws XMLStreamException {
    return new SaxonEventWriter(createXMLStreamWriter(stream, encoding));
  }

  @Override
  public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException {
    return new SaxonEventWriter(createXMLStreamWriter(stream));
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

  private static XMLStreamException wrapException(Throwable e) {
    if (e instanceof XMLStreamException) {
      return (XMLStreamException) e;
    }
    return new XMLStreamException(e);
  }

}
