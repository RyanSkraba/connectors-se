package org.talend.components.bigquery;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.bigquery.output.BigQueryOutputProperties;
import org.talend.sdk.component.api.service.Service;

@Service
public class BigQueryConfigurationService {

    public BigQueryOutputProperties toOutputDataSet(final BigQueryOutputConfig configuration) {
        final BigQueryOutputProperties properties = new BigQueryOutputProperties("name");
        properties.setDatasetProperties(toDataSet(configuration.getDataset()));
        properties.tableOperation
                .setValue(BigQueryOutputProperties.TableOperation.valueOf(configuration.getTableOperation().name()));
        properties.writeOperation
                .setValue(BigQueryOutputProperties.WriteOperation.valueOf(configuration.getWriteOperation().name()));
        return properties;
    }

    public BigQueryDatasetProperties toDataSet(final BigQueryDataSet configuration) {
        final BigQueryDatasetProperties ref = new BigQueryDatasetProperties("datasetRef");
        ref.setDatastoreProperties(toDataStore(configuration.getDataStore()));
        ref.bqDataset.setValue(configuration.getBqDataset());
        ref.query.setValue(configuration.getQuery());
        ref.sourceType.setValue(BigQueryDatasetProperties.SourceType.valueOf(configuration.getSourceType().name()));
        return ref;
    }

    private BigQueryDatastoreProperties toDataStore(final BigQueryDataStore dataStore) {
        final BigQueryDatastoreProperties ref = new BigQueryDatastoreProperties("datastoreRef");
        ref.projectName.setValue(dataStore.getProjectName());
        ref.serviceAccountFile.setValue(dataStore.getServiceAccountFile());
        ref.tempGsFolder.setValue(dataStore.getTempGsFolder());
        return ref;
    }

    public RuntimeContainer newRuntimeContainer() {
        return new RuntimeContainer() {

            @Override
            public Object getComponentData(final String s, final String s1) {
                // todo: support replacement of BeamJobRuntimeContainer.PIPELINE_OPTIONS
                // note that it sounds like a design issue to have pipeline options here
                // and not at a higher level so probably don't reproduce this design when solving that
                return null;
            }

            @Override
            public void setComponentData(final String s, final String s1, final Object o) {
                // no-op
            }

            @Override
            public String getCurrentComponentId() {
                return null;
            }

            @Override
            public Object getGlobalData(final String s) {
                return null;
            }
        };
    }
}
