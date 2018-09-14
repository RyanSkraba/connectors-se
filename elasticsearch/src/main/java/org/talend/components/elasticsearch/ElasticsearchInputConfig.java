package org.talend.components.elasticsearch;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.configuration.ui.widget.TextArea;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@Documentation("Elasticsearch input dataset.")
@OptionsOrder({ "dataset", "query" })
public class ElasticsearchInputConfig implements Serializable {

    @Option
    @Documentation("Dataset.")
    private ElasticsearchDataSet dataset;

    @Option
    @TextArea
    @Documentation("The query.")
    private String query;
}
