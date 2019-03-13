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

import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Allows you to optionally transform from one format to another upon entry and exit of this
 * service.
 * <p>
 * This allows you to transform the message payload from one format to another upon entry and exit
 * to this {@link ServiceList} implementation. This means you that you can automatically convert
 * between JSON and XML (using {@code com.adaptris.core.json.streaming.JsonStreamingInputFactory}
 * and {@code XmlOutputFactoryBuilder}; do your services treating the payload as XML, and render the
 * result as JSON automatically.
 * </p>
 * <p>
 * The standard implementation of {@link Plugin} which is {@link StaxTransformPlugin}
 * allows you to specify a rule under which you perform the transforms (an expression based metadata
 * value against a regular expression). If the rule is true, then the transform is applied;
 * otherwise it is a no-op operation.
 * </p>
 * <p>
 * Note that this behaviour is achievable in a number of ways with Interlok itself already, most easily with 
 * the {@code interlok-config-conditional} package; this is simply a convenience. There will also be 
 * situations where streaming from JSON to XML won't work, such as when the incoming message is a JSON 
 * array (you may need to have additional post configuration) or when the XML has attributes.
 * </p>
 * 
 * @config stax-plugin-service-list
 *
 */
@ComponentProfile(
    summary = "Conditionally map from one format to another on entry and exit via STaX",
    since = "3.8.4", tag = "xml,stax,json")
@DisplayOrder(order = {"onEntry", "onExit"})
@XStreamAlias("stax-plugin-service-list")
public class ServiceListWithPlugin extends ServiceList {

  private static Plugin NO_OP = (m) -> {
    return m;
  };

  private Plugin onEntry;
  private Plugin onExit;

  @Override
  protected void applyServices(AdaptrisMessage msg) throws ServiceException {
    try {
      ObjectUtils.defaultIfNull(getOnEntry(), NO_OP).transform(msg);
      super.applyServices(msg);
      ObjectUtils.defaultIfNull(getOnExit(), NO_OP).transform(msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
    }
  }

  @Override
  protected void doInit() throws CoreException {
    LifecycleHelper.init(getOnEntry());
    LifecycleHelper.init(getOnExit());
  }

  @Override
  protected void doStart() throws CoreException {
    LifecycleHelper.start(getOnEntry());
    LifecycleHelper.start(getOnExit());

  }

  @Override
  protected void doStop() {
    LifecycleHelper.stop(getOnEntry());
    LifecycleHelper.stop(getOnExit());

  }

  @Override
  protected void doClose() {
    LifecycleHelper.close(getOnEntry());
    LifecycleHelper.close(getOnExit());
  }

  public Plugin getOnEntry() {
    return onEntry;
  }

  public void setOnEntry(Plugin onEntry) {
    this.onEntry = onEntry;
  }

  public ServiceListWithPlugin withOnEntry(Plugin onEntry) {
    setOnEntry(onEntry);
    return this;
  }

  public Plugin getOnExit() {
    return onExit;
  }

  public void setOnExit(Plugin onExit) {
    this.onExit = onExit;
  }

  public ServiceListWithPlugin withOnExit(Plugin onEntry) {
    setOnExit(onEntry);
    return this;
  }

}
