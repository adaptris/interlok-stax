package com.adaptris.stax;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

public abstract class StaxUtils {

  public static void closeQuietly(XMLStreamWriter w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception ignore) {

    }
  }

  public static void closeQuietly(XMLEventWriter w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception e) {

    }
  }


  public static void closeQuietly(XMLEventReader r) {
    try {
      if (r != null) {
        r.close();
      }
    } catch (Exception ignored) {

    }
  }
}
