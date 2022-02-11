package com.ichanskiy.softserve.document;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Status {
    ACTIVE("active"),
    HIDDEN("hidden"),
    DELETED("deleted");

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}