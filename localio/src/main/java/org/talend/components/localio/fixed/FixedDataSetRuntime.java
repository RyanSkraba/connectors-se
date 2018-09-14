// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.localio.fixed;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.talend.components.localio.LocalIOErrorCode;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.runtime.beam.spi.record.AvroRecord;

public class FixedDataSetRuntime {

    /**
     * The dataset instance that this runtime is configured for.
     */
    private FixedDataSetConfiguration configuration;

    private RecordBuilderFactory factory;

    // Unused -- issue with converting JSON to Record internally.
    // private JsonReaderFactory jsonpReaderFactory;

    public FixedDataSetRuntime(FixedDataSetConfiguration configuration, RecordBuilderFactory factory
    // JsonReaderFactory jsonpReaderFactory
    ) {
        this.configuration = configuration;
        this.factory = factory;
    }

    public Schema getSchema() {
        switch (configuration.getFormat()) {
        case CSV:
            // Try to get the schema from the specified value.
            String csvSchema = configuration.getCsvSchema();
            if (!csvSchema.trim().isEmpty()) {
                try {
                    CSVRecord r = CSVFormat.RFC4180 //
                            .withDelimiter(configuration.getFieldDelimiter().charAt(0)) //
                            .withRecordSeparator(configuration.getRecordDelimiter()) //
                            .parse(new StringReader(csvSchema)).iterator().next();
                    Schema.Builder sb = factory.newSchemaBuilder(Type.RECORD);
                    for (String fieldName : r)
                        sb = sb.withEntry(
                                factory.newEntryBuilder().withName(fieldName).withType(Type.STRING).withNullable(true).build());
                    return sb.build();
                } catch (Exception e) {
                    throw LocalIOErrorCode.createCannotParseSchema(e, csvSchema);
                }
            }
            // Fall back to a schema based on the number of columns.
            try {
                int maxSize = 0;
                for (CSVRecord r : CSVFormat.RFC4180 //
                        .withDelimiter(configuration.getFieldDelimiter().charAt(0)) //
                        .withRecordSeparator(configuration.getRecordDelimiter())
                        .parse(new StringReader(configuration.getValues())))
                    maxSize = Math.max(maxSize, r.size());
                if (maxSize == 0)
                    throw LocalIOErrorCode.requireAtLeastOneRecord(new RuntimeException());
                Schema.Builder sb = factory.newSchemaBuilder(Type.RECORD);
                for (int i = 0; i < maxSize; i++)
                    sb = sb.withEntry(factory.newEntryBuilder().withName("field" + i).withType(Type.STRING).build());
                return sb.build();
            } catch (IOException e) {
                throw LocalIOErrorCode.createCannotParseSchema(e, configuration.getValues());
            }
        case JSON:
            if (configuration.getValues().trim().isEmpty())
                throw LocalIOErrorCode.requireAtLeastOneRecord(new RuntimeException());
            return getValues(1).get(0).getSchema();
        case AVRO:
            // Unused -- direct translation to record.
        }
        throw LocalIOErrorCode.createCannotParseSchema(null, configuration.getSchema());
    }

    public List<Record> getValues(int limit) {
        List<Record> values = new ArrayList<>();
        switch (configuration.getFormat()) {
        case CSV:
            Schema csv = getSchema();
            try {
                for (CSVRecord r : CSVFormat.RFC4180 //
                        .withDelimiter(configuration.getFieldDelimiter().charAt(0)) //
                        .withRecordSeparator(configuration.getRecordDelimiter())
                        .parse(new StringReader(configuration.getValues()))) {
                    for (int i = 0; i < r.size(); i++) {
                        Record.Builder rb = factory.newRecordBuilder();
                        Schema.Entry e = csv.getEntries().get(i);
                        rb = rb.withString(e, r.get(i));
                        values.add(rb.build());
                    }
                }
            } catch (IOException e) {
                throw LocalIOErrorCode.createCannotParseSchema(e, configuration.getValues());
            }
            break;
        case JSON:

            // ObjectMapper mapper = new ObjectMapper();
            // JsonSchemaInferrer jsonSchemaInferrer = new JsonSchemaInferrer(mapper);
            // JsonGenericRecordConverter converter = null;
            // JsonFactory jsonFactory = new JsonFactory();
            // try (StringReader r = new StringReader(configuration.getValues())) {
            // JsonReader jsonR = jsonpReaderFactory.createReader(r);
            // JsonObject obj = jsonR.readObject();
            // RecordConverters.toRe
            //
            //
            //
            // }
            // Iterator<JsonNode> value = mapper.readValues(jsonFactory.createParser(r), JsonNode.class);
            // int count = 0;
            // while (value.hasNext() && count++ < limit) {
            // String json = value.next().toString();
            // if (converter == null) {
            // Schema jsonSchema = jsonSchemaInferrer.inferSchema(json);
            // converter = new JsonGenericRecordConverter(jsonSchema);
            // }
            // values.add(converter.convertToAvro(json));
            // }
            // } catch (IOException e) {
            // throw LocalIOErrorCode.createCannotParseJson(e, configuration.getSchema(), configuration.getValues());
            // }
            break;
        case AVRO:
            org.apache.avro.Schema schema;
            try {
                schema = new org.apache.avro.Schema.Parser().parse(configuration.getSchema());
            } catch (Exception e) {
                throw LocalIOErrorCode.createCannotParseSchema(e, configuration.getSchema());
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(configuration.getValues().trim().getBytes())) {
                JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, bais);
                DatumReader<IndexedRecord> reader = new GenericDatumReader<>(schema);
                int count = 0;
                while (count++ < limit) {
                    values.add(new AvroRecord(reader.read(null, decoder)));
                }
            } catch (EOFException e) {
                // Indicates the end of the values.
            } catch (IOException e) {
                throw LocalIOErrorCode.createCannotParseAvroJson(e, configuration.getSchema(), configuration.getValues());
            }
            break;
        }
        return values;
    }
}
