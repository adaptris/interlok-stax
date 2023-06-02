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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;

public class MatchConditionTest {

  @Test
  public void testLifecycle() throws Exception {
    MatchCondition condition = new MatchCondition();
    try {
      LifecycleHelper.initAndStart(condition);
      fail();
    } catch (CoreException expected) {

    } finally {
      LifecycleHelper.stopAndClose(condition);
    }
    condition.setMatches("application/json");
    try {
      LifecycleHelper.initAndStart(condition);
    } finally {
      LifecycleHelper.stopAndClose(condition);
    }
  }

  @Test
  public void testMatch() throws Exception {
    MatchCondition condition =
        new MatchCondition().withWhen("%message{Content-Type}").withMatches("application/xml");
    AdaptrisMessage msg = TransformPluginServiceTest.createMessage();
    try {
      LifecycleHelper.initAndStart(condition);
      assertTrue(condition.matches(msg));
    } finally {
      LifecycleHelper.stopAndClose(condition);
    }
  }

  @Test
  public void testNoMatch() throws Exception {
    MatchCondition condition =
        new MatchCondition().withWhen("Content-Type").withMatches("text/xml");
    AdaptrisMessage msg = TransformPluginServiceTest.createMessage();
    try {
      LifecycleHelper.initAndStart(condition);
      assertFalse(condition.matches(msg));
    } finally {
      LifecycleHelper.stopAndClose(condition);
    }
  }

}
