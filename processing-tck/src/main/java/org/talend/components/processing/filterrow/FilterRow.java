package org.talend.components.processing.filterrow;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.api.component.Icon.IconType.FILTER_ROW;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;

@Version
@Processor(name = "FilterRow")
@Icon(FILTER_ROW)
@Documentation("This component filters the input with some logical rules.")
public class FilterRow implements Serializable {

    private final FilterRowConfiguration configuration;

    private Predicate<JsonObject> predicate;

    public FilterRow(@Option("configuration") final FilterRowConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void init() {
        if (configuration.getFilters() == null || configuration.getFilters().isEmpty()) {
            predicate = e -> true;
        } else {
            final List<Predicate<JsonObject>> predicates = configuration.getFilters().stream()
                    .filter(c -> c.getColumnName() != null && !c.getColumnName().isEmpty()).map(criteria -> {
                        final FilterRowConfiguration.Transformer transformer = ofNullable(criteria.getFunction())
                                .orElse(FilterRowConfiguration.Transformer.EMPTY);
                        final FilterRowConfiguration.Operator operator = ofNullable(criteria.getOperator())
                                .orElse(FilterRowConfiguration.Operator.EQUAL);
                        final String columnName = criteria.getColumnName();
                        final Object value = operator.isNumber() ? Double.parseDouble(criteria.getValue()) : criteria.getValue();
                        return (Predicate<JsonObject>) input -> operator.test(transformer.apply(input, columnName), value);
                    }).collect(toList());
            if (predicates.isEmpty() || configuration.getLogicalOpType() == null) {
                predicate = e -> true;
            } else {
                predicate = configuration.getLogicalOpType().apply(predicates);
            }
        }
    }

    @ElementListener
    public void onElement(final JsonObject element, @Output final OutputEmitter<JsonObject> output,
            @Output("reject") final OutputEmitter<JsonObject> reject) {
        if (predicate.test(element)) {
            output.emit(element);
        } else { // runtime is allowed to provide a mock here if there is no "design" connection but it can't provide "null"
            reject.emit(element);
        }
    }
}
