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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.bcel.internal.generic.Type;

public class JsonMapperConfig {

    protected final Map<String, Collection<MethodAlias>> methodsToClass = new HashMap<String, Collection<MethodAlias>>();

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
        String clsName = clazz.getName();
        int pos = clsName.indexOf("_$$_javassist");
        if (pos > 0) {
            clsName = clsName.substring(0, pos);
        }
        Collection<MethodAlias> result = methodsToClass.get(clsName);
        if (null == result) {
            String config = configMap.get(clsName);
            result = parseClass(clazz, clsName, String.valueOf(config));
        }
        return result;
    }

    private static class DefaultJsonConfig extends JsonMapperConfig {
        @Override
        public Collection<MethodAlias> getMethodsForClass(Class<?> clazz) {
            Collection<MethodAlias> result = methodsToClass.get(clazz.getName());
            if (null == result) {

                result = new HashSet<MethodAlias>();

                for (Method method : Reflector.getMethods(clazz)) {
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
            Set<MethodAlias> methodsToPut = new LinkedHashSet<MethodAlias>();

            final List<String> configMethods = new ArrayList<String>(Arrays.asList(string.split("\\s*,\\s*")));

            for (Method method : Reflector.getMethods(clazz)) {
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
            throw new RuntimeException(e);
        }
    }

    private final Map<String, String> configMap = new HashMap<>();

    private void parseString(String config) throws Exception {
        Matcher match = pattern.matcher(config);
        while (match.find()) {
            configMap.put(match.group(1), match.group(2));
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
