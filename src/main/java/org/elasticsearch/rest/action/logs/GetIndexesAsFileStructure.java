package org.elasticsearch.rest.action.logs;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.io.IOException;

/**
 * Aggregates the log documents by hosts and by facility. It should
 * give the folder-like structure folder/files to show on the page.
 *
 * Created by deniskonakov on 2015-04-20.
 */
public class GetIndexesAsFileStructure extends BaseRestHandler {
    @Inject
    public GetIndexesAsFileStructure(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(RestRequest.Method.GET, "/_logs/as_files", this);
    }

    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        StringTerms stringTerms = client.prepareSearch().setQuery(QueryBuilders.matchAllQuery())
                .setSize(0).addAggregation(AggregationBuilders.terms("hosts").field("host.raw")
                        .subAggregation(AggregationBuilders.terms("components").field("facility.raw")))
                .get().getAggregations().get("hosts");

        try {
            XContentBuilder result = XContentFactory.jsonBuilder().startArray();
            for (Terms.Bucket bucket : stringTerms.getBuckets()) {
                result.startObject().field("folder", bucket.getKey());
                StringTerms st = bucket.getAggregations().get("components");
                if (st != null) {
                    result.array("files", st.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKey).toArray());
                }
                result.endObject();
            }
            result.endArray();

            channel.sendResponse(new BytesRestResponse(RestStatus.OK, result));
        } catch (IOException e) {
            try {
                channel.sendResponse(new BytesRestResponse(channel, e));
            } catch (IOException e1) {
                this.logger.error("We were unable to send Error response for exception", e1);
                channel.sendResponse(new BytesRestResponse(RestStatus.NO_CONTENT));
            }
        }

    }
}
