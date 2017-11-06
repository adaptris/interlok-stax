package com.adaptris.stax;

import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.stream.XMLStreamException;
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
    // This should work, and give 100% coverage; odd that it doesn't.
    factory.close(proxy(writer));
  }

  private XMLStreamWriter proxy(XMLStreamWriter writer) {
    return (XMLStreamWriter) Proxy.newProxyInstance(XMLStreamWriter.class.getClassLoader(), new Class[]
    {
        XMLStreamWriter.class
    },  new ExceptionOnClose(writer));
  }

  private class ExceptionOnClose implements InvocationHandler {

    private XMLStreamWriter target;

    private ExceptionOnClose(XMLStreamWriter target) {
      this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String m = method.getName();
      if ("close".equalsIgnoreCase(m)) {
        throw new XMLStreamException();
      }
      else {
        return method.invoke(target, args);
      }
    }
  }
}
