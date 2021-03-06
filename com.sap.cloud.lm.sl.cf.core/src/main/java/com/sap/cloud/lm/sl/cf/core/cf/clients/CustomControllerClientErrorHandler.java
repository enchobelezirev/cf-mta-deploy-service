package com.sap.cloud.lm.sl.cf.core.cf.clients;

import java.text.MessageFormat;
import java.util.Map;
import java.util.function.Supplier;

import org.cloudfoundry.client.lib.CloudOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import com.sap.cloud.lm.sl.cf.client.util.ResilientCloudOperationExecutor;
import com.sap.cloud.lm.sl.common.ParsingException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

public class CustomControllerClientErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomControllerClientErrorHandler.class);
    private Supplier<ResilientCloudOperationExecutor> executorFactory = ResilientCloudOperationExecutor::new;

    CustomControllerClientErrorHandler withExecutorFactory(Supplier<ResilientCloudOperationExecutor> executorFactory) {
        this.executorFactory = executorFactory;
        return this;
    }

    public void handleErrors(Runnable runnable) {
        handleErrorsOrReturnResult(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> T handleErrorsOrReturnResult(Supplier<T> supplier, HttpStatus... statusesToIgnore) {
        return createExecutor(statusesToIgnore).execute(() -> {
            try {
                return supplier.get();
            } catch (HttpStatusCodeException e) {
                throw asCloudOperationException(e);
            }
        });
    }

    protected ResilientCloudOperationExecutor createExecutor(HttpStatus... statusesToIgnore) {
        return executorFactory.get()
                              .withStatusesToIgnore(statusesToIgnore);
    }

    private CloudOperationException asCloudOperationException(HttpStatusCodeException exception) {
        String description = getDescriptionFromResponseBody(exception.getResponseBodyAsString());
        return new CloudOperationException(exception.getStatusCode(), exception.getStatusText(), description);
    }

    private String getDescriptionFromResponseBody(String responseBody) {
        try {
            return attemptToParseDescriptionFromResponseBody(responseBody);
        } catch (ParsingException e) {
            LOGGER.warn(MessageFormat.format("Could not parse description from response body: {0}", responseBody), e);
        }
        return null;
    }

    private String attemptToParseDescriptionFromResponseBody(String responseBody) {
        Map<String, Object> responseEntity = JsonUtil.convertJsonToMap(responseBody);
        Object result = responseEntity.get("description");
        return result instanceof String ? (String) result : null;
    }

}
