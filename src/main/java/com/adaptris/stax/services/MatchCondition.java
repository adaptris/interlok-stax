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

import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.UnresolvedMetadataException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link InputCondition} implementation that matches against a regular expression.
 * 
 * @config stax-plugin-match-rule
 */
@ComponentProfile(
    summary = "InputCondition implementation that matches against a specific regular expression",
    since = "3.8.4")
@DisplayOrder(order = {"when", "matches"})
@XStreamAlias("stax-plugin-match-rule")
public class MatchCondition implements InputCondition {
  @InputFieldHint(expression = true)
  private String when;
  @NotNull
  private String matches;

  private transient Pattern matchPattern;

  public MatchCondition() {
    
  }
  
  @Override
  public void init() throws CoreException {
    try {
      Args.notBlank(getMatches(), "matches");
      matchPattern = Pattern.compile(getMatches());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public boolean matches(AdaptrisMessage msg) {
    // msg.resolve will throw a UnresolvableMetadataException...
    String contentType = "";
    try {
      contentType = StringUtils.defaultIfEmpty(msg.resolve(getWhen()), "");
    } catch (UnresolvedMetadataException e) {

    }
    return matchPattern.matcher(contentType).matches();
  }

  public String getWhen() {
    return when;
  }

  /**
   * Specify the value to check against when determining whether to apply the transform plugin.
   * 
   * @param s the value, which supports the {@code %message{}} syntax to resolve metadata.
   */
  public void setWhen(String s) {
    this.when = s;
  }

  /**
   * @see #setWhen(String)
   */
  public MatchCondition withWhen(String v) {
    setWhen(v);
    return this;
  }

  public String getMatches() {
    return matches;
  }

  /**
   * Set the regular expression to match {@link #getWhen()} against.
   * 
   * @param s a string that conforms to {@link java.util.Pattern} syntax.
   */
  public void setMatches(String s) {
    this.matches = s;
  }

  /**
   * @see #setMatches(String)
   */
  public MatchCondition withMatches(String regexp) {
    setMatches(regexp);
    return this;
  }

}
