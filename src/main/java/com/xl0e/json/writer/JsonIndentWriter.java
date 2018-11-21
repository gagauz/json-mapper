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
package com.xl0e.json.writer;

import java.io.Writer;

/**
 * Wrapper around {@link PlainJsonWriter} to indent JSON text
 *
 */
public class JsonIndentWriter extends PlainJsonWriter {
    private final StringBuilder indent = new StringBuilder();
    private final String tab = "  ";

    public JsonIndentWriter(Writer writer) {
        super(writer);
    }

    @Override
    public PlainJsonWriter write(String o) {
        writeComma();
        writeIndent();
        writeRaw(o);
        return this;
    }

    @Override
    protected void writeComma() {
        if (comma) {
            writeRaw(',');
            writeRaw('\n');
            comma = false;
        }
    }

    @Override
    public JsonWriter openObj() {
        writeComma();
        writeIndent();
        super.openObj();
        writeRaw('\n');
        indent.append(tab);
        return this;
    }

    @Override
    public JsonWriter closeObj() {
        writeRaw('\n');
        indent.setLength(indent.length() - tab.length());
        writeIndent();
        super.closeObj();
        return this;
    }

    @Override
    public JsonWriter openArr() {
        writeIndent();
        super.openArr();
        writeRaw('\n');
        indent.append(tab);
        return this;
    }

    @Override
    public JsonWriter closeArr() {
        writeRaw('\n');
        indent.setLength(indent.length() - tab.length());
        writeIndent();
        super.closeArr();
        return this;
    }

    @Override
    public JsonWriter writeName(Object key) {
        if (comma) {
            writeComma();
        }
        writeIndent();
        writeRaw(String.valueOf(key));
        writeRaw(':');
        writeRaw(' ');
        name = true;
        return this;
    }

    protected void writeIndent() {
        if (!name) {
            name = false;
            writeRaw(indent.toString());
        }
    }
}
