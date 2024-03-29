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
import javax.xml.stream.XMLStreamWriter;

/**
 * Pluggable implementation for building specific {@link XMLOutputFactory} instances.
 *
 *
 */
public interface XmlOutputFactoryBuilder extends StreamWriterFactory {

  /**
   * Create an output factory.
   *
   * @return a configured {@link XMLOutputFactory} instance.
   */
  XMLOutputFactory build();

  /**
   * Create a {@link XMLStreamWriter} that wraps the specified writer.
   *
   *
   */
  @Override
  default XMLStreamWriter create(Writer w) throws Exception {
    return build().createXMLStreamWriter(w);
  }

}
