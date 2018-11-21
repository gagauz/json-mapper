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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class TestCaseParser {

    public static void testParser1() throws IOException {
        Parser p = new Parser(new StringReader("asdasd?k=v&a=b&c=d&m="));
        Map<String, String> params = new HashMap<>();
        String[] lastKey = { null };
        p.onDelimiter('?', s -> {
        })
                .onDelimiter('=', s -> {
                    lastKey[0] = s;
                    params.put(s, null);
                })
                .onDelimiter('&', s -> {
                    params.put(lastKey[0], s);
                    lastKey[0] = null;
                })
                .onEnd(s -> {
                    if (null == lastKey[0]) {
                        params.put(s, null);
                    } else {
                        params.put(lastKey[0], s);
                    }
                })
                .parse();
        System.out.println(params);
    }

    public static void testParser() throws IOException {
        Parser p = new Parser(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-file-for-parser.txt")));
        int[] ints = { 0, 0 };
        p.onDelimiter('{', s -> {
            ints[0]++;
        }).onDelimiter('}', s -> {
            ints[1]++;
        }).parse();
        if (ints[0] != ints[1]) {
            throw new IllegalStateException("Error in open and close delimiters");
        }
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            testParser();
        }
        System.out.println("Parser : " + (System.currentTimeMillis() - start));
        testParser1();
    }
}
