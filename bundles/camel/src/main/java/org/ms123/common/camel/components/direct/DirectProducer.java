/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.camel.components.direct;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultAsyncProducer;
import java.util.Collection;

/**
 * The direct producer.
 *
 * @version 
 */
public class DirectProducer extends DefaultAsyncProducer {

	private final DirectEndpoint endpoint;

	public DirectProducer(DirectEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	public void process(Exchange exchange) throws Exception {
		Collection<DirectConsumer> consumers = endpoint.getConsumers();
		if (consumers.size() == 0) {
			throw new DirectConsumerNotAvailableException("No consumers available on endpoint: " + endpoint, exchange);
		} else {
			for (DirectConsumer consumer : consumers) {
				consumer.getProcessor().process(exchange);
			}
		}
	}

	public boolean process(Exchange exchange, AsyncCallback callback) {
		Collection<DirectConsumer> consumers = endpoint.getConsumers();
		if (consumers.size() == 0) {
			// indicate its done synchronously
			exchange.setException(new DirectConsumerNotAvailableException("No consumers available on endpoint: " + endpoint, exchange));
			callback.done(true);
			return true;
		} else {
			boolean answer = false;
			for (DirectConsumer consumer : consumers) {
				answer = consumer.getAsyncProcessor().process(exchange, callback);
			}
			return answer;
		}
	}
}
