package org.talend.components.fileio.hdfs;

import static org.talend.sdk.component.api.component.Icon.IconType.FILE_HDFS_O;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.simplefileio.runtime.SimpleFileIOOutputRuntime;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.Processor;

@Version
@Icon(FILE_HDFS_O)
@Processor(name = "SimpleFileIOOutput")
@Documentation("This component writes data to HDFS.")
public class SimpleFileIOOutput extends PTransform<PCollection<IndexedRecord>, PDone> {

    private final SimpleFileIOOutputConfig configuration;

    private final SimpleFileIOConfigurationService service;

    public SimpleFileIOOutput(@Option("configuration") final SimpleFileIOOutputConfig configuration,
            final SimpleFileIOConfigurationService service) {
        this.configuration = configuration;
        this.service = service;
    }

    @Override
    public PDone expand(final PCollection<IndexedRecord> input) {
        final SimpleFileIOOutputRuntime runtime = new SimpleFileIOOutputRuntime();
        runtime.initialize(null, service.toOutputConfiguration(configuration));
        return runtime.expand(input);
    }
}
