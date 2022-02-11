package com.ichanskiy.softserve.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ichanskiy.softserve.document.Status;
import com.ichanskiy.softserve.document.Twit;
import com.ichanskiy.softserve.service.TwitService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.repositories.RepositoryException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ichanskiy.softserve.document.Twit.TWIT_INDEX;

@Service
@RequiredArgsConstructor
public class TwitServiceImpl implements TwitService {

    private final ObjectMapper objectMapper;
    private final RestHighLevelClient elasticSearchClient;

    @SneakyThrows
    public Twit findById(String id) {
        return toTwit(elasticSearchClient.get(new GetRequest(TWIT_INDEX, id), RequestOptions.DEFAULT)
                .getSourceAsString());
    }

    @SneakyThrows
    public String create(Twit twit) {
        UUID uuid = UUID.randomUUID();
        twit.setId(uuid.toString());

        IndexRequest indexRequest = new IndexRequest(TWIT_INDEX)
                .id(twit.getId())
                .source(objectMapper.writeValueAsString(twit), XContentType.JSON);

        return elasticSearchClient.index(indexRequest, RequestOptions.DEFAULT).getId();
    }

    @SneakyThrows
    public void delete(String id) {
        DeleteRequest request = new DeleteRequest(TWIT_INDEX, id);
        elasticSearchClient.delete(request, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    public List<Twit> searchByStatus(Status status) {
        SearchRequest searchRequest = new SearchRequest(TWIT_INDEX);
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery("status", status.getValue()));
        searchRequest.source(builder);

        SearchResponse response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        return responseToTwit(response);
    }

    @SneakyThrows
    public List<Twit> searchByAndAuthorNameAndTaggedEmails(String authorName, List<String> taggedEmails) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termsQuery("taggedEmails", taggedEmails))
                .filter(QueryBuilders.fuzzyQuery("author.name", authorName));

        SearchRequest searchRequest = new SearchRequest(TWIT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort(SortBuilders.fieldSort("createdAt").order(SortOrder.ASC));
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(10);
        searchRequest.source(searchSourceBuilder);

        SearchResponse response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        return responseToTwit(response);
    }

    @SneakyThrows
    public List<String> searchByTitlePrefix(String titlePrefix) {
        final String titleSuggestionName = "title_suggestion";
        SearchRequest searchRequest = new SearchRequest(TWIT_INDEX);
        CompletionSuggestionBuilder suggestBuilder = new CompletionSuggestionBuilder("title")
                .size(10)
                .prefix(titlePrefix, Fuzziness.TWO)
                .skipDuplicates(true)
                .analyzer("standard");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.suggest(new SuggestBuilder().addSuggestion(titleSuggestionName, suggestBuilder));
        searchRequest.source(sourceBuilder);

        SearchResponse response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);

        return response.getSuggest()
                .getSuggestion(titleSuggestionName)
                .getEntries()
                .stream()
                .flatMap(options -> options.getOptions()
                        .stream()
                        .map(option -> option.getText().toString()))
                .toList();
    }

    @SneakyThrows
    public Map<String, Double> countOfAuthorsTwitsBetweenDate(ZonedDateTime from, ZonedDateTime to) {
        final String authorAggregation = "aggregation_by_author";
        final String subAggregationName = "current_year";
        SearchRequest searchRequest = new SearchRequest(TWIT_INDEX);

        TermsAggregationBuilder aggregationByAuthor = AggregationBuilders
                .terms(authorAggregation)
                .field("author.email.keyword");

        aggregationByAuthor.subAggregation(AggregationBuilders
                .dateRange(subAggregationName)
                .addRange(from, to)
                .field("createdAt"));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.aggregation(aggregationByAuthor);
        sourceBuilder.size(0);
        searchRequest.source(sourceBuilder);
        SearchResponse response = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);

        Terms aggregationByAuthorResponse = response.getAggregations().get(authorAggregation);
        if (aggregationByAuthorResponse == null) {
            return Map.of();
        }

        Map<String, Double> authorToDocumentsAggregation = new HashMap<>();
        for (Terms.Bucket bucket : aggregationByAuthorResponse.getBuckets()) {
            String authorEmail = bucket.getKeyAsString();
            Aggregations subAggregations = bucket.getAggregations();
            ParsedDateRange dateRange = subAggregations.get(subAggregationName);
            double docCount = dateRange.getBuckets().get(0).getDocCount();
            authorToDocumentsAggregation.put(authorEmail, docCount);
        }

        return authorToDocumentsAggregation;
    }

    private List<Twit> responseToTwit(SearchResponse search) {
        return Arrays.stream(search.getHits().getHits())
                .map(SearchHit::getSourceAsString)
                .map(this::toTwit)
                .toList();
    }

    private Twit toTwit(String json) {
        try {
            return objectMapper.readValue(json, Twit.class);
        } catch (IOException e) {
            throw new RepositoryException("Event deserialization failure for: ", json);
        }
    }

}


