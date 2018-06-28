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

import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds an {@link XMLStreamWriter} using {@link XMLOutputFactory}.
 * 
 * @config stax-default-writer-factory
 *
 */
@XStreamAlias("stax-default-stream-writer")
public class DefaultWriterFactory extends StreamWriterFactoryImpl {

  @Override
  public XMLStreamWriter create(Writer w) throws XMLStreamException {
    return XMLOutputFactory.newInstance().createXMLStreamWriter(w);
  }

}
