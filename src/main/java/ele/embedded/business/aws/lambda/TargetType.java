package ele.embedded.business.aws.lambda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TargetType {
    THING,
    GROUP;

    @JsonValue
    public String toLower() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static TargetType fromString(String value) {
        return value == null ? THING : TargetType.valueOf(value.toUpperCase());
    }
}