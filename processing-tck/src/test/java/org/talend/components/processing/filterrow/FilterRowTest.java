package org.talend.components.processing.filterrow;

import static java.util.Arrays.asList;
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
class FilterRowTest {

    @Injected
    private ComponentsHandler handler;

    @Service
    private JsonBuilderFactory factory;

    @Test
    void filter() {
        handler.setInputData(asList(factory.createObjectBuilder().add("index", 1).build(),
                factory.createObjectBuilder().add("index", 2).build(), factory.createObjectBuilder().add("index", 3).build()));
        components().component("input", "test://emitter")
                .component("filter",
                        "Processing://FilterRow?configuration.filters[0].columnName=index&configuration.filters[0].value=2")
                .component("output", "test://collector").connections().from("input").to("filter").from("filter").to("output")
                .build().run();

        final List<JsonObject> values = handler.getCollectedData(JsonObject.class);
        assertEquals(1, values.size());
        assertEquals(2, values.iterator().next().getInt("index"));
    }
}
