package com.ichanskiy.softserve.service;

import com.ichanskiy.softserve.document.Status;
import com.ichanskiy.softserve.document.Twit;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface TwitService {

    String create(Twit twit);

    void delete(String id);

    Twit findById(String id);

    List<Twit> searchByStatus(Status status);

    List<Twit> searchByAndAuthorNameAndTaggedEmails(String authorName, List<String> taggedEmails);

    List<String> searchByTitlePrefix(String titlePrefix);

    Map<String, Double> countOfAuthorsTwitsBetweenDate(ZonedDateTime from, ZonedDateTime to);

}
