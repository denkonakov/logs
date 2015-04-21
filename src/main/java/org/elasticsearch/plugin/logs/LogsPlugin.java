package org.elasticsearch.plugin.logs;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.rest.action.logs.GetIndexesAsFileStructure;
import org.elasticsearch.rest.action.logs.GetIndexesList;
import org.elasticsearch.rest.action.logs.GetLogDataFromIndex;

/**
 * Created by deniskonakov on 2015-04-20.
 */
public class LogsPlugin extends AbstractPlugin {
    public LogsPlugin() {
    }

    public String name() {
        return "logs";
    }

    public String description() {
        return "Shows logstash indexes grouped by as somewhat close to the usual files";
    }

    public void processModule(Module module) {
        if(module instanceof RestModule) {
            ((RestModule)module).addRestAction(GetIndexesAsFileStructure.class);
            ((RestModule)module).addRestAction(GetLogDataFromIndex.class);
            ((RestModule)module).addRestAction(GetIndexesList.class);
        }

    }
}
