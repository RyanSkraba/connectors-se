package org.talend.components.fileio.s3;

import static org.talend.sdk.component.api.component.Icon.IconType.FILE_S3_O;

import java.io.Serializable;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@Icon(FILE_S3_O)
@Documentation("Dataset of a S3 sink.")
@OptionsOrder({ "dataset", "overwrite", "mergeOutput" })
public class S3OutputConfig implements Serializable {

    @Option
    @Documentation("The dataset for S3")
    private S3DataSet dataset;

    @Option
    @Documentation("Should overwrite data.")
    private boolean overwrite;

    @Option
    @Documentation("Should merge data.")
    private boolean mergeOutput;
}
