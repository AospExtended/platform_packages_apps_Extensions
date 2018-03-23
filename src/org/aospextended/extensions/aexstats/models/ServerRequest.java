package org.aospextended.extensions.aexstats.models;

/**
 * Created by ishubhamsingh on 25/9/17.
 */

public class ServerRequest {

    private String operation;
    private StatsData stats;

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setStats(StatsData stats) {
        this.stats = stats;
    }

}