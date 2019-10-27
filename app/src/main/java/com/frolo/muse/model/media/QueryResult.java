package com.frolo.muse.model.media;

import java.util.List;

public final class QueryResult {
    private final String query;
    private final List<?extends Media> result;

    public QueryResult(String query, List<?extends Media> result) {
        this.query = query;
        this.result = result;
    }

    public String getQuery() {
        return query;
    }

    public List<? extends Media> getResult() {
        return result;
    }
}
