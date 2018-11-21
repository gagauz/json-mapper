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

import static com.xl0e.json.mapper.ReflectionUtils.isArray;
import static com.xl0e.json.mapper.ReflectionUtils.isIterable;
import static com.xl0e.json.mapper.ReflectionUtils.isIterator;
import static com.xl0e.json.mapper.ReflectionUtils.isMap;
import static com.xl0e.json.mapper.ReflectionUtils.isPrimitive;
import static com.xl0e.json.mapper.ReflectionUtils.isString;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.xl0e.json.writer.JsonWriter;

public class JsonMapperImpl implements JsonMapper {

    private boolean isCycleRefference(Object object, String parent) {
        String hash = object.getClass().getName() + '#' + object.hashCode() + '>';
        return parent.contains(hash);
    }

    private static final char SQT = '"';

    private final Set<String> errorMethodCache = new HashSet<>(50);

    private final JsonMapperConfig config;

    private JsonMapperImpl(JsonMapperConfig config) {
        this.config = config;
    }

    public static JsonMapper instanse() {
        return new JsonMapperImpl(JsonMapperConfig.init());
    }

    public static JsonMapper instanse(JsonMapperConfig config) throws Exception {
        return new JsonMapperImpl(config);
    }

    public static JsonMapper instanse(File config) throws Exception {
        return new JsonMapperImpl(JsonMapperConfig.init(new FileInputStream(config)));
    }

    public static JsonMapper instanse(InputStream config) throws Exception {
        return new JsonMapperImpl(JsonMapperConfig.init(config));
    }

    private JsonWriter writer;
    private Deque<Class<?>> hierarchy = new LinkedList<>();

    @Override
    public void map(Object o, JsonWriter writer) {
        this.writer = writer;
        try {
            map(o);
            writer.flush();// Important
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void map(final Object o) throws Exception {
        if (null == o) {
            writer.write("");
        } else {
            Class<?> cls = o.getClass();
            if (isPrimitive(cls)) {
                mapPrimitive(o);
            } else if (isString(cls)) {
                mapString((String) o);
            } else if (isIterator(cls)) {
                mapIterator((Iterator<?>) o);
            } else if (isMap(cls)) {
                mapMap((Map<?, ?>) o);
            } else if (isIterable(cls)) {
                mapIterator(((Iterable<?>) o).iterator());
            } else if (isArray(cls)) {
                mapArray((Object[]) o);
            } else if (cls.isMemberClass()) {
                writer.openObj();
                writer.write("'member class " + cls.getName() + "'");
                writer.closeObj();
            } else {
                if (!hierarchy.contains(cls)) {
                    hierarchy.addLast(cls);
                    mapObject(o);
                    hierarchy.removeLast();
                } else {
                    writer.write("\"cycle refference\"");
                }
            }
        }
    }

    private void mapObject(Object instance) {

        Collection<MethodAlias> methods = config.getMethodsForClass(instance.getClass());

        writer.openObj();
        for (MethodAlias m : methods) {
            mapMethod(instance, m);
            writer.comma();
        }

        writer.closeObj();
    }

    private void mapMethod(Object instance, MethodAlias m) {

        String name = m.getAliasName();
        if (errorMethodCache.contains(instance.getClass().getName() + '.' + name)) {
            return;
        }
        writer.writeName(name);
        try {
            map(m.getMethod().invoke(instance));
        } catch (Exception e) {
            errorMethodCache.add(instance.getClass().getName() + '.' + name);
            writer.write("#ERROR");
            e.printStackTrace();
        }
    }

    private void mapString(String value) {
        writer.write(SQT + value.replace("\"", "\\\"") + SQT);
    }

    private void mapPrimitive(Object value) {
        writer.write(String.valueOf(value));
    }

    private void mapArray(Object[] value) throws Exception {
        writer.openArr();
        for (int i = 0; i < value.length; i++) {
            map(value[i]);
            writer.comma();
        }
        writer.closeArr();

    }

    private void mapIterator(Iterator<?> value) throws Exception {
        writer.openArr();
        while (value.hasNext()) {
            Object o = value.next();
            map(o);
            writer.comma();
        }
        writer.closeArr();
    }

    private void mapMap(Map<?, ?> value) throws Exception {
        writer.openObj();
        for (Entry<?, ?> e : value.entrySet()) {
            writer.writeName(e.getKey());
            map(e.getValue());
            writer.comma();
        }
        writer.closeObj();
    }
}
