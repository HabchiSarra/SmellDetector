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
