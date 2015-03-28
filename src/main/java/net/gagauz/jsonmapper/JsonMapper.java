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

    public Set<String> REGISTRY = new HashSet<String>();

    private boolean register(Object object) {
        if (null == object) {
            return true;
        }
        Class<?> cls = object.getClass();
        if (isString(cls) || isPrimitive(cls)) {
            return true;
        }
        String hash = cls.getName() + '#' + object.hashCode();
        return REGISTRY.add(hash);
    }

    private static final String SQT = "\"";
    private static final String ESQT = "\\\"";

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

    private JsonIndentWriter sb = new JsonIndentWriter();

    public String map(Object o) {
        long start = System.currentTimeMillis();

        try {
            map(o, ">");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("JSON parsing took " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("JSON data size " + sb.toString().length() + " b");
        return sb.toString();
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
            } else if (register(o)) {
                //if (!parent.contains(cls.getName() + '>')) {

                String hierarchy = "";//parent + cls.getName() + '>';

                Collection<MethodAlias> methods = config.getMethodsForClass(cls);

                if (methods.size() == 1) {
                    mapMethod(o, methods.iterator().next(), hierarchy, true);
                } else if (!methods.isEmpty()) {
                    sb.write("{").start();
                    sb.nl();
                    Iterator<MethodAlias> i = methods.iterator();
                    mapMethod(o, i.next(), hierarchy, false);
                    while (i.hasNext()) {
                        sb.write(',').nl();
                        mapMethod(o, i.next(), hierarchy, false);
                    }
                    sb.finish().write("}");

                } else {
                    sb.write("{}");
                }
            } else {
                sb.write("'ref " + o.hashCode() + "'");
            }
        }
    }

    private void mapMethod(Object instance, MethodAlias m, String hierarchy, boolean collapse) {

        String name = m.getAliasName();
        if (errorMethodCache.contains(instance.getClass().getName() + '.' + name)) {
            return;
        }

        try {
            if (!collapse)
                sb.write(name).write(":");
            map(m.getMethod().invoke(instance), hierarchy);
        } catch (Exception e) {
            errorMethodCache.add(instance.getClass().getName() + '.' + name);
            System.out.println(m);
            e.printStackTrace();
        }
    }

    private void mapString(Object value) {
        //.replaceAll(SQT, ESQT)
        sb.write(SQT).write(String.valueOf(value)).write(SQT);
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
        sb.write(e.getKey()).write(':');
        map(e.getValue(), parent);
        while (i.hasNext()) {
            sb.write(',').write(e.getKey()).write(':');
            map(e.getValue(), parent);
        }
        sb.finish().write("}");
    }
}
