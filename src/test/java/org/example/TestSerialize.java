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
package org.example;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.codehaus.jackson.map.ObjectMapper;

import com.g414.jackson.proxy.ProxySerializerFactory;

/**
 * Exercise the serializer...
 */
public class TestSerialize extends TestCase {
    public void testExample() throws Exception {
        ExampleImpl impl = new ExampleImpl();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerFactory(new ProxySerializerFactory());

        // Look, I'm an ExampleImpl...
        StringWriter w1 = new StringWriter();
        mapper.writeValue(w1, impl);
        System.out.println(w1.toString());

        // Look, I'm an Example1...
        StringWriter w2 = new StringWriter();
        mapper.writeValue(w2, impl.asExample1());
        System.out.println(w2.toString());

        // Look, I'm an Example2...
        StringWriter w3 = new StringWriter();
        mapper.writeValue(w3, impl.asExample2());
        System.out.println(w3.toString());
    }
}
