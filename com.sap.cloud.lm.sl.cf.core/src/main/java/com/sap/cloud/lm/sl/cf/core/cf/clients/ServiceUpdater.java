package com.sap.cloud.lm.sl.cf.core.cf.clients;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import com.sap.cloud.lm.sl.common.util.MapUtil;
import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.client.lib.domain.CloudServicePlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.lm.sl.cf.core.util.MethodExecution;
import com.sap.cloud.lm.sl.cf.core.util.MethodExecution.ExecutionState;

@Named
public class ServiceUpdater extends CloudServiceOperator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdater.class);

    private static final String USER_PROVIDED_SERVICE_INSTANCES_URL = "/v2/user_provided_service_instances";
    private static final String SERVICE_CREDENTIALS = "credentials";
    private static final String ACCEPTS_INCOMPLETE_TRUE = "?accepts_incomplete=true";
    private static final String SERVICE_PARAMETERS = "parameters";

    @Inject
    public ServiceUpdater(RestTemplateFactory restTemplateFactory) {
        super(restTemplateFactory);
    }

    public MethodExecution<String> updateServicePlanQuietly(CloudControllerClient client, String serviceName, String servicePlan) {
        return ignoreBadGatewayErrors(() -> updateServicePlan(client, serviceName, servicePlan));
    }

    public MethodExecution<String> updateServicePlan(CloudControllerClient client, String serviceName, String servicePlan) {
        return new CustomControllerClientErrorHandler().handleErrorsOrReturnResult(() -> attemptToUpdateServicePlan(client, serviceName,
                                                                                                                    servicePlan));
    }

    public MethodExecution<String> updateServiceTagsQuietly(CloudControllerClient client, String serviceName, List<String> serviceTags) {
        return ignoreBadGatewayErrors(() -> updateServiceTags(client, serviceName, serviceTags));
    }

    public MethodExecution<String> updateServiceTags(CloudControllerClient client, String serviceName, List<String> serviceTags) {
        return new CustomControllerClientErrorHandler().handleErrorsOrReturnResult(() -> attemptToUpdateServiceTags(client, serviceName,
                                                                                                                    serviceTags));
    }

    public MethodExecution<String> updateServiceParametersQuietly(CloudControllerClient client, String serviceName,
                                                                  Map<String, Object> parameters) {
        return ignoreBadGatewayErrors(() -> updateServiceParameters(client, serviceName, parameters));
    }

    public MethodExecution<String> updateServiceParameters(CloudControllerClient client, String serviceName,
                                                           Map<String, Object> parameters) {
        return new CustomControllerClientErrorHandler().handleErrorsOrReturnResult(() -> attemptToUpdateServiceParameters(client,
                                                                                                                          serviceName,
                                                                                                                          parameters));
    }

    private MethodExecution<String> attemptToUpdateServicePlan(CloudControllerClient client, String serviceInstanceName,
                                                               String servicePlanName) {
        CloudServiceInstance serviceInstance = client.getServiceInstance(serviceInstanceName);
        CloudServicePlan servicePlan = findPlanForService(client, serviceInstance, servicePlanName);
        String servicePlanGuid = servicePlan.getMetadata()
                                            .getGuid()
                                            .toString();
        return attemptToUpdateServiceParameter(client, serviceInstance, SERVICE_INSTANCES_URL, SERVICE_PLAN_GUID, servicePlanGuid);
    }

    private MethodExecution<String> attemptToUpdateServiceTags(CloudControllerClient client, String serviceInstanceName,
                                                               List<String> serviceTags) {
        CloudServiceInstance serviceInstance = client.getServiceInstance(serviceInstanceName);
        return attemptToUpdateServiceParameter(client, serviceInstance, SERVICE_INSTANCES_URL, SERVICE_TAGS, serviceTags);
    }

    private String getCloudControllerUrl(CloudControllerClient client) {
        return client.getCloudControllerUrl()
                     .toString();
    }

    private MethodExecution<String> attemptToUpdateServiceParameters(CloudControllerClient client, String serviceName,
                                                                     Map<String, Object> parameters) {
        assertServiceAttributes(serviceName, parameters);
        CloudServiceInstance serviceInstance = client.getServiceInstance(serviceName);

        if (serviceInstance.isUserProvided()) {
            return attemptToUpdateServiceParameter(client, serviceInstance, USER_PROVIDED_SERVICE_INSTANCES_URL, SERVICE_CREDENTIALS,
                                                   parameters);
        }

        return attemptToUpdateServiceParameter(client, serviceInstance, SERVICE_INSTANCES_URL, SERVICE_PARAMETERS, parameters);
    }

    private MethodExecution<String> attemptToUpdateServiceParameter(CloudControllerClient client, CloudServiceInstance serviceInstance,
                                                                    String serviceUrl, String parameterName, Object parameter) {
        RestTemplate restTemplate = getRestTemplate(client);
        String cloudControllerUrl = getCloudControllerUrl(client);
        String updateServiceUrl = getUrl(cloudControllerUrl, getUpdateServiceUrl(serviceUrl, serviceInstance.getMetadata()
                                                                                                            .getGuid()
                                                                                                            .toString()));

        Map<String, Object> serviceRequest = MapUtil.asMap(parameterName, parameter);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(serviceRequest);
        ResponseEntity<String> response = restTemplate.exchange(updateServiceUrl, HttpMethod.PUT, requestEntity, String.class);
        return MethodExecution.fromResponseEntity(response);
    }

    private String getUpdateServiceUrl(String serviceUrl, String serviceGuid) {
        return serviceUrl + "/" + serviceGuid + ServiceUpdater.ACCEPTS_INCOMPLETE_TRUE;
    }

    private void assertServiceAttributes(String serviceName, Object parameters) {
        Assert.notNull(serviceName, "Service name must not be null");
        Assert.notNull(parameters, "Service parameters must not be null");
    }

    private <T> T ignoreBadGatewayErrors(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (CloudOperationException e) {
            if (!e.getStatusCode()
                  .equals(HttpStatus.BAD_GATEWAY)) {
                throw e;
            }
            LOGGER.warn(MessageFormat.format("Controller operation failed. Status Code: {0}, Status Text: {1}, Description: {2}",
                                             e.getStatusCode(), e.getStatusText(), e.getDescription()),
                        e);
            return (T) new MethodExecution<String>(null, ExecutionState.FINISHED);
        }
    }

}
