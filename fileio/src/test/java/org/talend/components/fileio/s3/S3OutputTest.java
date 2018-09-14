package org.talend.components.fileio.s3;

import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;
import static org.talend.sdk.component.runtime.manager.ComponentManager.ComponentType.PROCESSOR;

import java.io.IOException;
import java.util.Map;

import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.talend.components.adapter.beam.transform.ConvertToIndexedRecord;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit.ComponentsHandler;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

@Disabled("Need a S3 account or to impl a mock")
@WithComponents("org.talend.components.fileio")
class S3OutputTest {

    @Injected
    private ComponentsHandler handler;

    @Service
    private S3ConfigurationService configurationService;

    @Test
    void output() throws IOException {
        final S3OutputConfig configuration = new S3OutputConfig();
        configuration.setDataset(new S3DataSet());
        configuration.getDataset().setDatastore(new S3DataStore());
        configuration.setMergeOutput(true);

        final Map<String, String> asConfig = configurationByExample().forInstance(configuration).configured().toMap();
        final Pipeline pipeline = Pipeline.create();
        final PTransform<PCollection<IndexedRecord>, PDone> processor = handler.asManager()
                .createComponent("FileIO", "S3Output", PROCESSOR, 1, asConfig)
                .map(e -> (PTransform<PCollection<IndexedRecord>, PDone>) e)
                .orElseThrow(() -> new IllegalArgumentException("No component for fixed flow input"));
        pipeline.apply(Create.of(ConvertToIndexedRecord.convertToAvro(new String[] { "1", "one" }), //
                ConvertToIndexedRecord.convertToAvro(new String[] { "2", "two" }))).apply(processor);
        pipeline.run().waitUntilFinish();

        /*
         * todo:
         * final S3DatasetProperties datasetProps =
         * configurationService.toOutputConfiguration(configuration).getDatasetProperties();
         * final FileSystem s3FileSystem = S3Connection.createFileSystem(datasetProps);
         * MiniDfsResource.assertReadFile(s3FileSystem, s3.getS3APath(datasetProps), "1;one", "2;two");
         * MiniDfsResource.assertFileNumber(s3FileSystem, s3.getS3APath(datasetProps), 1);
         */
    }
}
