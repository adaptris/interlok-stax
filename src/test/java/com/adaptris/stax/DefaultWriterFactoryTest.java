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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;

public class DefaultWriterFactoryTest {

  @Test
  public void testCreate() throws Exception {
    DefaultWriterFactory factory = new DefaultWriterFactory();
    assertNotNull(factory.create(new StringWriter()));
  }

  @Test
  public void testClose() throws Exception {
    DefaultWriterFactory factory = new DefaultWriterFactory();
    XMLStreamWriter writer = factory.create(new StringWriter());
    assertNotNull(writer);
    factory.close(writer);
    factory.close((XMLStreamWriter) null);
  }

}
