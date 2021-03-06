package com.sap.cloud.lm.sl.cf.web.api.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.cloud.lm.sl.cf.web.api.model.parameters.ParameterConverter;
import com.sap.cloud.lm.sl.common.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableParameterMetadata.class)
@JsonDeserialize(as = ImmutableParameterMetadata.class)
public interface ParameterMetadata {

    String getId();

    @Nullable
    Object getDefaultValue();

    @Value.Default
    default boolean getRequired() {
        return false;
    }

    ParameterType getType();

    @Nullable
    ParameterConverter getCustomConverter();

}
