/*
 *  Copyright 2013 Michael Gagauz
 *  
 *  This file is part of JsonMapper.
 *
 *  JsonMapper is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JsonMapper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JsonMapper.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.gagauz.jsonmapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonMapperConfig {

    protected final Map<Class<?>, Collection<MethodAlias>> methodsToClass = new HashMap<Class<?>, Collection<MethodAlias>>();

    private final static Pattern pattern = Pattern.compile("(?i)(?:([a-z0-9_\\.]++)\\s*\\{([^}]++)\\})");

    public static JsonMapperConfig init() {
        return new DefaultJsonConfig();
    }

    public static JsonMapperConfig init(InputStream is) throws Exception {
        String config = readStream(is);
        return init(config);
    }

    public static JsonMapperConfig init(String config) throws Exception {
        JsonMapperConfig instance = new JsonMapperConfig();
        instance.parseString(config);
        return instance;
    }

    public Collection<MethodAlias> getMethodsForClass(Class<?> clazz) {
        Collection<MethodAlias> result = methodsToClass.get(clazz);
        if (null != result) {
            return result;
        }
        return Collections.<MethodAlias>emptyList();
    }

    private static class DefaultJsonConfig extends JsonMapperConfig {
        @Override
        public Collection<MethodAlias> getMethodsForClass(Class<?> clazz) {
            Collection<MethodAlias> result = methodsToClass.get(clazz);
            if (null == result) {

                result = new HashSet<MethodAlias>();

                for (Method method : Reflector.getMethods(clazz)) {
                    String name = method.getName();
                    if (name.startsWith("get")) {
                        name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    }

                    result.add(new MethodAlias(method, name));
                }

                methodsToClass.put(clazz, result);
            }
            return result;
        }
    }

    private void parseClass(String className, String string) throws Exception {
        Class<?> clazz = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(className.trim());
            System.out.println("Configure " + clazz);
            Set<MethodAlias> methodsToPut = new LinkedHashSet<MethodAlias>();

            final List<String> configMethods = new ArrayList<String>(Arrays.asList(string.split("\\s*,\\s*")));

            for (Method method : Reflector.getMethods(clazz)) {
                String name = method.getName();
                if (name.startsWith("get")) {
                    name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
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

            if (!methodsToPut.isEmpty()) {
                methodsToClass.put(clazz, methodsToPut);
            }
            if (!configMethods.isEmpty()) {
                String error = "Config for class " + className + " contains undeclared methods : ";
                for (String name : configMethods) {
                    error += name + ", ";
                }
                throw new IllegalArgumentException(error);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load class " + className);
        } catch (Exception e) {
            throw e;
        }
    }

    private void parseString(String config) throws Exception {
        Matcher match = pattern.matcher(config);
        while (match.find()) {
            parseClass(match.group(1), match.group(2));
        }
    }

    private static String readStream(final InputStream is) {
        final char[] buffer = new char[10];
        final StringBuilder out = new StringBuilder();
        try {
            final Reader in = new InputStreamReader(is, "UTF-8");
            try {
                int rsz = 0;
                while (rsz >= 0) {
                    out.append(buffer, 0, rsz);
                    rsz = in.read(buffer, 0, buffer.length);
                }
            } finally {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out.toString();
    }
}
