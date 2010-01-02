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
package org.codehaus.jackson.map.deser;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.map.util.ClassUtil;
import org.codehaus.jackson.type.JavaType;

import com.g414.jackson.proxy.MapBackedBeanProxy;
import com.g414.jackson.proxy.MapBackedBeanProxyFactory;

/**
 * A very basic Jackson deserializer factory that extends the BeanSerializer
 * factory to allow deserialization using interfaces.
 */
public class ProxyDeserializerFactory extends BeanDeserializerFactory {
	protected static final Set<Class> javaClasses;
	static {
		Set<Class> newJavaClasses = new HashSet<Class>();
		newJavaClasses.add(String.class);
		newJavaClasses.add(Long.class);
		newJavaClasses.add(Integer.class);
		javaClasses = Collections.unmodifiableSet(newJavaClasses);
	}

	public ProxyDeserializerFactory() {
		super();
	}

	@Override
	public JsonDeserializer<Object> createBeanDeserializer(
			DeserializationConfig config, JavaType type, DeserializerProvider p)
			throws JsonMappingException {
		if (type.isInterface()) {
			return new BeanDeserializerProxyImpl(type);
		}

		return super.createBeanDeserializer(config, type, p);
	}

	protected boolean isJavaType(JavaType type) {
		return javaClasses.contains(type.getRawClass());
	}

	protected static String normalizeName(String methodName) {
		char[] name = methodName.substring(3).toCharArray();
		name[0] = Character.toLowerCase(name[0]);
		final String propName = new String(name);
		return propName;
	}

	protected static class BeanDeserializerProxyImpl extends BeanDeserializer {
		public BeanDeserializerProxyImpl(final JavaType type) {
			super(type);

			for (Method method : type.getRawClass().getMethods()) {
				if (method.getName().length() > 3
						&& !method.getName().equals("getClass")
						&& method.getName().startsWith("get")
						&& method.getReturnType() != null) {
					final String propName = normalizeName(method.getName());
					final Class propClass = method.getReturnType();

					final JavaType propType = TypeFactory.fromClass(propClass);

					this.addProperty(new PlaceHolderSettableBeanProperty(
							propName, propType));
				}
			}
		}

		@Override
		public Object deserializeFromObject(JsonParser jp,
				DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			Object bean;
			MapBackedBeanProxy beanImpl;
			try {
				bean = MapBackedBeanProxyFactory.newProxyInstance(_beanType
						.getRawClass());
				beanImpl = (MapBackedBeanProxy) Proxy
						.getInvocationHandler(bean);
			} catch (Exception e) {
				ClassUtil.unwrapAndThrowAsIAE(e);
				return null; // never gets here
			}

			while (jp.nextToken() != JsonToken.END_OBJECT) { // otherwise field
				// name
				String propName = jp.getCurrentName();
				SettableBeanProperty prop = this._props.get(propName);

				if (prop != null) { // normal case
					Object value = prop.deserialize(jp, ctxt);
					beanImpl.set(propName, value);
					continue;
				}
			}

			return bean;
		}
	}

	protected static class PlaceHolderSettableBeanProperty extends
			SettableBeanProperty {
		public PlaceHolderSettableBeanProperty(String propName, JavaType type) {
			super(propName, type);
		}

		@Override
		public void deserializeAndSet(JsonParser arg0,
				DeserializationContext arg1, Object arg2) throws IOException,
				JsonProcessingException {
		}

		@Override
		public void set(Object arg0, Object arg1) throws IOException {
		}

		@Override
		protected Class<?> getDeclaringClass() {
			return this.getType().getRawClass();
		}
	}
}
