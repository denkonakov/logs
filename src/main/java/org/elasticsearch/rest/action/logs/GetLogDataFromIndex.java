package org.elasticsearch.rest.action.logs;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;

/**
 * Show the logs from the selected index
 *
 * Created by deniskonakov on 2015-04-20.
 */
public class GetLogDataFromIndex extends BaseRestHandler {
    public static final String DEFAULT_FOLDER_FILE = "start";
    public static int DEFAULT_LINES = 20;
    public static int FETCH_SIZE = 1000;

    @Inject
    public GetLogDataFromIndex(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(RestRequest.Method.GET, "/_logs/{folder}/{file}", this);
    }

    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        String folder = request.param("folder", DEFAULT_FOLDER_FILE);
        String file = request.param("file", DEFAULT_FOLDER_FILE);
        if (folder.equals(DEFAULT_FOLDER_FILE) || file.equals(DEFAULT_FOLDER_FILE)) {
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, "[]"));
        }

        String type = request.param("type", READ_MODE.TAIL.getValue());
        int lines = request.paramAsInt("lines", DEFAULT_LINES);
        String index = request.param("index", READ_MODE.ALL.getValue());
        logger.info("Folder='{}', File='{}', Type='{}', Lines='{}', Index='{}'", folder, file, type, lines, index);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index.equals(GetLogDataFromIndex.READ_MODE.ALL.getValue()) ? new String[0] : new String[]{index}).setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("host.raw", folder)).must(QueryBuilders.termQuery("facility.raw", file)));
        if (GetLogDataFromIndex.READ_MODE.cast(type) != GetLogDataFromIndex.READ_MODE.ALL) {
            searchRequestBuilder.setFrom(0).setSize(lines);
        }

        switch (GetLogDataFromIndex.READ_MODE.cast(type)) {
            case MORE:
            case ALL:
                searchRequestBuilder.addSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.ASC));
                break;
            case TAIL:
                searchRequestBuilder.addSort(SortBuilders.fieldSort("@timestamp").order(SortOrder.DESC));
        }

        SearchResponse searchResponse = searchRequestBuilder.get();
        if (RestStatus.OK == searchResponse.status()) {
            List<LogEntry> logs = new LinkedList();
            if (READ_MODE.cast(type) == READ_MODE.ALL) {
                for (int i = 0; searchResponse.getHits().hits().length != 0; i++) {
                    searchResponse = searchRequestBuilder.setSize(FETCH_SIZE).setFrom(FETCH_SIZE * i).get();
                    fillLogList(logs, searchResponse.getHits());
                }
            } else {
                fillLogList(logs, searchResponse.getHits());
            }

            if (GetLogDataFromIndex.READ_MODE.cast(type) == GetLogDataFromIndex.READ_MODE.TAIL) {
                Collections.reverse(logs);
            }

            try {
                XContentBuilder result = XContentFactory.jsonBuilder().startArray();
                for (LogEntry logEntry : logs) {
                    result.startObject().field("time", logEntry.getTime()).field("data", logEntry.getLogMessage()).endObject();
                }
                result.endArray();

                channel.sendResponse(new BytesRestResponse(RestStatus.OK, result));
            } catch (IOException e) {
                try {
                    channel.sendResponse(new BytesRestResponse(channel, e));
                } catch (IOException e1) {
                    this.logger.error("We were unable to send Error response for exception", e1, new Object[0]);
                    channel.sendResponse(new BytesRestResponse(RestStatus.NO_CONTENT));
                }
            }
        } else {
            this.logger.error("The result of querying the indexes was not OK!");
        }

    }

    private void fillLogList(List<LogEntry> logs, SearchHits hits) {
        for (SearchHit hit : hits) {
            Map source = hit.getSource();
            if (source != null) {
                logs.add(this.createLogFromDocument(source));
            } else {
                this.logger.error("The Hit source was null! ID'{}'", new Object[]{hit.getId()});
            }
        }

    }

    private GetLogDataFromIndex.LogEntry createLogFromDocument(Map<String, Object> source) {
        StringBuilder sb = new StringBuilder();
        sb.append(source.get("Severity")).append(" : ").append("[").append(source.get("Thread")).append("] ");
        if (source.get("StackTrace") != null) {
            sb.append(source.get("StackTrace"));
        } else {
            sb.append(source.get("LoggerName")).append("(").append(source.get("SourceMethodName")).append(")").append(" - ").append(source.get("message"));
        }

        return new GetLogDataFromIndex.LogEntry(source.get("Time").toString(), sb.toString());
    }

    private static class LogEntry {
        private String time;
        private String data;

        public LogEntry(String time, String logMessage) {
            this.time = time;
            this.data = logMessage;
        }

        public String getTime() {
            return this.time;
        }

        public String getLogMessage() {
            return this.data;
        }
    }

    public enum READ_MODE {
        MORE("more"),
        TAIL("tail"),
        ALL("all");

        private String value;

        READ_MODE(String value) {
            this.value = value;
            READ_MODE.Holder.MAP.put(value, this);
        }

        public String getValue() {
            return this.value;
        }

        public static GetLogDataFromIndex.READ_MODE cast(String val) {
            READ_MODE t = READ_MODE.Holder.MAP.get(val);
            if (t == null) {
                throw new IllegalStateException(String.format("Unsupported type %s.", val));
            } else {
                return t;
            }
        }

        private static class Holder {
            static Map<String, GetLogDataFromIndex.READ_MODE> MAP = new HashMap();

            private Holder() {
            }
        }
    }
}
