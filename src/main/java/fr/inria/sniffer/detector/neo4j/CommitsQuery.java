package fr.inria.sniffer.detector.neo4j;

/**
 * Return all existing commits in the current database.
 * 'commit_number' being the commit ordinal in the project and 'key' the commit sha1.
 */
public class CommitsQuery extends Query {
    public CommitsQuery(QueryEngine queryEngine) {
        super(queryEngine, "COMMITS");
    }

    @Override
    protected String getQuery(boolean details) {
        return "MATCH (a:App) RETURN DISTINCT a.commit_number as commit_number, a.app_key as key";
    }
}
