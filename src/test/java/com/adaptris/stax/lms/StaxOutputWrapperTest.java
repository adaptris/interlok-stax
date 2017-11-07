package com.adaptris.stax.lms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.stax.lms.StaxOutputWrapper.StaxConfig;

public class StaxOutputWrapperTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}


  @Test
  public void testDefaults() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    assertEquals("UTF-8", wrapper.getStaxConfig(StaxConfig.Encoding.name()));
    assertEquals("root", wrapper.getStaxConfig(StaxConfig.RootElement.name()));
    assertEquals("", wrapper.getStaxConfig(StaxConfig.Prefix.name()));
    assertEquals("", wrapper.getStaxConfig(StaxConfig.NamespaceURI.name()));
    System.out.println(wrapper);
    assertTrue(wrapper.toString().contains("UTF-8"));
  }

  @Test
  public void testBuilder() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    assertEquals("ISO-8859-1", wrapper.withEncoding("ISO-8859-1").getStaxConfig(StaxConfig.Encoding));
    assertEquals("hello", wrapper.withRootElement("hello").getStaxConfig(StaxConfig.RootElement));
    assertEquals("hello", wrapper.withPrefix("hello").getStaxConfig(StaxConfig.Prefix));
    assertEquals("hello", wrapper.withNamespaceURI("hello").getStaxConfig(StaxConfig.NamespaceURI));
  }

  @Test
  public void testEventWriter() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    try {
      wrapper.eventWriter();
      fail();
    } catch (IllegalArgumentException expected) {

    }
    assertNotNull(wrapper.start().eventWriter());
    wrapper.close();
  }

  @Test
  public void testClose() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    StaxOutputWrapper wrapper = new StaxOutputWrapper(f);
    wrapper.close();
    wrapper.start().close();
  }

}
