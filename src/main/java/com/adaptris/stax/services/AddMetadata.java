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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link PostTransform} implementation that adds metadata.
 * 
 * <p>
 * Note that this wraps {@link AddMetadataService} under the covers with
 * {@link AddMetadataService#setOverwrite(Boolean)} true, so behaviour is consistent with the
 * documentation for that class.
 * </p>
 * 
 * @config stax-plugin-add-metadata
 */
@ComponentProfile(summary = "PostTransform implementation that adds metadata",
    since = "3.8.4")
@XStreamAlias("stax-plugin-add-metadata")
public class AddMetadata implements PostTransform {

  @XStreamImplicit
  @Valid
  @NotNull
  @AutoPopulated
  @AffectsMetadata
  private Set<MetadataElement> metadataElements;

  private transient AddMetadataService wrappedService;

  public AddMetadata() {
    setMetadataElements(new HashSet<>());
  }

  @Override
  public void init() throws CoreException {
    wrappedService = new AddMetadataService(getMetadataElements());
    wrappedService.setOverwrite(true);
    LifecycleHelper.initAndStart(wrappedService);
  }

  @Override
  public void close() {
    LifecycleHelper.stopAndClose(wrappedService);
  }

  @Override
  public AdaptrisMessage applyChanges(AdaptrisMessage msg) throws ServiceException {
    wrappedService.doService(msg);
    return msg;
  }

  public Set<MetadataElement> getMetadataElements() {
    return metadataElements;
  }

  public void setMetadataElements(Set<MetadataElement> metadata) {
    this.metadataElements = Args.notNull(metadata, "metadata");
  }

  public AddMetadata withMetadata(Set<MetadataElement> e) {
    setMetadataElements(e);
    return this;
  }

  public AddMetadata withMetadata(MetadataElement... elements) {
    return withMetadata(new HashSet<MetadataElement>(Arrays.asList(elements)));
  }
}
