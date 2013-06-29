package org.ut.biolab.medsavant.shared.query;

/**
 * Sort direction for the query
 */
public class QuerySort {

    private String column;

    private QuerySortDirection direction;

    public QuerySort(QuerySortDirection direction, String column) {
        this.direction = direction;
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public QuerySortDirection getDirection() {
        return direction;
    }

    public void setDirection(QuerySortDirection direction) {
        this.direction = direction;
    }
}
