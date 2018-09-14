package org.talend.components.processing.limit;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

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
@Processor(name = "Limit")
@Icon(custom = "limit", value = CUSTOM)
@Documentation("This component filters the input with a counter/limit.")
public class Limit implements Serializable {

    private final LimitConfiguration configuration;

    private volatile AtomicLong counter;

    public Limit(@Option("configuration") final LimitConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void init() {
        counter = new AtomicLong(configuration.getLimit());
    }

    @ElementListener
    public void onElement(final JsonObject element, @Output final OutputEmitter<JsonObject> output) {
        if (counter.getAndDecrement() > 0) {
            output.emit(element);
        }
    }
}
