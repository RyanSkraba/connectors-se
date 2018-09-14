package org.talend.components.processing.fieldselector;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.talend.sdk.component.runtime.manager.chain.Job.components;

import java.util.List;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit.ComponentsHandler;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

@WithComponents("org.talend.components.processing")
class FieldSelectorTest {

    @Injected
    private ComponentsHandler handler;

    @Service
    private JsonBuilderFactory factory;

    @Test
    void map() {
        handler.setInputData(asList(factory.createObjectBuilder().add("index", 1).build(),
                factory.createObjectBuilder().add("index", 2).build(), factory.createObjectBuilder().add("index", 3).build()));
        components().component("input", "test://emitter").component("filter",
                "Processing://FieldSelector?configuration.selectors[0].field=remapped&configuration.selectors[0].path=/index")
                .component("output", "test://collector").connections().from("input").to("filter").from("filter").to("output")
                .build().run();

        final List<JsonObject> values = handler.getCollectedData(JsonObject.class);
        assertEquals(asList(1, 2, 3), values.stream().map(json -> json.getInt("remapped")).collect(toList()));
    }
}
