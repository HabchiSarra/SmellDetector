package neo4j;

public class ClassCounter extends Query {
    private ClassCounter(QueryEngine queryEngine) {
        super(queryEngine, "ClassCounter");
    }

    public static ClassCounter createClassCounter(QueryEngine queryEngine) {
        return new ClassCounter(queryEngine);
    }

    @Override
    protected String getQuery(boolean details) {
        String query = "match(c:Class) return c.app_key as sha1,count(*) as nb_classes";
        return query;
    }
}
