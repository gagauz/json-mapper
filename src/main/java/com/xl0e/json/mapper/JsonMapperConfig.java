/*
 *  Copyright 2013 Michael Gagauz
 *
 *  This file is part of JsonMapperImpl.
 *
 *  JsonMapperImpl is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JsonMapperImpl is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JsonMapperImpl.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xl0e.json.mapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sun.org.apache.bcel.internal.generic.Type;

public class JsonMapperConfig {

    protected final Map<String, Collection<MethodAlias>> methodsToClass = new HashMap<>();
    private final Map<String, String> configMap = new HashMap<>();

    public static JsonMapperConfig init() {
        return new DefaultJsonConfig();
    }

    public static JsonMapperConfig init(InputStream is) throws Exception {
        JsonMapperConfig instance = new JsonMapperConfig();
        instance.parseStream(new InputStreamReader(is));
        return instance;
    }

    public static JsonMapperConfig init(String config) throws Exception {
        JsonMapperConfig instance = new JsonMapperConfig();
        instance.parseStream(new StringReader(config));
        return instance;
    }

    public Collection<MethodAlias> getMethodsForClass(Class<?> clazz) {
        String clsName = clazz.getName();
        int pos = clsName.indexOf("_$$_javassist");
        if (pos > 0) {
            clsName = clsName.substring(0, pos);
        }
        Collection<MethodAlias> result = methodsToClass.get(clsName);
        if (null == result) {
            String config = configMap.get(clsName);
            result = parseClass(clazz, clsName, config);
        }
        return result;
    }

    private static class DefaultJsonConfig extends JsonMapperConfig {
        @Override
        public Collection<MethodAlias> getMethodsForClass(Class<?> clazz) {
            Collection<MethodAlias> result = methodsToClass.get(clazz.getName());
            if (null == result) {

                result = new HashSet<>();

                for (Method method : ReflectionUtils.getMethods(clazz)) {
                    String name = method.getName();

                    if (!method.getReturnType().equals(Type.VOID) && method.getParameterTypes().length == 0) {
                        if (name.startsWith("get")) {
                            name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                        }
                        result.add(new MethodAlias(method, name));
                    }
                }

                methodsToClass.put(clazz.getName(), result);
            }
            return result;
        }
    }

    private Set<MethodAlias> parseClass(Class<?> clazz, String clsName, String string) {
        try {
            System.out.println("Configure " + clazz);
            Set<MethodAlias> methodsToPut = new LinkedHashSet<>();

            final List<String> configMethods = Arrays.asList(string.split("\\s*,\\s*")).stream().map(String::trim).collect(Collectors.toList());

            for (Method method : ReflectionUtils.getMethods(clazz)) {
                String name = method.getName();
                if (name.startsWith("get")) {
                    name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                } else if (name.startsWith("is")
                        && (method.getReturnType().equals(Boolean.class) || method.getReturnType().equals(boolean.class))) {
                    name = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                }
                for (final String configMethod : configMethods) {
                    String[] altNames = configMethod.split(" as ");
                    for (int i = 0; i < altNames.length; i++) {
                        altNames[i] = altNames[i].trim();
                    }

                    if (altNames[0] != "" && name.equals(altNames[0])) {
                        methodsToPut.add(new MethodAlias(method, altNames));
                        configMethods.remove(configMethod);
                        break;
                    }
                }
            }

            methodsToClass.put(clsName, methodsToPut);
            if (!configMethods.isEmpty()) {
                String error = "Config for class " + clazz + " contains undeclared methods : ";
                for (String name : configMethods) {
                    error += name + ", ";
                }
                throw new IllegalArgumentException(error);
            }
            return methodsToPut;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void parseStream(Reader reader) throws Exception {
        Parser parser = new Parser(reader);
        final String[] name = { "" };
        parser
                .onDelimiter('{', s -> name[0] = s.trim())
                .onDelimiter('}', s -> configMap.put(name[0], s.trim()))
                .parse();
    }

}
