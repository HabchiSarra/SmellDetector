package neo4j;

public class CommitSizeQuery extends Query {
    private CommitSizeQuery(QueryEngine queryEngine) {
        super(queryEngine, "CommitSizeQuery");
    }


    public static CommitSizeQuery createCommitSize(QueryEngine queryEngine) {
        return new CommitSizeQuery(queryEngine);
    }


    @Override
    protected String getQuery(boolean details) {
        String query = "match(a:App) return a.app_key as sha1, " +
                "a.number_of_classes as number_of_classes,  a.number_of_methods as number_of_methods";
        return query;
    }
}
