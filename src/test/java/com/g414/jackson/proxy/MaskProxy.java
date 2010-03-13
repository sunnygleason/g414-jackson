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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An InvocationHandler that allows us to serialize classes as a specified
 * interface which "masks" the methods on the target class.
 */
public class MaskProxy<T> implements InvocationHandler {
    private final Class<T> iface;
    private final Object delegate;
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

    public MaskProxy(Class<T> iface, Object delegate) {
        this.iface = iface;
        this.delegate = delegate;
        this.finalHandler = UNSUPPORTED_OPERATION_HANDLER;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (iface.getMethod(method.getName(), method.getParameterTypes()) == null) {
            return finalHandler.invoke(proxy, method, args);
        }

        try {
            return delegate.getClass().getMethod(method.getName(),
                    method.getParameterTypes()).invoke(delegate, args);
        } catch (NoSuchMethodException nsme) {
        } catch (IllegalArgumentException iae) {
        } catch (IllegalAccessException iae2) {
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
        // allow SecurityException to go out unchecked

        return finalHandler.invoke(proxy, method, args);
    }
}
