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

import java.util.ArrayList;
import java.util.List;

public class TestClass {

    private static int counter = 0;
    private int id;
    private TestClass parent;
    private List<TestClass> children;

    public TestClass() {
        id = ++counter;
    }

    public TestClass(TestClass parent) {
        setParent(parent);
    }

    public String getMethod1() {
        return "abc" + id;
    }

    public int getMethod2() {
        return 1;
    }

    public TestClass getParent() {
        return parent;
    }

    public void setParent(TestClass newParent) {
        if (this == newParent) {
            throw new IllegalStateException();
        }
        if (null != parent) {
            if (null == newParent && null != parent.children) {
                parent.children.remove(this);
            }
            parent.children.add(this);
        }

        if (null != newParent) {
            if (null == newParent.children) {
                newParent.children = new ArrayList<TestClass>();
            }
            newParent.children.add(this);
        }
        parent = newParent;
    }

    public List<TestClass> getChildren() {
        return children;
    }
}
