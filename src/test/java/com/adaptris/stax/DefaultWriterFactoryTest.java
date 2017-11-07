package com.adaptris.stax;

import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultWriterFactoryTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}


  @Test
  public void testCreate() throws Exception {
    DefaultWriterFactory factory = new DefaultWriterFactory();
    assertNotNull(factory.create(new StringWriter()));
  }

  @Test
  public void testClose() throws Exception {
    DefaultWriterFactory factory = new DefaultWriterFactory();
    XMLStreamWriter writer = factory.create(new StringWriter());
    assertNotNull(writer);
    factory.close(writer);
    factory.close((XMLStreamWriter) null);
  }

}
