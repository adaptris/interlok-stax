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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;

/**
 * Whether or not the configured plugin should be fired or not.
 * 
 */
@FunctionalInterface
public interface InputCondition extends ComponentLifecycle {

  /**
   * Whether or not this message should have the plugin applied.
   * 
   * @param msg the message
   * @return true or false.
   */
  boolean matches(AdaptrisMessage msg);
}
