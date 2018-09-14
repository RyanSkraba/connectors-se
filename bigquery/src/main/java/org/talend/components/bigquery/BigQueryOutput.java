package org.talend.components.bigquery;

import static org.talend.sdk.component.api.component.Icon.IconType.BIGQUERY;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.bigquery.runtime.BigQueryOutputRuntime;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.Processor;

@Version
@Icon(BIGQUERY)
@Processor(name = "BigQueryOutput")
@Documentation("This component writes inputs to BigQuery.")
public class BigQueryOutput extends PTransform<PCollection<IndexedRecord>, PDone> {

    private final BigQueryOutputConfig configuration;

    private final BigQueryConfigurationService service;

    public BigQueryOutput(@Option("configuration") final BigQueryOutputConfig configuration,
            final BigQueryConfigurationService service) {
        this.configuration = configuration;
        this.service = service;
    }

    @Override
    public PDone expand(final PCollection<IndexedRecord> input) {
        final BigQueryOutputRuntime runtime = new BigQueryOutputRuntime();
        runtime.initialize(service.newRuntimeContainer(), service.toOutputDataSet(configuration));
        return runtime.expand(input);
    }
}
