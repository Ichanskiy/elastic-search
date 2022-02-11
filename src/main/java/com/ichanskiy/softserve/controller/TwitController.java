package com.ichanskiy.softserve.controller;

import com.ichanskiy.softserve.document.Status;
import com.ichanskiy.softserve.document.Twit;
import com.ichanskiy.softserve.service.TwitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController("/twits")
@RequiredArgsConstructor
public class TwitController {

    private final TwitService twitService;

    @GetMapping
    public Twit findById(@RequestParam String id) {
        return twitService.findById(id);
    }

    @PostMapping
    public String create(@RequestBody Twit twit) {
        return twitService.create(twit);
    }

    @DeleteMapping
    public void delete(@RequestParam String id) {
        twitService.delete(id);
    }

    @GetMapping("/status")
    public List<Twit> searchByStatus(@RequestParam Status status) {
        return twitService.searchByStatus(status);
    }

    @GetMapping("/titlePrediction")
    public List<String> searchByTitlePrefix(@RequestParam String title) {
        return twitService.predictionByTitlePrefix(title);
    }

    @GetMapping("/byAuthorNameAndTaggedEmails")
    public List<Twit> searchByAndAuthorNameAndTaggedEmails(@RequestParam String authorName,
                                                           @RequestParam List<String> taggedEmails) {
        return twitService.searchByAndAuthorNameAndTaggedEmails(authorName, taggedEmails);
    }

    @GetMapping("/countOfAuthorsTwitsByPeriod")
    public Map<String, Double> countOfAuthorsTwitsBetweenDate(@RequestParam ZonedDateTime startDate,
                                                              @RequestParam ZonedDateTime endDate) {
        return twitService.authorsToCountOfTwitsBetweenDate(startDate, endDate);
    }

}
