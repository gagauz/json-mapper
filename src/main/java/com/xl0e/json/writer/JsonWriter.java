package com.xl0e.json.writer;

public interface JsonWriter {

    void flush();

    JsonWriter write(String string);

    JsonWriter openObj();

    JsonWriter closeObj();

    JsonWriter comma();

    JsonWriter writeName(Object key);

    JsonWriter openArr();

    JsonWriter closeArr();

}
