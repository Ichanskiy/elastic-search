package com.ichanskiy.softserve.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Author {

    private String email;
    private String name;

}
