package com.sap.cloud.lm.sl.cf.process.steps;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.cloudfoundry.client.lib.ApplicationServicesUpdateCallback;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.cloudfoundry.client.lib.domain.ImmutableCloudApplication;
import org.cloudfoundry.client.lib.domain.ImmutableCloudMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.common.util.GenericArgumentMatcher;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil;

@RunWith(Parameterized.class)
public class CreateOrUpdateAppStepTest extends CreateOrUpdateAppStepBaseTest {

    private final String expectedExceptionMessage;
    private final ApplicationServicesUpdateCallback callback;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
            // (0) Disk quota is 0:
            {
                "create-app-step-input-00.json", null,
            },
            // (1) Memory is 0:
            {
                "create-app-step-input-01.json", null,
            },
            // (2) Everything is specified properly:
            {
                "create-app-step-input-02.json", null,
            },
            // (3) Binding parameters exist, and the services do too:
            {
                "create-app-step-input-03.json", null,
            },
            // (4) Binding parameters exist, but the services do not:
            {
                "create-app-step-input-04.json", "Could not bind application \"application\" to service \"service-2\": 500 Internal Server Error: Something happened!",
            },
            // (5) Binding parameters exist, but the services do not and service-2 is optional - so no exception should be thrown:
            {
                "create-app-step-input-05.json", null,
            },
            // (6) Service keys to inject are specified:
            {
                "create-app-step-input-06.json", null,
            },
            // (7) Service keys to inject are specified but not exist:
            {
                "create-app-step-input-07.json",
                "Unable to retrieve required service key element \"expected-service-key\" for service \"existing-service\"",
            },
// @formatter:on
        });
    }

    public CreateOrUpdateAppStepTest(String stepInput, String expectedExceptionMessage) {
        this.stepInput = JsonUtil.fromJson(TestUtil.getResourceAsString(stepInput, CreateOrUpdateAppStepTest.class), StepInput.class);
        this.expectedExceptionMessage = expectedExceptionMessage;
        this.callback = (e, applicationName, serviceName) -> {
            if (expectedExceptionMessage != null) {
                throw new RuntimeException(expectedExceptionMessage, e);
            }
        };
    }

    @Before
    public void setUp() {
        step.shouldPrettyPrint = () -> false;
        loadParameters();
        prepareContext();
        prepareClient();
    }

    @Test
    public void testExecute() {
        step.execute(execution);

        assertStepFinishedSuccessfully();

        validateClient();
    }

    private void loadParameters() {
        if (expectedExceptionMessage != null) {
            expectedException.expectMessage(expectedExceptionMessage);
        }
        application = stepInput.applications.get(stepInput.applicationIndex);
        application = ImmutableCloudApplicationExtended.builder()
                                                       .from(application)
                                                       .moduleName("test")
                                                       .build();
    }

    private void prepareContext() {
        // TODO
        context.setVariable(Variables.APPS_TO_DEPLOY, Collections.emptyList());
        StepsTestUtil.mockApplicationsToDeploy(stepInput.applications, execution);
        context.setVariable(Variables.SERVICES_TO_BIND, mapToCloudServiceExtended());
        context.setVariable(Variables.APP_ARCHIVE_ID, "dummy");
        context.setVariable(Variables.MODULES_INDEX, stepInput.applicationIndex);
        context.setVariable(Variables.SERVICE_KEYS_CREDENTIALS_TO_INJECT, new HashMap<>());
    }

    private List<CloudServiceInstanceExtended> mapToCloudServiceExtended() {
        return application.getServices()
                          .stream()
                          .map(this::extracted)
                          .collect(Collectors.toList());
    }

    private CloudServiceInstanceExtended extracted(String serviceName) {
        for (SimpleService simpleService : stepInput.services) {
            if (simpleService.name.equals(serviceName)) {
                return simpleService.toCloudServiceExtended();
            }
        }
        return ImmutableCloudServiceInstanceExtended.builder()
                                                    .name(serviceName)
                                                    .build();
    }

    private void prepareClient() {
        for (CloudApplicationExtended cloudApplicationExtended : stepInput.applications) {
            Mockito.doReturn(ImmutableCloudApplication.builder()
                                                      .metadata(ImmutableCloudMetadata.builder()
                                                                                      .guid(UUID.randomUUID())
                                                                                      .build())
                                                      .build())
                   .when(client)
                   .getApplication(cloudApplicationExtended.getName());
        }

        for (SimpleService simpleService : stepInput.services) {
            CloudServiceInstanceExtended service = simpleService.toCloudServiceExtended();
            if (!service.isOptional()) {
                Mockito.when(client.getServiceInstance(service.getName()))
                       .thenReturn(service);
            }
        }

        for (Map.Entry<String, String> entry : stepInput.bindingErrors.entrySet()) {
            String serviceName = entry.getValue();
            Mockito.doThrow(new CloudOperationException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                        expectedExceptionMessage + "Something happened!"))
                   .when(client)
                   .bindServiceInstance(eq(entry.getKey()), eq(serviceName), Mockito.any(), Mockito.any());
        }

        for (Map.Entry<String, List<CloudServiceKey>> entry : stepInput.existingServiceKeys.entrySet()) {
            List<CloudServiceKey> serviceKeys = entry.getValue();
            Mockito.when(client.getServiceKeys(eq(entry.getKey())))
                   .thenReturn(serviceKeys);
        }
    }

    private void validateClient() {
        Integer diskQuota = (application.getDiskQuota() != 0) ? application.getDiskQuota() : null;
        Integer memory = (application.getMemory() != 0) ? application.getMemory() : null;

        Mockito.verify(client)
               .createApplication(eq(application.getName()), argThat(GenericArgumentMatcher.forObject(application.getStaging())),
                                  eq(diskQuota), eq(memory), eq(application.getUris()), eq(null));
        for (String service : application.getServices()) {
            if (!isOptional(service)) {
                Mockito.verify(client)
                       .bindServiceInstance(application.getName(), service,
                                            getBindingParametersForService(application.getBindingParameters(), service),
                                            step.getApplicationServicesUpdateCallback(context));
            }
        }
        Mockito.verify(client)
               .updateApplicationEnv(application.getName(), application.getEnv());
    }

    private Map<String, Object> getBindingParametersForService(Map<String, Map<String, Object>> bindingParameters, String serviceName) {
        return bindingParameters == null ? Collections.emptyMap() : bindingParameters.getOrDefault(serviceName, Collections.emptyMap());
    }

    private boolean isOptional(String service) {
        for (SimpleService simpleService : stepInput.services) {
            if (simpleService.name.equals(service)) {
                return simpleService.isOptional;
            }
        }
        return false;
    }

    @Override
    protected CreateOrUpdateAppStep createStep() {
        return new CreateAppStepMock();
    }

    private class CreateAppStepMock extends CreateOrUpdateAppStep {
        @Override
        protected ApplicationServicesUpdateCallback getApplicationServicesUpdateCallback(ProcessContext context) {
            return callback;
        }
    }
}
