/*
 * Copyright 2020 Hippo
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
 *
 */

package rip.hippo.lwjeb.configuration.exception.handle.impl;

import rip.hippo.lwjeb.configuration.exception.handle.ExceptionHandler;

/**
 * @author Hippo
 * @version 5.0.0, 10/30/19
 * @since 5.0.0
 * <p>
 * The simplified handler just prints a small text where the error was thrown.
 * </p>
 */
public enum SimplifiedExceptionHandler implements ExceptionHandler {
  INSTANCE;

  /**
   * @inheritDoc
   */
  @Override
  public void handleException(Throwable t) {
    System.err.println(t.toString() + " -> " + t.getStackTrace()[0] + (t.getMessage() == null ? "" : (" -> " + t.getMessage())));
  }
}
