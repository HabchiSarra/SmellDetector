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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class LICQuery extends Query {

    private LICQuery(QueryEngine queryEngine) {
        super(queryEngine, "LIC");
    }

    public static LICQuery createLICQuery(QueryEngine queryEngine) {
        return new LICQuery(queryEngine);
    }


    @Override
    public List<Map<String, Object>> fetchResult(boolean details) throws CypherException {
        List<Map<String, Object>> result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (a:App)-[:APP_OWNS_CLASS]->(cl:Class) WHERE exists(cl.is_inner_class) AND NOT exists(cl.is_static) " +
                    "RETURN DISTINCT a.commit_number as commit_number, cl.app_key as key, cl.file_path as file_path";
            if (details) {
                query += ",cl.name as instance";
            } else {
                query += ",count(cl) as LIC";
            }
            query += " ORDER BY key";
            result = queryEngine.toMap(graphDatabaseService.execute(query));
            ignored.success();
        }
        return result;
    }


}
