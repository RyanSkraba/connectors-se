package org.talend.components.fileio.hdfs;

import org.talend.components.simplefileio.SimpleFileIODatasetProperties;
import org.talend.components.simplefileio.SimpleFileIODatastoreProperties;
import org.talend.components.simplefileio.SimpleFileIOFormat;
import org.talend.components.simplefileio.input.SimpleFileIOInputProperties;
import org.talend.components.simplefileio.output.SimpleFileIOOutputProperties;
import org.talend.sdk.component.api.service.Service;

@Service
public class SimpleFileIOConfigurationService {

    public SimpleFileIOInputProperties toInputConfiguration(final SimpleFileIODataSet configuration) {
        final SimpleFileIOInputProperties properties = new SimpleFileIOInputProperties("name");
        properties.setDatasetProperties(toDataSet(configuration));
        properties.limit.setValue(configuration.getLimit());
        return properties;
    }

    public SimpleFileIOOutputProperties toOutputConfiguration(final SimpleFileIOOutputConfig configuration) {
        final SimpleFileIOOutputProperties properties = new SimpleFileIOOutputProperties("name");
        properties.setDatasetProperties(toDataSet(configuration.getDataset()));
        properties.mergeOutput.setValue(configuration.isMergeOutput());
        properties.overwrite.setValue(configuration.isOverwrite());
        return properties;
    }

    private SimpleFileIODatasetProperties toDataSet(final SimpleFileIODataSet configuration) {
        final SimpleFileIODatasetProperties dataset = new SimpleFileIODatasetProperties("datasetRef");
        dataset.fieldDelimiter
                .setValue(SimpleFileIODatasetProperties.FieldDelimiterType.valueOf(configuration.getFieldDelimiter().name()));
        dataset.specificFieldDelimiter.setValue(configuration.getSpecificFieldDelimiter());
        dataset.recordDelimiter
                .setValue(SimpleFileIODatasetProperties.RecordDelimiterType.valueOf(configuration.getRecordDelimiter().name()));
        dataset.specificRecordDelimiter.setValue(configuration.getSpecificRecordDelimiter());
        dataset.format.setValue(SimpleFileIOFormat.valueOf(configuration.getFormat().name()));
        dataset.path.setValue(configuration.getPath());
        dataset.setDatastoreProperties(toDataStore(configuration.getDatastore()));
        return dataset;
    }

    private SimpleFileIODatastoreProperties toDataStore(final SimpleFileIODataStore datastore) {
        final SimpleFileIODatastoreProperties datastoreRef = new SimpleFileIODatastoreProperties("datastoreRef");
        datastoreRef.kerberosKeytab.setValue(datastore.getKerberosKeytab());
        datastoreRef.kerberosPrincipal.setValue(datastore.getKerberosPrincipal());
        datastoreRef.useKerberos.setValue(datastore.isUseKerberos());
        datastoreRef.userName.setValue(datastore.getUsername());
        return datastoreRef;
    }
}
