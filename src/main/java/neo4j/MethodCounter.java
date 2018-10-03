package neo4j;

public class MethodCounter extends Query {
    private MethodCounter(QueryEngine queryEngine) {
        super(queryEngine, "MethodCounter");
    }

    public static MethodCounter createMethodCounter(QueryEngine queryEngine) {
        return new MethodCounter(queryEngine);
    }

    @Override
    protected String getQuery(boolean details) {
        String query = "match(m:Method) return m.app_key as sha1,count(*) as nb_methods";
        return query;
    }
}