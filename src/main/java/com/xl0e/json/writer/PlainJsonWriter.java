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

import java.io.IOException;
import java.io.Writer;

/**
 * Simple class wrapper around StringBuilder
 *
 */
public class PlainJsonWriter implements JsonWriter {
    private int size;
    protected final Writer writer;
    protected boolean comma;
    protected boolean name;

    public PlainJsonWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public PlainJsonWriter write(String o) {
        if (comma) {
            writeComma();
        }
        writeRaw(o);
        return this;
    }

    protected void writeComma() {
        writeRaw(',');
        comma = false;
    }

    protected PlainJsonWriter write(char o) {
        writeRaw(o);
        return this;
    }

    public int size() {
        return size;
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonWriter openObj() {
        writeRaw('{');
        comma = false;
        name = false;
        return this;
    }

    @Override
    public JsonWriter closeObj() {
        writeRaw('}');
        comma = false;
        name = false;
        return this;
    }

    @Override
    public JsonWriter comma() {
        comma = true;
        name = false;
        return this;
    }

    @Override
    public JsonWriter writeName(Object key) {
        if (comma) {
            writeComma();
        }
        writeRaw(String.valueOf(key));
        writeRaw(':');
        name = true;
        return this;
    }

    @Override
    public JsonWriter openArr() {
        comma = false;
        writeRaw('[');
        name = false;
        return this;
    }

    @Override
    public JsonWriter closeArr() {
        comma = false;
        writeRaw(']');
        name = false;
        return this;
    }

    protected void writeRaw(char c) {
        try {
            writer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeRaw(String c) {
        try {
            writer.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
