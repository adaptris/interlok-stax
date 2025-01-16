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

import java.util.Arrays;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import javax.xml.stream.XMLOutputFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link XmlOutputFactoryBuilder} implementation that uses {@code net.sf.saxon.lib.SerializerFactory#getXMLStreamWriter()}.
 *
 *
 * @config stax-saxon-stream-writer
 */
@XStreamAlias("stax-saxon-stream-writer")
public class SaxonStreamWriterFactory implements XmlOutputFactoryBuilder {

  private transient static InheritableThreadLocal<SaxonWriterImpl> myFactory = new InheritableThreadLocal<>();

  @NotNull
  @AutoPopulated
  @Valid
  @InputFieldDefault(value = "empty set")
  private KeyValuePairSet outputProperties;

  public SaxonStreamWriterFactory() {
    super();
    setOutputProperties(new KeyValuePairSet());
  }

  public SaxonStreamWriterFactory(KeyValuePairSet outputProperties) {
    this();
    setOutputProperties(outputProperties);
  }

  public SaxonStreamWriterFactory(KeyValuePair... outputProperties) {
    this(new KeyValuePairSet(Arrays.asList(outputProperties)));
  }

  @Override
  public synchronized XMLOutputFactory build() {
    if (myFactory.get() == null) {
      myFactory.set(new SaxonWriterImpl(getOutputProperties()));
    }
    return myFactory.get();
  }

  /**
   * @return the outputProperties
   */
  public KeyValuePairSet getOutputProperties() {
    return outputProperties;
  }

  /**
   * Set any output properties required.
   * <p>
   * The keys should match the properties specified by {@code SaxonOutputKeys}; bear in mind no validation is done on the values or the
   * license required for those features.
   * </p>
   *
   * @param kvps
   *          any output properties to set
   */
  public void setOutputProperties(KeyValuePairSet kvps) {
    outputProperties = Args.notNull(kvps, "outputProperties");
  }

}
