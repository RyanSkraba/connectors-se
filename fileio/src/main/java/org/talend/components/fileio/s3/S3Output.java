package org.talend.components.fileio.s3;

import static org.talend.sdk.component.api.component.Icon.IconType.FILE_S3_O;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.talend.components.simplefileio.runtime.s3.S3OutputRuntime;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.Processor;

@Version
@Icon(FILE_S3_O)
@Processor(name = "S3Output")
@Documentation("This component writes data to S3.")
public class S3Output extends PTransform<PCollection<IndexedRecord>, PDone> {

    private final S3OutputConfig configuration;

    private final S3ConfigurationService service;

    public S3Output(@Option("configuration") final S3OutputConfig dataSet, final S3ConfigurationService service) {
        this.configuration = dataSet;
        this.service = service;
    }

    @Override
    public PDone expand(final PCollection<IndexedRecord> input) {
        final S3OutputRuntime runtime = new S3OutputRuntime();
        runtime.initialize(null, service.toOutputConfiguration(configuration));
        return runtime.expand(input);
    }
}
