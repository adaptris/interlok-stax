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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.adaptris.core.MetadataElement;
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

  @Test
  public void testSplit_NotFound_Suppress() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document/x");
    splitter.setSuppressPathNotFound(true);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(0, list.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSplit_Remove() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    try (CloseableIterable<AdaptrisMessage> i = CloseableIterable.ensureCloseable(splitter.splitMessage(msg))) {
      i.iterator().remove();
    };
  }

  @Test
  public void testSplit_NoSlashPrefix() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("envelope/document").withXmlDocumentFactoryConfig(null)
        .withNamespaceContext(null);
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
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document").withEncoding("ISO-8859-1");
    assertEquals("ISO-8859-1", splitter.getEncoding());    
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE, "UTF-8");
    assertEquals("ISO-8859-1", splitter.evaluateEncoding(msg));
    splitter.setEncoding(null);
    assertEquals("UTF-8", splitter.evaluateEncoding(msg));

    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    assertEquals("UTF-8", splitter.evaluateEncoding(msg));
  }

  @Test
  public void testSplitCopyMetadata() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE, Collections.singleton(new MetadataElement("key", "value")));
    List<AdaptrisMessage> list = toList(splitter.splitMessage(msg));
    assertEquals(3, list.size());
    for (int i = 0; i < list.size(); i++) {
      XPath xpath = new XPath();
      Document d = XmlHelper.createDocument(list.get(i), DocumentBuilderFactoryBuilder.newInstance());
      assertEquals("" + (i + 1), xpath.selectSingleTextItem(d, "/document/nested"));
      assertTrue(list.get(i).headersContainsKey("key"));
      assertEquals("value", list.get(i).getMetadataValue("key"));
    }
  }

  @Test(expected = CoreException.class)
  public void testSplit_DoubleIterator() throws Exception {
    StaxPathSplitter splitter = new StaxPathSplitter("/envelope/document/x");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    Iterable<AdaptrisMessage> iterable = splitter.splitMessage(msg);
    for (Iterator<AdaptrisMessage> i =  iterable.iterator(); i.hasNext();) {
      i.next();
    }
    // This should throw an IllegalState
    Iterator<AdaptrisMessage> i =  iterable.iterator();  
  }


  protected static List<AdaptrisMessage> toList(Iterable<AdaptrisMessage> iter) {
    if (iter instanceof List) {
      return (List<AdaptrisMessage>) iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (CloseableIterable<AdaptrisMessage> messages = CloseableIterable.ensureCloseable(iter)) {
      for (AdaptrisMessage msg : messages) {
        result.add(msg);
      }
    } catch (IOException e) {
    }
    return result;
  }

}
