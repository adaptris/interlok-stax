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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.util.LifecycleHelper;

public class PostTransformTest {

  @Test
  public void testLifecycle() throws Exception {
    AddMetadata postTransform = new AddMetadata();
    try {
      LifecycleHelper.initAndStart(postTransform);
    } finally {
      LifecycleHelper.stopAndClose(postTransform);
    }
  }

  @Test
  public void testApplyChanges() throws Exception {
    AddMetadata postTransform = new AddMetadata().withMetadata(new MetadataElement("a", "b"),
        new MetadataElement("c", "d"));
    AdaptrisMessage msg = TransformPluginServiceTest.createMessage();
    try {
      LifecycleHelper.initAndStart(postTransform);
      postTransform.applyChanges(msg);
    } finally {
      LifecycleHelper.stopAndClose(postTransform);
    }
    assertEquals("b", msg.getMetadataValue("a"));
    assertEquals("d", msg.getMetadataValue("c"));
  }

}
