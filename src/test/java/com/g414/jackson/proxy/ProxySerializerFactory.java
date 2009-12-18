/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.g414.jackson.proxy;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;

/**
 * A very basic Jackson serializer factory that extends the BeanSerializer
 * factory to allow serialization of proxy classes. Note: if there are Proxy
 * instances in use which aren't MaskProxy instances, this will break.
 */
public class ProxySerializerFactory extends BeanSerializerFactory {
	public ProxySerializerFactory() {
		super();
	}

	@Override
	public <T> JsonSerializer<T> createSerializer(Class<T> type,
			SerializationConfig config) {
		if (Proxy.isProxyClass(type)) {
			return (JsonSerializer<T>) this.createSerializerImpl(type, config);
		}

		return super.createSerializer(type, config);
	}

	private <T> JsonSerializer<T> createSerializerImpl(final Class<T> type,
			final SerializationConfig config) {
		return new JsonSerializer<T>() {
			@Override
			public void serialize(Object target, JsonGenerator jsonGen,
					SerializerProvider provider) throws IOException,
					JsonProcessingException {
				InvocationHandler h = Proxy.getInvocationHandler(target);

				// Note: expect this to break if it's not a MaskProxy!
				// TODO: make safer
				MaskProxy<T> p = (MaskProxy<T>) h;

				JsonSerializer<T> ser = ProxySerializerFactory.super
						.createSerializer(p.getInterface(), config);
				ser.serialize((T) target, jsonGen, provider);
			}
		};
	}

}
