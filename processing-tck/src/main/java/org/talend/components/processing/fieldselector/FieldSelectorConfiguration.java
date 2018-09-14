package org.talend.components.processing.fieldselector;

import java.io.Serializable;
import java.util.List;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@OptionsOrder("selectors")
@Documentation("Allow to extract only a subpart of the fields.")
public class FieldSelectorConfiguration implements Serializable {

    @Option
    @Documentation("The list of columns to extract to create the new record.")
    private List<Selector> selectors;

    @Data
    @OptionsOrder({ "field", "path" })
    @Documentation("Identifies a field.")
    public static class Selector {

        @Option
        @Required
        @Documentation("The targer field name")
        private String field = "";

        @Option
        @Required
        @Documentation("The extraction path")
        private String path = "";
    }
}
