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

/**
 * Wrapper around {@link JsonWriter} to indent JSON text 
 *
 */
public class JsonIndentWriter extends JsonWriter {
    private final StringBuilder sb = new StringBuilder(100000);
    private final StringBuilder indent = new StringBuilder();
    private final String tab = "   ";

    @Override
    public JsonIndentWriter start() {
        indent.append(tab);
        return this;
    }

    @Override
    public JsonIndentWriter finish() {
        indent.setLength(indent.length() - tab.length());
        nl();
        return this;
    }

    @Override
    public JsonIndentWriter write(Object o) {
        sb.append(o);
        return this;
    }

    @Override
    public JsonIndentWriter nl() {
        sb.append("\n").append(indent);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public int size() {
        return sb.length();
    }

    @Override
    public String copy(int off) {
        return sb.substring(off + 1);
    }
}
