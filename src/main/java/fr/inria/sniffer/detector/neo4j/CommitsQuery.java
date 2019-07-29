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

/**
 * Return all existing commits in the current database.
 * 'commit_number' being the commit ordinal in the project and 'key' the commit sha1.
 */
public class CommitsQuery extends Query {
    public CommitsQuery(QueryEngine queryEngine) {
        super(queryEngine, "COMMITS");
    }

    @Override
    protected String getQuery(boolean details) {
        return "MATCH (a:App) RETURN DISTINCT a.commit_number as commit_number, a.app_key as key";
    }
}
