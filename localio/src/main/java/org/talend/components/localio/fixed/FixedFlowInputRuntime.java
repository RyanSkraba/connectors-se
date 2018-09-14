package org.talend.components.localio.fixed;

import static org.talend.sdk.component.api.component.Icon.IconType.FLOW_SOURCE_O;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.PartitionMapper;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.runtime.beam.coder.registry.SchemaRegistryCoder;

@Version
@Icon(FLOW_SOURCE_O)
@PartitionMapper(name = "FixedFlowInputRuntime")
@Documentation("This component duplicates an input a configured number of times.")
public class FixedFlowInputRuntime extends PTransform<PBegin, PCollection<Record>> {

    private final FixedFlowInputConfiguration configuration;

    private final FixedDataSetRuntime datasetRuntime;

    public FixedFlowInputRuntime(@Option("configuration") final FixedFlowInputConfiguration configuration,
            FixedDataSetRuntime datasetRuntime) {
        this.configuration = configuration;
        this.datasetRuntime = datasetRuntime;
    }

    @Override
    public PCollection<Record> expand(final PBegin begin) {
        // The values to include in the PCollection
        List<Record> values = new LinkedList<>();

        if (configuration.getOverrideValuesAction() == FixedFlowInputConfiguration.OverrideValuesAction.NONE
                || configuration.getOverrideValuesAction() == FixedFlowInputConfiguration.OverrideValuesAction.APPEND) {
            if (!configuration.getDataset().getValues().trim().isEmpty()) {
                values.addAll(datasetRuntime.getValues(Integer.MAX_VALUE));
            }
        }

        if (configuration.getOverrideValuesAction() == FixedFlowInputConfiguration.OverrideValuesAction.APPEND
                || configuration.getOverrideValuesAction() == FixedFlowInputConfiguration.OverrideValuesAction.REPLACE) {
            configuration.getDataset().setValues(configuration.getOverrideValues());
            if (!configuration.getDataset().getValues().trim().isEmpty()) {
                values.addAll(datasetRuntime.getValues(Integer.MAX_VALUE));
            }
        }

        if (values.size() != 0) {
            final PCollectionView<List<Record>> data = ((PCollection<Record>) begin
                    .apply(Create.of(values).withCoder(SchemaRegistryCoder.of()))).apply(View.asList());
            return begin.apply(GenerateSequence.from(0).to(configuration.getRepeat())).apply(ParDo.of(new DoFn<Long, Record>() {

                @ProcessElement
                public void processElement(ProcessContext c) {
                    List<Record> indexedRecords = c.sideInput(data);
                    indexedRecords.forEach(r -> c.output(r));
                }
            }).withSideInputs(data));
        } else {
            return begin.apply(Create.of(Collections.emptyList()));
            // Deactivate RowGenerator for the poc.

            // return begin.apply(RowGeneratorIO.read().withSchema(runtime.getSchema()) //
            // .withSeed(0L) //
            // .withPartitions(1) //
            // .withRows(configuration.getRepeat()));
        }
    }
}
