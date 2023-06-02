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

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.xml.stream.XMLInputFactory;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default implementation of {@link XmlInputFactoryBuilder}.
 *
 * @config stax-default-stream-input
 */
@XStreamAlias("stax-default-stream-input")
public class DefaultInputFactory implements XmlInputFactoryBuilder {
  // Boolean properties as implied by XMLInputFactory
  private static final List<String> BOOLEAN_PROPERTIES = Arrays.asList(XMLInputFactory.IS_NAMESPACE_AWARE, XMLInputFactory.IS_VALIDATING,
      XMLInputFactory.IS_COALESCING, XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
      XMLInputFactory.SUPPORT_DTD);

  @AdvancedConfig
  @Valid
  private KeyValuePairSet inputFactoryProperties;

  public DefaultInputFactory() {
  }

  /**
   * Create a XMLInputFactory.
   *
   * <p>
   * Note that because {@link KeyValuePairSet} only supports string properties, then you probably won't be able to configure things like
   * {@link XMLInputFactory#REPORTER} using this class.
   * </p>
   */
  @Override
  public XMLInputFactory build() {
    return configure(StaxUtils.createInputFactory(), getInputFactoryProperties());
  }

  public KeyValuePairSet getInputFactoryProperties() {
    return inputFactoryProperties;
  }

  public void setInputFactoryProperties(KeyValuePairSet props) {
    inputFactoryProperties = props;
  }

  public DefaultInputFactory withInputFactoryProperties(KeyValuePairSet kvps) {
    setInputFactoryProperties(kvps);
    return this;
  }

  public DefaultInputFactory withInputFactoryProperties(KeyValuePair... kvps) {
    return withInputFactoryProperties(new KeyValuePairSet(Arrays.asList(kvps)));
  }

  /**
   * Create an XMLInputFactory instance with the associated properties.
   *
   * @param factory
   *          the {@code XMLInputFactory} that needs configuring
   * @param properties
   *          the properties, if null, a safe default is assumed.
   * @return a configured XMLInputFactory instance.
   * @see XMLInputFactory#IS_SUPPORTING_EXTERNAL_ENTITIES
   */
  protected static XMLInputFactory configure(XMLInputFactory factory, KeyValuePairSet properties) {
    Args.notNull(factory, "xml-input-factory");
    final KeyValuePairSet kvps = properties != null ? properties : new KeyValuePairSet();
    for (KeyValuePair p : kvps) {
      if (BOOLEAN_PROPERTIES.contains(p.getKey())) {
        factory.setProperty(p.getKey(), BooleanUtils.toBoolean(p.getValue()));
      } else {
        factory.setProperty(p.getKey(), p.getValue());
      }
    }
    return factory;
  }

}
