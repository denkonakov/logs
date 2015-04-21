package org.elasticsearch.rest.action.logs;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Returns the list of Indexes. Will return only 30 last ones.
 * We suppose than there is NO other indexes but logstash-* ones.
 *
 * Created by deniskonakov on 2015-04-20.
 */
public class GetIndexesList extends BaseRestHandler {
    public static final int MAX_INDEXES_TO_SHOW = 30;

    @Inject
    public GetIndexesList(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(RestRequest.Method.GET, "/_logs/indexes", this);
    }

    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        MetaData metaData = client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData();

        String[] indicies = metaData.getConcreteAllIndices();
        Arrays.sort(indicies, Collections.reverseOrder());
        this.logger.debug("Indexes we got='{}', size='{}'", indicies, indicies.length);

        try {
            XContentBuilder result = XContentFactory.jsonBuilder().startArray();
            for (int i = 0; i < (MAX_INDEXES_TO_SHOW <= indicies.length ? MAX_INDEXES_TO_SHOW : indicies.length); i++) {
                result.value(indicies[i]);
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
