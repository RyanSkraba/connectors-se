package org.talend.components.bigquery;

import static org.talend.sdk.component.api.component.Icon.IconType.BIGQUERY;

import java.io.Serializable;

import org.talend.components.bigquery.output.BigQueryOutputProperties;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@Icon(BIGQUERY)
@Documentation("Dataset of a BigQuery component.")
@OptionsOrder({ "dataset", "tableOperation", "writeOperation" })
public class BigQueryOutputConfig implements Serializable {

    @Option
    @Documentation("BigQuery Dataset")
    private BigQueryDataSet dataset;

    @Option
    @Documentation("The BigQuery table operation")
    private TableOperation tableOperation = TableOperation.NONE;

    @Option
    @Documentation("The BigQuery write operation")
    private WriteOperation writeOperation = WriteOperation.APPEND;

    public enum TableOperation {
        /**
         * Specifics that tables should not be created.
         *
         * <p>
         * If the output table does not exist, the write fails.
         */
        NONE,
        /**
         * Specifies that tables should be created if needed. This is the default behavior.
         *
         * <p>
         * Requires that a table schema is provided via {@link BigQueryDatasetProperties#main}. This precondition is
         * checked before starting a job. The schema is not required to match an existing table's schema.
         *
         * <p>
         * When this transformation is executed, if the output table does not exist, the table is created from the
         * provided schema.
         */
        CREATE_IF_NOT_EXISTS,
        /**
         * Specifies that tables should be droped if exists, and create by the provided schema, which actually the
         * combine with TRUNCATE and CREATE_IF_NOT_EXISTS
         */
        DROP_IF_EXISTS_AND_CREATE,
        /**
         * Specifies that write should replace a table.
         *
         * <p>
         * The replacement may occur in multiple steps - for instance by first removing the existing table, then
         * creating a replacement, then filling it in. This is not an atomic operation, and external programs may see
         * the table in any of these intermediate steps.
         */
        TRUNCATE,
    }

    public enum WriteOperation {
        /**
         * Specifies that rows may be appended to an existing table.
         */
        APPEND,
        /**
         * Specifies that the output table must be empty. This is the default behavior.
         *
         * <p>
         * If the output table is not empty, the write fails at runtime.
         *
         * <p>
         * This check may occur long before data is written, and does not guarantee exclusive access to the table. If
         * two programs are run concurrently, each specifying the same output table and a
         * {@link BigQueryOutputProperties.WriteOperation} of
         * {@link BigQueryOutputProperties.WriteOperation#WRITE_TO_EMPTY}, it is possible for both to succeed.
         */
        WRITE_TO_EMPTY
    }
}
