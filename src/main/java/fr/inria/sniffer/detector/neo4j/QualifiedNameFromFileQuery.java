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
