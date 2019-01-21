package fr.inria.sniffer.detector.neo4j;

public class IsMethodExistingQuery extends Query {
    private final String sha1;
    private final String methodIdentifier;

    public IsMethodExistingQuery(QueryEngine queryEngine,
                                 String sha1, String methodIdentifier) {
        super(queryEngine, "MethodExistence");
        this.sha1 = sha1;
        this.methodIdentifier = methodIdentifier;
    }

    @Override
    protected String getQuery(boolean details) {
        return "MATCH (n:Method) WHERE " +
                "n.app_key='" + sha1 + "' AND n.full_name ='" + methodIdentifier + "' " +
                "RETURN n";
    }
}
