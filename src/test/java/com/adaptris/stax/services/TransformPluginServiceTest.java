/*
 * Copyright Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.adaptris.stax.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.transform.XmlTransformService;
import com.adaptris.core.transform.XmlValidationService;
import com.adaptris.core.transform.schema.BasicXmlSchemaValidator;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.stax.DefaultInputFactory;
import com.adaptris.stax.DefaultWriterFactory;
import com.adaptris.stax.DummyJsonStreamingInputFactory;
import com.adaptris.stax.DummyJsonStreamingOutput;
import com.adaptris.stax.SaxonStreamWriterFactory;

public class TransformPluginServiceTest extends ExampleServiceCase {
  private static final String XML_MESSAGE = "<?xml version=\"1.0\" " + "encoding=\"UTF-8\"?>"
      + System.lineSeparator() + "<envelope>" + System.lineSeparator()
      + "<document><nested>1</nested></document>" + System.lineSeparator()
      + "<document><nested>2</nested></document>" + System.lineSeparator()
      + "<document><nested>3</nested></document>" + System.lineSeparator() + "</envelope>";

  @Override
  protected ServiceListWithPlugin retrieveObjectForSampleConfig() {
    StaxTransformPlugin onEntry = new StaxTransformPlugin()
        .withInputCondition(
            new MatchCondition().withWhen("%message{Content-Type}").withMatches("application/json"))
        .withInputBuilder(new DummyJsonStreamingInputFactory())
        .withOutputBuilder(new DefaultWriterFactory());
    StaxTransformPlugin onExit = new StaxTransformPlugin()
        .withInputCondition(new MatchCondition().withWhen("%message{Accept}").withMatches("application/json"))
        .withPostTransform(
            new AddMetadata().withMetadata(new MetadataElement("Content-Type", "application/json")))
        .withInputBuilder(new DefaultInputFactory())
        .withOutputBuilder(new DummyJsonStreamingOutput());

    XmlTransformService transform = new XmlTransformService();
    transform.setUrl("http://localhost:8080/path/to/transform.xsl");
    XmlValidationService validator = new XmlValidationService(
        new BasicXmlSchemaValidator().withSchema("http://localhost:8080/path/to/schema.xsd"));
    ServiceListWithPlugin list = new ServiceListWithPlugin().withOnEntry(onEntry)
        .withOnExit(onExit).withServices(new XmlValidationService(
            new BasicXmlSchemaValidator().withSchema("http://localhost:8080/path/to/schema.xsd")),
            transform);
    return list;
  }

  @Test
  public void testInit() throws Exception {
    ServiceListWithPlugin service = new ServiceListWithPlugin();
    service.setOnEntry(new StaxTransformPlugin());
    // won't init
    try {
      LifecycleHelper.initAndStart(service);
      fail();
    } catch (CoreException expected) {

    }
    service = createForTests();
    try {
      LifecycleHelper.initAndStart(service);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = createMessage();
    ServiceListWithPlugin service = new ServiceListWithPlugin();
    execute(service, msg);
    // This effectively does nothing
    assertEquals("application/xml", msg.getMetadataValue("Content-Type"));
  }

  @Test
  public void testDoService_WithMatches() throws Exception {
    AdaptrisMessage msg = createMessage();
    ServiceListWithPlugin service = createForTests();
    execute(service, msg);
    // This should have changed the Content-Type key to text/xml
    assertEquals("text/xml", msg.getMetadataValue("Content-Type"));
  }

  @Test
  public void testDoService_NoMatch() throws Exception {
    AdaptrisMessage msg = createMessage();
    msg.addMetadata(new MetadataElement("Content-Type", "plain/text"));
    msg.addMetadata(new MetadataElement("Accept", "plain/text"));
    ServiceListWithPlugin service = createForTests();
    execute(service, msg);
    assertEquals("plain/text", msg.getMetadataValue("Content-Type"));
  }

  @Test
  public void testDoService_NoMetadata() throws Exception {
    AdaptrisMessage msg = createMessage();
    msg.clearMetadata();
    ServiceListWithPlugin service = createForTests();
    execute(service, msg);
    assertNull(msg.getMetadataValue("Content-Type"));
  }

  @Test
  public void testDoService_Exception() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage(XML_MESSAGE);
    msg.addMetadata(new MetadataElement("Content-Type", "application/xml"));
    msg.addMetadata(new MetadataElement("Accept", "application/xml"));
    ServiceListWithPlugin service = createForTests();
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {
    }
  }


  public static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_MESSAGE);
    msg.addMetadata(new MetadataElement("Content-Type", "application/xml"));
    msg.addMetadata(new MetadataElement("Accept", "application/xml"));
    return msg;
  }

  private ServiceListWithPlugin createForTests() {
    StaxTransformPlugin onEntry = new StaxTransformPlugin()
        .withInputCondition(new MatchCondition().withWhen("%message{Content-Type}").withMatches("application/xml"))
        .withPostTransform(
            new AddMetadata().withMetadata(new MetadataElement("Content-Type", "application/xml")))
        .withInputBuilder(new DefaultInputFactory())
        .withOutputBuilder(new SaxonStreamWriterFactory());
    StaxTransformPlugin onExit = new StaxTransformPlugin()
        .withInputCondition(new MatchCondition().withWhen("%message{Accept}").withMatches("application/xml"))
        .withPostTransform(
            new AddMetadata().withMetadata(new MetadataElement("Content-Type", "text/xml")))
        .withInputBuilder(new DefaultInputFactory())
        .withOutputBuilder(new SaxonStreamWriterFactory());

    ServiceListWithPlugin list =
        new ServiceListWithPlugin().withOnEntry(onEntry).withOnExit(onExit);
    return list;
  }
}
