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

import static net.gagauz.jsonmapper.Reflector.isArray;
import static net.gagauz.jsonmapper.Reflector.isIterable;
import static net.gagauz.jsonmapper.Reflector.isIterator;
import static net.gagauz.jsonmapper.Reflector.isMap;
import static net.gagauz.jsonmapper.Reflector.isPrimitive;
import static net.gagauz.jsonmapper.Reflector.isString;

import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JsonMapper {

    private boolean isCycleRefference(Object object, String parent) {
        String hash = object.getClass().getName() + '#' + object.hashCode() + '>';
        return parent.contains(hash);
    }

    private static final char SQT = '"';

    private final Set<String> errorMethodCache = new HashSet<String>(50);

    private final JsonMapperConfig config;

    private JsonMapper(JsonMapperConfig config) {
        this.config = config;
    }

    public static JsonMapper instanse() {
        return new JsonMapper(JsonMapperConfig.init());
    }

    public static JsonMapper instanse(JsonMapperConfig config) throws Exception {
        return new JsonMapper(config);
    }

    private JsonWriter sb;

    public void map(Object o, Writer writer) {
        long start = System.currentTimeMillis();
        sb = new JsonWriter(writer);
        try {
            map(o, ">");
            writer.flush();// Important
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("JSON parsing took " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("JSON data size " + sb.size() + " b");
    }

    private void map(final Object o, final String parent) throws Exception {
        if (null == o) {
            sb.write("null");
        } else {
            Class<?> cls = o.getClass();
            if (isPrimitive(cls)) {
                mapPrimitive(o);
            } else if (isString(cls)) {
                mapString(o);
            } else if (isIterator(cls)) {
                mapIterator((Iterator<?>) o, parent);
            } else if (isMap(cls)) {
                mapMap((Map<?, ?>) o, parent);
            } else if (isIterable(cls)) {
                mapIterator(((Iterable<?>) o).iterator(), parent);
            } else if (isArray(cls)) {
                mapArray((Object[]) o, parent);
            } else if (cls.isMemberClass()) {
                sb.write("{").start().nl();
                sb.write("'member class " + cls.getName() + "'").nl().finish().write('}');
            } else {
                if ((!isCycleRefference(o, parent))) {
                    mapObject(o, parent);
                } else {
                    sb.write("\"cycle refference\"");
                }
            }
        }
    }

    private void mapObject(Object instance, String parent) {
        String hierarchy = parent + instance.getClass().getName() + '#' + instance.hashCode() + '>';

        Collection<MethodAlias> methods = config.getMethodsForClass(instance.getClass());

        if (methods.size() == 1) {
            mapMethod(instance, methods.iterator().next(), hierarchy, true);
        } else if (!methods.isEmpty()) {
            sb.write("{").start();
            sb.nl();
            Iterator<MethodAlias> i = methods.iterator();
            mapMethod(instance, i.next(), hierarchy, false);
            while (i.hasNext()) {
                sb.write(',').nl();
                mapMethod(instance, i.next(), hierarchy, false);
            }
            sb.finish().write("}");

        } else {
            sb.write("{}");
        }

    }

    private void mapMethod(Object instance, MethodAlias m, String hierarchy, boolean collapse) {

        String name = m.getAliasName();
        if (errorMethodCache.contains(instance.getClass().getName() + '.' + name)) {
            return;
        }
        if (!collapse)
            sb.write(name).write(":");
        try {
            map(m.getMethod().invoke(instance), hierarchy);
        } catch (Exception e) {
            errorMethodCache.add(instance.getClass().getName() + '.' + name);
            throw new RuntimeException(e);
        }
    }

    private void mapString(Object value) {
        sb.write(SQT).write(String.valueOf(value).replace("\"", "\\\"")).write(SQT);
    }

    private void mapPrimitive(Object value) {
        sb.write(String.valueOf(value));
    }

    private void mapArray(Object[] value, String parent) throws Exception {
        if (value.length == 0) {
            sb.write("[]");
            return;
        }
        sb.write("[").start().nl();
        map(value[0], parent);
        for (int i = 1; i < value.length; i++) {
            sb.write(',');
            map(value[i], parent);
        }
        sb.finish().write("]");

    }

    private void mapIterator(Iterator<?> value, String parent) throws Exception {
        if (!value.hasNext()) {
            sb.write("[]");
            return;
        }
        sb.write("[").start().nl();
        map(value.next(), parent);
        while (value.hasNext()) {
            sb.write(',');
            map(value.next(), parent);
        }
        sb.finish().write("]");
    }

    private void mapMap(Map value, String parent) throws Exception {
        if (value.isEmpty()) {
            sb.write("{}");
            return;
        }
        sb.write("{").start().nl();
        Iterator<Entry> i = value.entrySet().iterator();
        Entry e = i.next();
        map(e.getKey(), parent);
        sb.write(':');
        map(e.getValue(), parent);
        while (i.hasNext()) {
            sb.write(',');
            map(e.getKey(), parent);
            sb.write(':');
            map(e.getValue(), parent);
        }
        sb.finish().write("}");
    }
}
