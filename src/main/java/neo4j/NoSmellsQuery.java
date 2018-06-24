package neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;

/**
 * Created by sarra on 24/07/17.
 */
public class NoSmellsQuery extends Query {
    public NoSmellsQuery(QueryEngine queryEngine) {
        super(queryEngine, "NOSMELL");
    }

    @Override
    public List<Map<String, Object>> fetchResult(boolean details) throws CypherException {
        List<Map<String, Object>> result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (a:App) where not( exists(a.has_IGS) OR exists(a.has_LIC)" +
                    "OR exists(a.has_IOD) OR exists(a.has_MIM) OR exists(a.has_IWR) OR exists(a.has_NLMR)" +
                    " OR exists(a.has_UIO) OR exists(a.has_UHA) OR exists(a.has_UCS) OR exists(a.has_HMU))" +
                    " return a.commit_number as commit_number, a.app_key as key ";
            if (details) {
                query += ", '-' as instance, a.commit_status as commit_status";
            } else {
                query += ",count(n) as NOSMELL";
            }
            result = queryEngine.toMap(graphDatabaseService.execute(query));
            ignored.success();
        }
        return result;
    }
}
