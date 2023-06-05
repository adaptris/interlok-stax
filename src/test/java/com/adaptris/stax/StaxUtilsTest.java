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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;

// This is all just a bit of fakery to get 100% (ha ha).
public class StaxUtilsTest extends StaxUtils {

  @Test
  public void testCloseEventReader() {
    closeQuietly(proxyEventReader(false));
    closeQuietly(proxyEventReader(true));
    closeQuietly((XMLEventReader) null);
  }

  @Test
  public void testCloseEventWriter() {
    closeQuietly(proxyEventWriter(false));
    closeQuietly(proxyEventWriter(true));
    closeQuietly((XMLEventWriter) null);
  }

  @Test
  public void testCloseStreamWriter() {
    closeQuietly(proxyStreamWriter(false));
    closeQuietly(proxyStreamWriter(true));
    closeQuietly((XMLStreamWriter) null);
  }

  private XMLEventReader proxyEventReader(boolean error) {
    InvocationHandler handler = new NoOpHandler();
    if (error) {
      handler = new ExceptionOnClose();
    }
    return (XMLEventReader) Proxy.newProxyInstance(XMLEventReader.class.getClassLoader(), new Class[] { XMLEventReader.class }, handler);
  }

  private XMLStreamWriter proxyStreamWriter(boolean error) {
    InvocationHandler handler = new NoOpHandler();
    if (error) {
      handler = new ExceptionOnClose();
    }
    return (XMLStreamWriter) Proxy.newProxyInstance(XMLStreamWriter.class.getClassLoader(), new Class[] { XMLStreamWriter.class }, handler);
  }

  private XMLEventWriter proxyEventWriter(boolean error) {
    InvocationHandler handler = new NoOpHandler();
    if (error) {
      handler = new ExceptionOnClose();
    }
    return (XMLEventWriter) Proxy.newProxyInstance(XMLEventWriter.class.getClassLoader(), new Class[] { XMLEventWriter.class }, handler);
  }

  private class NoOpHandler implements InvocationHandler {

    private NoOpHandler() {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return null;
    }
  }

  private class ExceptionOnClose implements InvocationHandler {

    private ExceptionOnClose() {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String m = method.getName();
      if ("close".equalsIgnoreCase(m)) {
        throw new XMLStreamException();
      }
      return null;
    }
  }

}
