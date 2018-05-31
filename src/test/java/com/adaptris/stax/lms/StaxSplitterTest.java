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
package com.adaptris.stax.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.CloseableIterable;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

public class StaxSplitterTest {

  private static final String XML_MESSAGE = "<?xml version=\"1.0\" "
      + "encoding=\"UTF-8\"?>" + System.lineSeparator() + "<envelope>" + System.lineSeparator()
      + "<document><nested>1</nested></document>" + System.lineSeparator() + "<document><nested>2</nested></document>"
      + System.lineSeparator() + "<document><nested>3</nested></document>"
      + System.lineSeparator() + "</envelope>";

  @Test
  public void testSplit() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    for (int i = 0; i < list.size(); i++) {
      XPath xpath = new XPath();
      Document d = XmlHelper.createDocument(list.get(i), DocumentBuilderFactoryBuilder.newInstance());
      assertEquals("" + (i + 1), xpath.selectSingleTextItem(d, "/document/nested"));
    }
  }

  @Test(expected = CoreException.class)
  public void testSplit_NotFound() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document/x");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    splitter.splitMessage(msg);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSplit_Remove() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    try (CloseableIterable<AdaptrisMessage> i = ensureCloseable(splitter.splitMessage(msg))) {
      i.iterator().remove();
    };
  }

  @Test
  public void testSplit_NoSlashPrefix() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    for (int i = 0; i < list.size(); i++) {
      XPath xpath = new XPath();
      Document d = XmlHelper.createDocument(list.get(i), DocumentBuilderFactoryBuilder.newInstance());
      assertEquals("" + (i + 1), xpath.selectSingleTextItem(d, "/document/nested"));
    }
  }

  @Test
  public void testBufferSize() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document");
    assertEquals(8192, splitter.bufferSize());
    assertNull(splitter.getBufferSize());
    splitter.setBufferSize(1024);
    assertEquals(1024, splitter.bufferSize());
  }

  @Test
  public void testEncoding() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document");
    splitter.setEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", splitter.getEncoding());    
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE, "UTF-8");
    assertEquals("ISO-8859-1", splitter.evaluateEncoding(msg));
    splitter.setEncoding(null);
    assertEquals("UTF-8", splitter.evaluateEncoding(msg));

    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    assertEquals("UTF-8", splitter.evaluateEncoding(msg));
  }

  protected static List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) {
    if (iter instanceof List) {
      return (List<AdaptrisMessage>) iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (CloseableIterable<AdaptrisMessage> messages = ensureCloseable(iter)) {
      for (AdaptrisMessage msg : messages) {
        result.add(msg);
      }
    } catch (IOException e) {
    }
    return result;
  }

  private static <E> CloseableIterable<E> ensureCloseable(final Iterable<E> iter) {
    if (iter instanceof CloseableIterable) {
      return (CloseableIterable<E>) iter;
    }

    return new CloseableIterable<E>() {
      @Override
      public void close() throws IOException {
        // No-op
      }

      @Override
      public Iterator<E> iterator() {
        return iter.iterator();
      }
    };
  }
}
