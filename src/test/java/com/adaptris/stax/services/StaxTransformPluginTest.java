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
package com.adaptris.stax.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.stax.DefaultInputFactory;
import com.adaptris.stax.SaxonStreamWriterFactory;

public class StaxTransformPluginTest {

  @Test
  public void testLifecycle() throws Exception {
    StaxTransformPlugin plugin = new StaxTransformPlugin();
    try {
      LifecycleHelper.initAndStart(plugin);
      fail();
    } catch (CoreException expected) {

    } finally {
      LifecycleHelper.stopAndClose(plugin);
    }
    plugin = createForTests();
    try {
      LifecycleHelper.initAndStart(plugin);
    } finally {
      LifecycleHelper.stopAndClose(plugin);
    }
  }

  @Test
  public void testDefaultBehaviour() throws Exception {
    StaxTransformPlugin plugin = createForTests().withInputCondition(null).withPostTransform(null);
    AdaptrisMessage msg = TransformPluginServiceTest.createMessage();
    LifecycleHelper.initAndStart(plugin);
    plugin.transform(msg);
    assertEquals("application/xml", msg.getMetadataValue("Content-Type"));
  }

  @Test
  public void testNoPostTransform() throws Exception {
    StaxTransformPlugin plugin = createForTests().withPostTransform(null);
    AdaptrisMessage msg = TransformPluginServiceTest.createMessage();
    LifecycleHelper.initAndStart(plugin);
    plugin.transform(msg);
    assertEquals("application/xml", msg.getMetadataValue("Content-Type"));
  }

  @Test
  public void testTransform() throws Exception {
    StaxTransformPlugin plugin = createForTests();
    AdaptrisMessage msg = TransformPluginServiceTest.createMessage();
    LifecycleHelper.initAndStart(plugin);
    plugin.transform(msg);
    assertEquals("text/xml", msg.getMetadataValue("Content-Type"));
  }



  private StaxTransformPlugin createForTests() {
    StaxTransformPlugin plugin = new StaxTransformPlugin()
        .withInputCondition(
            new MatchCondition().withWhen("%message{Content-Type}").withMatches("application/xml"))
        .withPostTransform(
            new AddMetadata().withMetadata(new MetadataElement("Content-Type", "text/xml")))
        .withInputBuilder(new DefaultInputFactory())
        .withOutputBuilder(new SaxonStreamWriterFactory());
    return plugin;
  }
}
