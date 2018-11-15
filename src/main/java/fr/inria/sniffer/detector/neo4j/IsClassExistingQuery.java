package fr.inria.sniffer.detector.neo4j;

public class IsClassExistingQuery extends Query {
    private final String sha1;
    private final String classIdentifier;

    public IsClassExistingQuery(QueryEngine queryEngine,
                                String sha1, String classIdentifier) {
        super(queryEngine, "ClassExistence");
        this.sha1 = sha1;
        this.classIdentifier = classIdentifier;
    }

    @Override
    protected String getQuery(boolean details) {
        return "MATCH (n:Class) WHERE " +
                "n.app_key='" + sha1 + "' AND n.name ='" + classIdentifier + "' " +
                "RETURN n";
    }
}
