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

/**
 * Wrapper around {@link Method} containing alias method name 
 *
 */
public class MethodAlias {

    private final String aliasName;
    private final Method method;

    public MethodAlias(Method m, String... altNames) {
        System.out.print("   " + altNames[0]);
        if (altNames.length > 1) {
            aliasName = altNames[1];
            if (null == aliasName || "".equals(aliasName)) {
                throw new IllegalArgumentException("Empty alias name for method " + m);
            }
            System.out.print(" as " + aliasName);
        } else {
            aliasName = altNames[0];
        }
        System.out.println("");
        method = m;
    }

    public String getAliasName() {
        return aliasName;
    }

    public Method getMethod() {
        return method;
    }
}
