package org.talend.components.processing.fieldselector;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.api.component.Icon.IconType.FIELD_SELECTOR;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPointer;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Processor;

@Version
@Icon(FIELD_SELECTOR)
@Processor(name = "FieldSelector")
@Documentation("This component remap the input to create a new record.")
public class FieldSelector implements Serializable {

    private final FieldSelectorConfiguration configuration;

    private final JsonBuilderFactory factory;

    private final JsonProvider provider;

    private transient List<BiConsumer<JsonObject, JsonObjectBuilder>> builders;

    public FieldSelector(@Option("configuration") final FieldSelectorConfiguration configuration,
            final JsonBuilderFactory factory, final JsonProvider provider) {
        this.configuration = configuration;
        this.factory = factory;
        this.provider = provider;
    }

    @ElementListener
    public JsonObject map(final JsonObject object) {
        if (builders == null) {
            builders = ofNullable(configuration.getSelectors()).orElseGet(Collections::emptyList).stream()
                    .filter(it -> it.getField() != null && !it.getField().isEmpty() && it.getPath() != null).map(selector -> {
                        final String field = selector.getField();
                        final JsonPointer pointer = provider.createPointer(selector.getPath());
                        return (BiConsumer<JsonObject, JsonObjectBuilder>) (obj, builder) -> {
                            try {
                                builder.add(field, pointer.getValue(obj));
                            } catch (final JsonException je) {
                                builder.add(field, JsonValue.NULL);
                            }
                        };
                    }).collect(toList());
        }

        final JsonObjectBuilder builder = factory.createObjectBuilder();
        builders.forEach(c -> c.accept(object, builder));
        return builder.build();
    }
}
