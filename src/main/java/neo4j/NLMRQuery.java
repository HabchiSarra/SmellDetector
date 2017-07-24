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
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class NLMRQuery extends Query {

    private NLMRQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static NLMRQuery createNLMRQuery(QueryEngine queryEngine) {
        return new NLMRQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws CypherException, IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (a:App)-[:APP_OWNS_CLASS]->(cl:Class) WHERE exists(cl.is_activity) AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method { name: 'onLowMemory' }) AND NOT (cl)-[:EXTENDS]->(:Class) SET a.has_NLMR=true RETURN a.commit_number as commit_number, cl.app_key as key";
            if(details){
                query += ",cl.name as instance, a.commit_status as commit_status";
            }else{
                query += ",count(cl) as NLMR";
            }
            Result result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_NLMR.csv");
            ignored.success();
        }
    }
}
