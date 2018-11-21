package com.xl0e.json.mapper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Parser {

    private final Reader reader;
    private Map<Character, Consumer<String>> handlers;
    private Consumer<String> end;

    public Parser(Reader reader) {
        this.reader = reader;
    }

    public Parser onDelimiter(char c, Consumer<String> consumer) {
        if (null == handlers) {
            handlers = new HashMap<>();
        }
        handlers.put(c, consumer);
        return this;
    }

    public Parser onEnd(Consumer<String> consumer) {
        end = consumer;
        return this;
    }

    public void parse() throws IOException {
        int r = 0;
        StringBuilder sb = new StringBuilder();
        while ((r = reader.read()) > -1) {
            Character c = Character.valueOf((char) r);
            Consumer<String> consumer = handlers.get(c);
            if (null == consumer) {
                sb.append(c);
            } else {
                consumer.accept(sb.toString());
                sb.delete(0, sb.length());
            }
        }
        if (null != end) {
            end.accept(sb.toString());
        }
    }
}
