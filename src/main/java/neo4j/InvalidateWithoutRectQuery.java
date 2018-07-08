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

import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class InvalidateWithoutRectQuery extends Query {

    private InvalidateWithoutRectQuery(QueryEngine queryEngine) {
        super(queryEngine, "IWR");
    }

    public static InvalidateWithoutRectQuery createInvalidateWithoutRectQuery(QueryEngine queryEngine) {
        return new InvalidateWithoutRectQuery(queryEngine);
    }

    @Override
    public List<Map<String, Object>> fetchResult(boolean details) throws CypherException {
        List<Map<String, Object>> result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (a:App)-[:APP_OWNS_CLASS]->(cl:Class{is_view:true})-[:CLASS_OWNS_METHOD]->(n:Method{name:'onDraw'})-[:CALLS]->(e:ExternalMethod{name:'invalidate'}) " +
                    "WHERE NOT (e)-[:METHOD_OWNS_ARGUMENT]->(:ExternalArgument) " +
                    "return DISTINCT a.commit_number as commit_number, n.app_key as key, cl.file_path as file_path";
            if (details) {
                query += ",n.full_name as instance";
            } else {
                query += ",count(n) as IWR";
            }
            query += " ORDER BY key";
            result = queryEngine.toMap(graphDatabaseService.execute(query));
            ignored.success();
        }
        return result;
    }
}