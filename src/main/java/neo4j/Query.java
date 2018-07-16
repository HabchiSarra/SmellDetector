/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 17/08/15.
 */
public abstract class Query {
    protected QueryEngine queryEngine;
    protected GraphDatabaseService graphDatabaseService;
    protected String smellName;

    public Query(QueryEngine queryEngine, String smellName) {
        this.queryEngine = queryEngine;
        this.smellName = smellName;
        graphDatabaseService = queryEngine.getGraphDatabaseService();
    }

    /**
     * Generate query to execute.
     *
     * @param details The specific query.
     * @return The query String.
     */
    protected abstract String getQuery(boolean details);

    public void execute(boolean details) throws CypherException, IOException {
        List<Map<String, Object>> result = fetchResult(details);
        queryEngine.resultToCSV(result, "_" + smellName + ".csv");

    }

    public final List<Map<String, Object>> fetchResult(boolean details, boolean orderByCommit) throws CypherException {
        List<Map<String, Object>> result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = getQuery(details);
            if (orderByCommit) {
                query += " ORDER BY commit_number";
            }
            result = queryEngine.toMap(graphDatabaseService.execute(query));
            ignored.success();
        }
        return result;
    }

    public final List<Map<String, Object>> fetchResult(boolean details) {
        return fetchResult(details, false);
    }


    /**
     * Return a stream to the neo4j result via the object {@link Result}.
     * This stream has to be fully consumed or closed to avoid any leak!
     *
     * @param details Export smell details.
     * @param orderByCommit Order by commit number.
     * @return The {@link Result}.
     * @throws CypherException If anything goes wrong.
     */
    public Result streamResult(boolean details, boolean orderByCommit) throws CypherException {
        String query = getQuery(details);
        if (orderByCommit) {
            query += " ORDER BY commit_number";
        }
        return graphDatabaseService.execute(query);
    }

    public Result streamResult(boolean details) throws CypherException {
        return streamResult(details, false);
    }

    public String getSmellName() {
        return smellName;
    }
}
