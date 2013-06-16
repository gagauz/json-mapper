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
import java.util.*;
import java.util.Map.Entry;

import static net.gagauz.jsonmapper.Reflector.*;

public class JsonMapper {

    private static final char SQT = '\'';

    private final Set<String> errorMethodCache = new HashSet<String>(50);

    private final JsonMapperConfig config;

    private JsonMapper(JsonMapperConfig config) {
        this.config = config;
    }

    public static JsonMapper instanse() {
        return new JsonMapper(JsonMapperConfig.init());
    }

    public static JsonMapper instanse(InputStream stream) throws Exception {
        return new JsonMapper(JsonMapperConfig.init(stream));
    }

    public static JsonMapper instanse(JsonMapperConfig config) throws Exception {
        return new JsonMapper(config);
    }

    public String map(Object o) {
        long start = System.currentTimeMillis();
        JsonIndentWriter sb = new JsonIndentWriter();
        try {
            map(o, sb, ">");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("JSON parsing took " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("JSON data size " + sb.toString().length() + " b");
        return sb.toString();
    }

    private void map(final Object o, final JsonIndentWriter sb, final String parent) throws Exception {

        if (null == o) {
            sb.write("null");
        } else if (isPrimitive(o.getClass())) {
            mapPrimitive(o, sb);
        } else if (isString(o.getClass())) {
            mapString(o, sb);
        } else if (isIterator(o.getClass())) {
            mapIterator((Iterator<?>) o, sb, parent);
        } else if (isMap(o.getClass())) {
            mapMap((Map<?, ?>) o, sb, parent);
        } else if (isIterable(o.getClass())) {
            mapIterator(((Iterable<?>) o).iterator(), sb, parent);
        } else if (isArray(o.getClass())) {
            mapArray((Object[]) o, sb, parent);
        } else if (o.getClass().isMemberClass()) {
            return;
        } else {
            if (!parent.contains(o.getClass().getName() + '>')) {

                String hierarchy = "";//parent + o.getClass().getName() + '>';

                Collection<MethodAlias> methods = config.getMethodsForClass(o.getClass());

                if (!methods.isEmpty()) {
                    sb.write("{").start();
                    sb.nl();
                    Iterator<MethodAlias> i = methods.iterator();
                    mapMethod(o, i.next(), sb, hierarchy);
                    while (i.hasNext()) {
                        mapMethod(o, i.next(), sb.write(',').nl(), hierarchy);
                    }
                    sb.finish().write("}");
                }

            } else {
                sb.write("{}");
            }
        }
    }

    private void mapMethod(Object instance, MethodAlias m, JsonIndentWriter sb, String hierarchy) throws Exception {

        String name = m.getAliasName();
        if (errorMethodCache.contains(instance.getClass().getName() + '.' + name)) {
            return;
        }

        try {
            sb.write(name).write(":");
            map(m.getMethod().invoke(instance), sb, hierarchy);
        } catch (Exception e) {
            errorMethodCache.add(instance.getClass().getName() + '.' + name);
            System.out.println(m);
            e.printStackTrace();
        }
    }

    private void mapString(Object value, JsonIndentWriter sb) {
        sb.write(SQT).write(String.valueOf(value).replaceAll("'", "\'")).write(SQT);
    }

    private void mapPrimitive(Object value, JsonIndentWriter sb) {
        sb.write(String.valueOf(value));
    }

    private void mapArray(Object[] value, JsonIndentWriter sb, String parent) throws Exception {
        if (value.length == 0) {
            sb.write("[]");
            return;
        }
        sb.write("[").start().nl();
        map(value[0], sb, parent);
        for (int i = 1; i < value.length; i++) {
            map(value[i], sb.write(','), parent);
        }
        sb.finish().write("]");

    }

    private void mapIterator(Iterator<?> value, JsonIndentWriter sb, String parent) throws Exception {
        if (!value.hasNext()) {
            sb.write("[]");
            return;
        }
        sb.write("[").start().nl();
        map(value.next(), sb, parent);
        while (value.hasNext()) {
            map(value.next(), sb.write(','), parent);
        }
        sb.finish().write("]");
    }

    private void mapMap(Map value, JsonIndentWriter sb, String parent) throws Exception {
        if (value.isEmpty()) {
            sb.write("{}");
            return;
        }
        sb.write("{").start().nl();
        Iterator<Entry> i = value.entrySet().iterator();
        Entry e = i.next();
        sb.write(e.getKey()).write(':');
        map(e.getValue(), sb, parent);
        while (i.hasNext()) {
            sb.write(',').write(e.getKey()).write(':');
            map(e.getValue(), sb, parent);
        }
        sb.finish().write("}");
    }
}
