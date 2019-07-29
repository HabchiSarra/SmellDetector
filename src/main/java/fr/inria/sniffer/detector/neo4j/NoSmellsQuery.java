/**
 *   Sniffer - Analyze the history of Android code smells at scale.
 *   Copyright (C) 2019 Sarra Habchi
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.inria.sniffer.detector.neo4j;

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
    protected String getQuery(boolean details) {
        String query = "MATCH (a:App) where not( exists(a.has_IGS) OR exists(a.has_LIC)" +
                "OR exists(a.has_IOD) OR exists(a.has_MIM) OR exists(a.has_IWR) OR exists(a.has_NLMR)" +
                " OR exists(a.has_UIO) OR exists(a.has_UHA) OR exists(a.has_UCS) OR exists(a.has_HMU))" +
                " return a.commit_number as commit_number, a.app_key as key ";
        if (details) {
            query += ", '-' as instance, a.commit_status as commit_status";
        } else {
            query += ",count(n) as NOSMELL";
        }
        return query;
    }
}
