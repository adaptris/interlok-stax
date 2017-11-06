package com.adaptris.stax;

import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

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

}
