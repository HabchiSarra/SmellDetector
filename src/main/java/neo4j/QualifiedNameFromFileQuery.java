package neo4j;

/**
 * Return the qualified name for the public class located in the given file
 * at the given commit.
 * 'qualified_name' will contain the canonical class name.
 */
public class QualifiedNameFromFileQuery extends Query {
    private final String sha;
    private final String file;

    public QualifiedNameFromFileQuery(QueryEngine queryEngine, String sha, String file) {
        super(queryEngine, "COMMITS");
        this.sha = sha;
        this.file = file;
    }

    @Override
    protected String getQuery(boolean details) {
        return "MATCH (a:Class) WHERE " +
                "a.app_key = \"" + sha + "\" AND " +
                "a.file_path = \"" + file + "\" AND " +
                "NOT EXISTS(a.is_inner_class) " +
                "RETURN a.name as qualified_name;";
    }
}
