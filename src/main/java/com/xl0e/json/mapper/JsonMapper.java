package com.xl0e.json.mapper;

import com.xl0e.json.writer.JsonWriter;

public interface JsonMapper {
    void map(Object o, JsonWriter writer);
}
