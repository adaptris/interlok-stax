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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;

public abstract class StaxUtils {

  public static void closeQuietly(XMLStreamWriter w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception ignore) {

    }
  }

  public static void closeQuietly(XMLEventWriter w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception e) {

    }
  }


  public static void closeQuietly(XMLEventReader r) {
    try {
      if (r != null) {
        r.close();
      }
    } catch (Exception ignored) {

    }
  }

  /**
   * Create an XMLInputFactory with {@link XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES} as false
   */
  public static XMLInputFactory createInputFactory() {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    return factory;
  }

}
