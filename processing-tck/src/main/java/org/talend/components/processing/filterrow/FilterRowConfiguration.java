package org.talend.components.processing.filterrow;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
@OptionsOrder({ "filters", "logicalOpType" })
@Documentation("A set of filter and a way to combine them to filter data.")
public class FilterRowConfiguration implements Serializable {

    @Option
    @Documentation("How to combine filters")
    private LogicalOpType logicalOpType = LogicalOpType.ALL;

    @Option
    @Documentation("The list of filters to apply")
    private List<Criteria> filters;

    @Data
    @OptionsOrder({ "columnName", "function", "operator", "value" })
    @Documentation("An unitary filter.")
    public static class Criteria {

        @Option
        @Required
        @Documentation("The column name to use for this criteria")
        private String columnName;

        @Option
        @Documentation("The function to apply on the column")
        private Transformer function = Transformer.EMPTY;

        @Option
        @Documentation("The operator")
        private Operator operator = Operator.EQUAL;

        @Option
        @Required
        @Documentation("The value to compare to")
        private String value = "";
    }

    public enum Operator implements BiPredicate<Object, Object> {
        EQUAL {

            @Override
            public boolean test(final Object o, final Object o2) {
                if (Number.class.isInstance(o) && String.class.isInstance(o2)) {
                    return Number.class.cast(o).doubleValue() == Double.parseDouble(String.valueOf(o2));
                }
                return Objects.equals(String.valueOf(o), o2);
            }
        },
        NOT_EQUAL {

            @Override
            public boolean test(final Object o, final Object o2) {
                return !EQUAL.test(o, o2);
            }
        },
        LOWER {

            @Override
            public boolean isNumber() {
                return true;
            }

            @Override
            public boolean test(final Object o, final Object o2) {
                return Number.class.isInstance(o) && Number.class.isInstance(o2)
                        && Number.class.cast(o).doubleValue() < Number.class.cast(o2).doubleValue();
            }
        },
        LOWER_OR_EQUAL {

            @Override
            public boolean isNumber() {
                return true;
            }

            @Override
            public boolean test(final Object o, final Object o2) {
                return !GREATER.test(o, o2);
            }
        },
        GREATER {

            @Override
            public boolean isNumber() {
                return true;
            }

            @Override
            public boolean test(final Object o, final Object o2) {
                return Number.class.isInstance(o) && Number.class.isInstance(o2)
                        && Number.class.cast(o).doubleValue() > Number.class.cast(o2).doubleValue();
            }
        },
        GREATER_OR_EQUAL {

            @Override
            public boolean isNumber() {
                return true;
            }

            @Override
            public boolean test(final Object o, final Object o2) {
                return !LOWER.test(o, o2);
            }
        },
        MATCH {

            @Override
            public boolean test(final Object o, final Object o2) {
                return String.class.isInstance(o) && CharSequence.class.isInstance(o2)
                        && String.class.cast(o).matches(String.valueOf(o2));
            }
        },
        NOT_MATCH {

            @Override
            public boolean test(final Object o, final Object o2) {
                return !MATCH.test(o, o2);
            }
        },
        CONTAINS {

            @Override
            public boolean test(final Object o, final Object o2) {
                return String.class.isInstance(o) && CharSequence.class.isInstance(o2)
                        && String.class.cast(o).contains(String.valueOf(o2));
            }
        },
        NOT_CONTAINS {

            @Override
            public boolean test(final Object o, final Object o2) {
                return !CONTAINS.test(o, o2);
            }
        };

        public boolean isNumber() {
            return false;
        }
    }

    public enum Transformer implements BiFunction<JsonObject, String, Object> {
        EMPTY {

            @Override
            public Object apply(final JsonObject value, final String column) {
                final JsonValue jsonValue = value.get(column);
                if (jsonValue == null) {
                    return null;
                }
                switch (jsonValue.getValueType()) {
                case STRING:
                    return JsonString.class.cast(jsonValue).getString();
                case NUMBER:
                    return JsonNumber.class.cast(jsonValue).doubleValue();
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                case NULL:
                    return null;
                case OBJECT:
                case ARRAY:
                default:
                    return jsonValue;
                }
            }
        },
        ABS_VALUE {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return Number.class.isInstance(val) ? Math.abs(Number.class.cast(val).doubleValue()) : value;
            }
        },
        LOWER_CASE {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return value == null ? null : String.valueOf(value).toLowerCase();
            }
        },
        UPPER_CASE {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return value == null ? null : String.valueOf(value).toUpperCase();
            }
        },
        FIRST_CHARACTER_LOWER_CASE {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return value == null ? null
                        : String.valueOf(value).chars().limit(1).mapToObj(i -> Character.toLowerCase((char) i)).findFirst()
                                .orElse(null);
            }
        },
        FIRST_CHARACTER_UPPER_CASE {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return value == null ? null
                        : String.valueOf(value).chars().limit(1).mapToObj(i -> Character.toUpperCase((char) i)).findFirst()
                                .orElse(null);
            }
        },
        LENGTH {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return value == null ? 0 : String.valueOf(value).length();
            }
        },
        COUNT {

            @Override
            public Object apply(final JsonObject val, final String column) {
                final Object value = EMPTY.apply(val, column);
                return value == null ? 0
                        : (Collection.class.isInstance(value) ? Collection.class.cast(value).size()
                                : value.getClass().isArray() ? Array.getLength(value)
                                        : Map.class.isInstance(value) ? Map.class.cast(value).size() : 0);
            }
        }
    }

    public enum LogicalOpType implements Function<List<Predicate<JsonObject>>, Predicate<JsonObject>> {
        /**
         * All criteria must be true for the record to be selected (AND).
         */
        ALL {

            @Override
            public Predicate<JsonObject> apply(final List<Predicate<JsonObject>> predicates) {
                return json -> predicates.stream().allMatch(it -> it.test(json));
            }
        },
        /**
         * At least one criteria must be true for the record to be selected (OR).
         */
        ANY {

            @Override
            public Predicate<JsonObject> apply(final List<Predicate<JsonObject>> predicates) {
                return json -> predicates.stream().anyMatch(it -> it.test(json));
            }
        },
        /**
         * No criteria can be true for the record to be selected (NOR).
         */
        NONE {

            @Override
            public Predicate<JsonObject> apply(final List<Predicate<JsonObject>> predicates) {
                return json -> predicates.stream().noneMatch(it -> it.test(json));
            }
        }
    }
}
