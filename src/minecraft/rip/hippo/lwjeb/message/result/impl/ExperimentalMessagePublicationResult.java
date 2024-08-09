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

package rip.hippo.lwjeb.message.result.impl;

import rip.hippo.lwjeb.bus.publish.AsynchronousPublishMessageBus;
import rip.hippo.lwjeb.message.handler.MessageHandler;
import rip.hippo.lwjeb.message.result.MessagePublicationResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Hippo
 * @version 5.0.0, 11/2/19
 * @since 5.0.0
 * <p>
 * This is an experiential result, these don't allow filters or priorities, this should only be used if publication/invocation speed is your only priority.
 * </p>
 */
public final class ExperimentalMessagePublicationResult<T> implements MessagePublicationResult<T> {

  /**
   * The bus.
   */
  private final AsynchronousPublishMessageBus<T> publishBus;

  /**
   * The list of handlers.
   */
  private final List<MessageHandler<T>> handlers;

  /**
   * The topic.
   */
  private final T topic;

  /**
   * Creates a new {@link ExperimentalMessagePublicationResult} with the desired bus, handlers, and topic.
   *
   * @param publishBus The bus.
   * @param handlers   The handlers.
   * @param topic      The topic.
   */
  public ExperimentalMessagePublicationResult(AsynchronousPublishMessageBus<T> publishBus, List<MessageHandler<T>> handlers, T topic) {
    this.publishBus = publishBus;
    this.handlers = handlers;
    this.topic = topic;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void async() {
    publishBus.addMessage(this);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void async(long timeout, TimeUnit timeUnit) {
    publishBus.addMessage(this, timeout, timeUnit);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void dispatch() {
    for (int i = 0, j = handlers.size(); i < j; i++) {
      handlers.get(i).handle(topic);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<MessageHandler<T>> getHandlers() {
    return handlers;
  }
}
