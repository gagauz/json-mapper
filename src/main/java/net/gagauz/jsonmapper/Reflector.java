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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.reflect.Modifier.*;

/**
 * Tools for class reflection 
 *
 */
public class Reflector {

    private static final int EXCLUDED = ABSTRACT | PRIVATE | PROTECTED | STATIC;

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || Long.class.equals(clazz) || Byte.class.equals(clazz) || Float.class.equals(clazz) || Integer.class.equals(clazz)
                || Double.class.equals(clazz)
                || Boolean.class.equals(clazz)
                || BigDecimal.class.equals(clazz);
    }

    public static boolean isString(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    public static boolean isIterable(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    public static boolean isMap(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    public static boolean isIterator(Class<?> clazz) {
        return Iterator.class.isAssignableFrom(clazz);
    }

    public static Collection<Method> getMethods(Class<?> clazz) {
        return fetchMethods(clazz, null, 0);
    }

    private static Collection<Method> fetchMethods(Class<?> clazz, Map<String, Method> result, int level) {
        if (null == result) {
            result = new HashMap<String, Method>();
        }
        for (Method m : clazz.getDeclaredMethods()) {
            String name = m.getName();
            if (!result.containsKey(name) && !name.equals("hashCode") && !name.equals("getClass") && !name.equals("toString")
                    && m.getParameterTypes().length == 0
                    && !m.isSynthetic() && !m.getReturnType().equals(Void.TYPE) && (m.getModifiers() & EXCLUDED) == 0) {
                result.put(name, m);
            }
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz.getName().startsWith("ru.kupi") && level < 5) {
            fetchMethods(superClazz, result, level + 1);
        }
        return result.values();
    }

}
