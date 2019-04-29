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
package com.adaptris.stax;

import javax.xml.stream.XMLInputFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is purely to spoof example generation by setting an alias that matches something from the
 * json-streaming project.
 * 
 */
@XStreamAlias("json-streaming-input")
public class DummyJsonStreamingInputFactory implements XmlInputFactoryBuilder {

  @Override
  public XMLInputFactory build() {
    return XMLInputFactory.newInstance();
  }

}
