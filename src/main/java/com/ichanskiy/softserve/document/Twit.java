package com.ichanskiy.softserve.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Twit {

    public static final String TWIT_INDEX = "twit";

    private String id;
    private Status status;
    private String title;
    private String text;
    private Author author;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;
    private List<String> taggedEmails;

}
