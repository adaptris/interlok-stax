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

package com.adaptris.stax.lms;

import org.apache.commons.io.IOUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.Args;


/**
 * Abstract implementation for STaX support services.
 *
 */
public abstract class StaxXmlOutput extends ServiceImp {

  // The object metadata key were we store the File + XMLEventWriter.
  static final String XML_OUTPUT_WRITER_KEY = StaxXmlOutput.class.getSimpleName();

  @Override
  public void prepare() throws CoreException {
    // Nothing to do
  }

  @Override
  protected void initService() throws CoreException {
    // Nothing to do
  }

  @Override
  protected void closeService() {
    // Nothing to do
  }

  protected FileBackedMessageFactory messageFactory(AdaptrisMessage msg) throws ServiceException {
    if (!FileBackedMessage.class.isAssignableFrom(msg.getClass())) {
      throw new ServiceException("Message must be an instanceof FileBackedMessage");
    }
    AdaptrisMessageFactory factory = msg.getFactory();
    if (!FileBackedMessageFactory.class.isAssignableFrom(factory.getClass())) {
      throw new ServiceException("MessageFactory associated with Message must be and instance of FileBackedMessageFactory");
    }
    return (FileBackedMessageFactory) factory;
  }

  protected StaxOutputWrapper unwrap(AdaptrisMessage msg) {
    return Args.notNull((StaxOutputWrapper) msg.getObjectHeaders().get(XML_OUTPUT_WRITER_KEY), "xmlEventWriter");
  }

  @SuppressWarnings("deprecation")
  protected void closeQuietly(StaxOutputWrapper f) {
    IOUtils.closeQuietly(f);
  }

}
