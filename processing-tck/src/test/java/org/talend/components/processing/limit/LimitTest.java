package org.talend.components.processing.limit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.talend.sdk.component.runtime.manager.chain.Job.components;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit.ComponentsHandler;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

@WithComponents("org.talend.components.processing")
class LimitTest {

    @Injected
    private ComponentsHandler handler;

    @Service
    private JsonBuilderFactory factory;

    @Test
    void limit() {
        handler.setInputData(asList(factory.createObjectBuilder().add("index", 1).build(),
                factory.createObjectBuilder().add("index", 2).build(), factory.createObjectBuilder().add("index", 3).build()));
        components().component("input", "test://emitter").component("limit", "Processing://Limit?configuration.limit=2")
                .component("output", "test://collector").connections().from("input").to("limit").from("limit").to("output")
                .build().run();

        assertEquals(2, handler.getCollectedData(JsonObject.class).size());
    }
}
