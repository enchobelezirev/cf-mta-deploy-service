package com.sap.cloud.lm.sl.cf.web.api.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.cloud.lm.sl.common.Nullable;

import io.swagger.annotations.ApiModelProperty;

@Value.Immutable
@JsonSerialize(as = ImmutableInfo.class)
@JsonDeserialize(as = ImmutableInfo.class)
public interface Info {

    @Nullable
    @ApiModelProperty
    @JsonProperty("api_version")
    Integer getApiVersion();

}
