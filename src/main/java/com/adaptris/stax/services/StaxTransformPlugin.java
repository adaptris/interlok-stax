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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.stax.CloseableStaxWrapper;
import com.adaptris.stax.XmlInputFactoryBuilder;
import com.adaptris.stax.XmlOutputFactoryBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Plugin} implementation that uses STaX to transform between 2 formats.
 * 
 * @config stax-transform-plugin
 *
 */
@XStreamAlias("stax-transform-plugin")
@ComponentProfile(summary = "transform-plugin implementation for TransformPluginServiceList",
    since = "3.8.4")
@DisplayOrder(
    order = {"inputCondition", "inputBuilder", "outputBuilder", "postTransform"})
public class StaxTransformPlugin implements Plugin {

  private static InputCondition NEVER_MATCH = (m) -> {
    return false;
  };

  private static PostTransform NO_OP_POST_PLUGIN = (m) -> {
    return m;
  };

  @NotNull
  private XmlInputFactoryBuilder inputBuilder;
  @NotNull
  private XmlOutputFactoryBuilder outputBuilder;

  @InputFieldDefault(value = "never fires")
  private InputCondition inputCondition;
  @InputFieldDefault(value = "never fires")
  private PostTransform postTransform;

  public StaxTransformPlugin() {
  }

  @Override
  public void init() throws CoreException {
    try {
      Args.notNull(getInputBuilder(), "inputFactory");
      Args.notNull(getOutputBuilder(), "outputFactory");
      LifecycleHelper.init(getInputCondition());
      LifecycleHelper.init(getPostTransform());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getInputCondition());
    LifecycleHelper.start(getPostTransform());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getInputCondition());
    LifecycleHelper.stop(getPostTransform());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getInputCondition());
    LifecycleHelper.close(getPostTransform());
  }

  public AdaptrisMessage transform(AdaptrisMessage msg) throws ServiceException {
    InputCondition input = ObjectUtils.defaultIfNull(getInputCondition(), NEVER_MATCH);
    PostTransform output = ObjectUtils.defaultIfNull(getPostTransform(), NO_OP_POST_PLUGIN);

    if (input.matches(msg)) {
      try (InputStream in = new BufferedInputStream(msg.getInputStream());
          OutputStream out = new BufferedOutputStream(msg.getOutputStream());
          CloseableStaxWrapper wrapper =
              new CloseableStaxWrapper(getInputBuilder().build().createXMLEventReader(in),
                  getOutputBuilder().build().createXMLEventWriter(out))) {
        wrapper.writer().add(wrapper.reader());
        output.applyChanges(msg);
      } catch (Exception e) {
        throw ExceptionHelper.wrapServiceException(e);
      }
    }
    return msg;
  }

  public XmlInputFactoryBuilder getInputBuilder() {
    return inputBuilder;
  }

  /**
   * Set the {@link XmlInputFactoryBuilder} to use when streaming the input.
   * 
   * @param fac the input factory.
   */
  public void setInputBuilder(XmlInputFactoryBuilder fac) {
    this.inputBuilder = Args.notNull(fac, "inputFactory");
  }

  public StaxTransformPlugin withInputBuilder(XmlInputFactoryBuilder f) {
    setInputBuilder(f);
    return this;
  }

  public XmlOutputFactoryBuilder getOutputBuilder() {
    return outputBuilder;
  }

  /**
   * Set the {@link XmlOutputFactoryBuilder} to use when streaming the output.
   * 
   * @param fac the output factory.
   */
  public void setOutputBuilder(XmlOutputFactoryBuilder fac) {
    this.outputBuilder = Args.notNull(fac, "outputFactory");
  }

  public StaxTransformPlugin withOutputBuilder(XmlOutputFactoryBuilder f) {
    setOutputBuilder(f);
    return this;
  }

  public InputCondition getInputCondition() {
    return inputCondition;
  }

  /**
   * Set the input rule that fires this plugin.
   * 
   * @param r th {@link InputCondition} implementation; by default it will never match.
   */
  public void setInputCondition(InputCondition r) {
    this.inputCondition = r;
  }

  /**
   * @see #setInputCondition(InputCondition)
   * 
   */
  public StaxTransformPlugin withInputCondition(InputCondition f) {
    setInputCondition(f);
    return this;
  }

  public PostTransform getPostTransform() {
    return postTransform;
  }

  /**
   * Set what changes about the message when this plugin is applied.
   * 
   * @param r the {@link PostTransform} implementation, by default nothing is done.
   */
  public void setPostTransform(PostTransform r) {
    this.postTransform = r;
  }

  /**
   * @see #setPostTransform(PostTransform)
   * 
   */
  public StaxTransformPlugin withPostTransform(PostTransform f) {
    setPostTransform(f);
    return this;
  }


}
