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

public class CommitSizeQuery extends Query {
    private CommitSizeQuery(QueryEngine queryEngine) {
        super(queryEngine, "CommitSizeQuery");
    }


    public static CommitSizeQuery createCommitSize(QueryEngine queryEngine) {
        return new CommitSizeQuery(queryEngine);
    }


    @Override
    protected String getQuery(boolean details) {
        String query = "match(a:App) RETURN " +
                "a.app_key as sha1, " +
                "a.number_of_classes as number_of_classes, " +
                "a.number_of_methods as number_of_methods, " +
                "a.number_of_interfaces as number_of_interfaces, " +
                "a.number_of_broadcast_receivers as number_of_broadcast_receivers, " +
                "a.number_of_services as number_of_services, " +
                "a.number_of_content_providers as number_of_content_providers, " +
                "a.number_of_activities as number_of_activities, " +
                "a.number_of_variables as number_of_variables, " +
                "a.number_of_inner_classes as number_of_inner_classes, " +
                "a.number_of_async_tasks as number_of_async_tasks, " +
                "a.number_of_views as number_of_views";
        return query;
    }
}
