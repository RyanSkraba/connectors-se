package org.talend.components.fileio.hdfs;

import static org.talend.sdk.component.api.component.Icon.IconType.FILE_HDFS_O;

import java.io.Serializable;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@Icon(FILE_HDFS_O)
@Documentation("Dataset of a HDFS source.")
@OptionsOrder({ "dataset", "overwrite", "mergeOutput" })
public class SimpleFileIOOutputConfig implements Serializable {

    @Option
    @Documentation("The dataset configuration.")
    private SimpleFileIODataSet dataset;

    @Option
    @Documentation("Should overwrite data.")
    private boolean overwrite;

    @Option
    @Documentation("Should merge data.")
    private boolean mergeOutput;
}
