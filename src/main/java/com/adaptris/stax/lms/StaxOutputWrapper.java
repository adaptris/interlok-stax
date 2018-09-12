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

import static com.adaptris.stax.StaxUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.adaptris.core.util.Args;

final class StaxOutputWrapper implements Closeable {

  static enum StaxConfig {
    Encoding {

      @Override
      String initialValue() {
        return "UTF-8";
      }

    },
    RootElement {
      @Override
      String initialValue() {
        return "root";
      }
    },
    Prefix {
      @Override
      String initialValue() {
        return "";
      }
    },
    NamespaceURI {
      @Override
      String initialValue() {
        return "";
      }
    };

    abstract String initialValue();
  }

  private static final Map DEFAULT_CONFIG;

  static {
    Map<StaxConfig, String> map = new HashMap<>();
    for (StaxConfig c : StaxConfig.values()) {
      map.put(c, c.initialValue());
    }
    DEFAULT_CONFIG = Collections.unmodifiableMap(map);
  }

  private Map<StaxConfig, String> config;

  private File outputFile;
  private OutputStreamWriter output;
  private XMLEventWriter xmlWriter;
  private XMLEventFactory eventFactory = XMLEventFactory.newInstance();

  protected StaxOutputWrapper(File f) throws Exception {
    this.outputFile = f;
    config = new HashMap<>(DEFAULT_CONFIG);
  }

  StaxOutputWrapper withEncoding(String e) {
    config.put(StaxConfig.Encoding, Args.notBlank(e, "encoding"));
    return this;
  }

  StaxOutputWrapper withRootElement(String e) {
    config.put(StaxConfig.RootElement, defaultIfEmpty(e, "root"));
    return this;
  }

  StaxOutputWrapper withPrefix(String e) {
    config.put(StaxConfig.Prefix, defaultIfEmpty(e, ""));
    return this;
  }

  StaxOutputWrapper withNamespaceURI(String e) {
    config.put(StaxConfig.NamespaceURI, defaultIfEmpty(e, ""));
    return this;
  }

  XMLEventWriter eventWriter() {
    return Args.notNull(xmlWriter, "xmlWriter");
  }

  File outputFile() {
    return outputFile;
  }

  String getStaxConfig(StaxConfig e) {
    return config.get(e);
  }

  String getStaxConfig(String e) {
    return config.get(StaxConfig.valueOf(e));
  }


  protected StaxOutputWrapper start() throws Exception {
    this.output = new OutputStreamWriter(new FileOutputStream(outputFile), config.get(StaxConfig.Encoding));
    this.xmlWriter = XMLOutputFactory.newInstance().createXMLEventWriter(output);
    xmlWriter.add(eventFactory.createStartDocument(config.get(StaxConfig.Encoding), "1.0"));
    xmlWriter.add(eventFactory.createStartElement(config.get(StaxConfig.Prefix), config.get(StaxConfig.NamespaceURI),
        config.get(StaxConfig.RootElement)));
    return this;
  }


  protected StaxOutputWrapper finish() throws Exception {
    xmlWriter.add(eventFactory.createEndElement(config.get(StaxConfig.Prefix), config.get(StaxConfig.NamespaceURI),
        config.get(StaxConfig.RootElement)));
    xmlWriter.add(eventFactory.createEndDocument());
    return this;
  }

  @SuppressWarnings("deprecation")
  public void close() throws IOException {
    closeQuietly(xmlWriter);
    IOUtils.closeQuietly(output);
  }

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("outputFile", outputFile).append("config", config)
        .toString();
  }

}
