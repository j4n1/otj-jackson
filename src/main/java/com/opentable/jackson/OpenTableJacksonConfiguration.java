/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opentable.jackson;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTableJacksonConfiguration
{
    public enum JacksonTimeFormat {
        MILLIS, ISO8601;
    }

    @Value("${ot.jackson.time-format:ISO8601}")
    JacksonTimeFormat timeFormat = JacksonTimeFormat.ISO8601;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModules( guavaModule(),
                                javaTimeModule(),
                                mrBeanModule(),
                                afterburnerModule(),
                                customUuidModule());

        // This needs to be set, otherwise the mapper will fail on every new property showing up.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Don't write out nulls by default -- if you really want them, you can change it with setOptions later.
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

        // No need to flush after every value, which cuts throughput by ~30%
        mapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);

        // Awful JAXB shit
        mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);

        switch(timeFormat) {
        case MILLIS:
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, Boolean.TRUE);
            break;
        case ISO8601:
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, Boolean.FALSE);
            break;
        default:
            throw new IllegalStateException("Unknown time format: " +timeFormat);
        }

        // by default, don't serialize null values.
        mapper.setSerializationInclusion(Include.NON_NULL);

        return mapper;
    }

    GuavaModule guavaModule() {
        return new GuavaModule();
    }

    JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    MrBeanModule mrBeanModule() {
        return new MrBeanModule();
    }

    AfterburnerModule afterburnerModule() {
        return new AfterburnerModule();
    }

    CustomUuidModule customUuidModule() {
        return new CustomUuidModule();
    }
}