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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A trivial map-backed InvocationHandler that allows us to deserialize classes
 * as a specified interface.
 */
public class MapBackedBeanProxy<T> implements InvocationHandler {
	private final Class<T> iface;
	private final Map<String, Object> delegate;
	private final InvocationHandler finalHandler;

	public static final InvocationHandler UNSUPPORTED_OPERATION_HANDLER = new InvocationHandler() {
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			throw new UnsupportedOperationException();
		}
	};

	public Class<T> getInterface() {
		return iface;
	}

	public MapBackedBeanProxy(Class<T> iface) {
		this.iface = iface;
		this.delegate = new LinkedHashMap<String, Object>();
		this.finalHandler = UNSUPPORTED_OPERATION_HANDLER;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getName().equals("toString") && args == null) {
			return iface.getName() + "$" + delegate.toString();
		}

		if (method.getName().equals("equals") && args.length == 1) {
			if (!Proxy.isProxyClass(args[1].getClass())) {
				return false;
			}

			InvocationHandler invocationHandler = Proxy
					.getInvocationHandler(args[1]);

			if (!(invocationHandler instanceof MapBackedBeanProxy)) {
				return false;
			}

			return delegate
					.equals(((MapBackedBeanProxy) invocationHandler).delegate);
		}

		if (method.getName().equals("hashCode") && args == null) {
			return delegate.hashCode();
		}

		if (iface.getMethod(method.getName(), method.getParameterTypes()) == null) {
			return finalHandler.invoke(proxy, method, args);
		}

		char[] methodChars = method.getName().toCharArray();

		if (methodChars.length > 3 && method.getName().startsWith("get")
				&& args.length == 0) {
			methodChars[3] = Character.toLowerCase(methodChars[3]);
			String property = new String(methodChars, 3, methodChars.length - 3);
			return delegate.get(property);
		}

		if (methodChars.length > 3 && method.getName().startsWith("set")
				&& args.length == 1) {
			methodChars[3] = Character.toLowerCase(methodChars[3]);
			String property = new String(methodChars, 3, methodChars.length - 3);
			delegate.put(property, args[0]);
			return null;
		}

		return finalHandler.invoke(proxy, method, args);
	}

	public void set(String property, Object value) {
		this.delegate.put(property, value);
	}
}
