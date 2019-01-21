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

package fr.inria.sniffer.detector.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class UnsupportedHardwareAccelerationQuery extends Query {

    private UnsupportedHardwareAccelerationQuery(QueryEngine queryEngine) {
        super(queryEngine, "UHA");
    }

    public static UnsupportedHardwareAccelerationQuery createUnsupportedHardwareAccelerationQuery(QueryEngine queryEngine) {
        return new UnsupportedHardwareAccelerationQuery(queryEngine);
    }


    @Override
    protected String getQuery(boolean details) {
        String[] uhas = {
                "drawPicture#android.graphics.Canvas",
                "drawVertices#android.graphics.Canvas",
                "drawPosText#android.graphics.Canvas",
                "drawTextOnPath#android.graphics.Canvas",
                "drawPath#android.graphics.Canvas",
                "setLinearText#android.graphics.Paint",
                "setMaskFilter#android.graphics.Paint",
                "setPathEffect#android.graphics.Paint",
                "setRasterizer#android.graphics.Paint",
                "setSubpixelText#android.graphics.Paint"
        };
        StringBuilder query = new StringBuilder("MATCH (a:App)-[:APP_OWNS_CLASS]->(cl:Class)-[:CLASS_OWNS_METHOD]->(m:Method)-[:CALLS]->(e:ExternalMethod) WHERE e.full_name='" + uhas[0] + "'");
        for (int i = 1; i < uhas.length; i++) {
            query.append(" OR e.full_name='").append(uhas[i]).append("' ");
        }
        query.append(" return DISTINCT a.commit_number as commit_number, m.app_key as key, cl.file_path as file_path");
        if (details) {
            query.append(",m.full_name as instance");
        } else {
            query.append(",count(m) as UHA");
        }
        return query.toString();
    }
}
