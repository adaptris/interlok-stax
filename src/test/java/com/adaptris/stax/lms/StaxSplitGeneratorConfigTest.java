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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.junit.jupiter.api.Test;

import com.adaptris.stax.StaxUtils;

public class StaxSplitGeneratorConfigTest {

  @Test
  public void testMembers() throws Exception {
    MySplitGeneratorConfig cfg = new MySplitGeneratorConfig();
    assertNull(cfg.getPath());
    assertNull(cfg.getXmlEventReader());
    assertNull(cfg.getXmlEventReader());
    assertNull(cfg.getInputReader());
  }

  @Test
  public void testPath() throws Exception {
    MySplitGeneratorConfig cfg = new MySplitGeneratorConfig().withPath("/fred");
    assertEquals("/fred", cfg.getPath());
    assertNull(cfg.getXmlEventReader());
    assertNull(cfg.getXmlEventReader());
    assertNull(cfg.getInputReader());
  }

  @Test
  public void testReader() throws Exception {
    try (BufferedReader buf = new BufferedReader(new StringReader("<hello/>"))) {
      XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(buf);
      MySplitGeneratorConfig cfg = new MySplitGeneratorConfig().withXmlEventReader(reader);
      assertNull(cfg.getPath());
      assertEquals(reader, cfg.getXmlEventReader());
      assertEquals(reader, cfg.getXmlEventReader());
      assertNull(cfg.getInputReader());
      cfg.withXmlEventReader(reader);
      assertNull(cfg.getPath());
      assertEquals(reader, cfg.getXmlEventReader());
      assertEquals(reader, cfg.getXmlEventReader());
      assertNull(cfg.getInputReader());
      StaxUtils.closeQuietly(reader);
    } finally {
    }
  }

  @Test
  public void testInputReader() throws Exception {
    try (BufferedReader buf = new BufferedReader(new StringReader("<hello/>"))) {
      MySplitGeneratorConfig cfg = new MySplitGeneratorConfig().withInputReader(buf);
      assertNull(cfg.getPath());
      assertNull(cfg.getXmlEventReader());
      assertNull(cfg.getXmlEventReader());
      assertEquals(buf, cfg.getInputReader());
    } finally {
    }
  }

  private class MySplitGeneratorConfig extends StaxSplitGeneratorConfig {
  }

}
