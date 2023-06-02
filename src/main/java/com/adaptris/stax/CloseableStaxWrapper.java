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

import static com.adaptris.stax.StaxUtils.closeQuietly;

import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;

/**
 * Wrapper around XMLEventReader and XMLEventWriter that implements {@link Closeable}
 *
 */
public class CloseableStaxWrapper implements Closeable {
  private transient XMLEventReader reader;
  private transient XMLEventWriter writer;

  public CloseableStaxWrapper(XMLEventReader r, XMLEventWriter w) {
    reader = r;
    writer = w;
  }

  @Override
  public void close() throws IOException {
    closeQuietly(reader);
    closeQuietly(writer);
  }

  public XMLEventReader reader() {
    return reader;
  }

  public XMLEventWriter writer() {
    return writer;
  }

}
