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

import java.io.File;
import java.io.IOException;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.util.IdGenerator;

public class FakeFileBackedMessage extends DefaultAdaptrisMessageImp implements FileBackedMessage {

  protected FakeFileBackedMessage(IdGenerator guid, AdaptrisMessageFactory fac) throws RuntimeException {
    super(guid, fac);
  }

  @Override
  public void initialiseFrom(File sourceObject) throws IOException, RuntimeException {
    throw new UnsupportedOperationException();
  }

  @Override
  public File currentSource() {
    throw new UnsupportedOperationException();
  }

}
