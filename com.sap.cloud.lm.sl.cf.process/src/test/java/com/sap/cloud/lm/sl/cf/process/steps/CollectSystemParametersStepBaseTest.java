package com.sap.cloud.lm.sl.cf.process.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudInfo;
import org.cloudfoundry.client.lib.domain.ImmutableCloudMetadata;
import org.junit.Before;
import org.mockito.Mock;

import com.sap.cloud.lm.sl.cf.core.helpers.CredentialsGenerator;
import com.sap.cloud.lm.sl.cf.core.util.DescriptorTestUtil;
import com.sap.cloud.lm.sl.cf.process.util.ReadOnlyParametersChecker;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.VersionRule;

public abstract class CollectSystemParametersStepBaseTest extends SyncFlowableStepTest<CollectSystemParametersStep> {

    protected static final String DEFAULT_TIMESTAMP = "19700101";
    protected static final String USER = "admin";
    protected static final String ORGANIZATION_NAME = "my-org";
    protected static final String ORGANIZATION_GUID = "1247566c-7bfd-48f3-a74e-d82711dc1180";
    protected static final String SPACE_NAME = "my-space";
    protected static final String SPACE_GUID = "98f099c0-41d4-455e-affc-b072f5b2b06f";
    protected static final String AUTHORIZATION_URL = "https://localhost:30032/uaa-security";
    protected static final String CONTROLLER_URL = "https://localhost:30030";
    protected static final String MULTIAPPS_CONTROLLER_URL = "https://localhost:50010";
    protected static final String DEFAULT_DOMAIN = "localhost";
    protected static final UUID DEFAULT_DOMAIN_GUID = UUID.fromString("7b5987e9-4325-4bb6-93e2-a0b1c562e60c");
    protected static final VersionRule VERSION_RULE = VersionRule.SAME_HIGHER;

    protected static final boolean DEFAULT_USE_NAMESPACES = false;
    protected static final boolean DEFAULT_USE_NAMESPACES_FOR_SERVICES = false;

    @Mock
    protected CredentialsGenerator credentialsGenerator;

    @Mock
    protected ReadOnlyParametersChecker readOnlyParametersChecker;

    @Before
    public void setUp() throws MalformedURLException {
        when(configuration.getControllerUrl()).thenReturn(new URL(CONTROLLER_URL));
        when(configuration.getDeployServiceUrl()).thenReturn(MULTIAPPS_CONTROLLER_URL);

        context.setVariable(Variables.USER, USER);
        context.setVariable(Variables.ORGANIZATION_NAME, ORGANIZATION_NAME);
        context.setVariable(Variables.ORGANIZATION_GUID, ORGANIZATION_GUID);
        context.setVariable(Variables.SPACE_NAME, SPACE_NAME);
        context.setVariable(Variables.SPACE_GUID, SPACE_GUID);

        context.setVariable(Variables.USE_NAMESPACES, DEFAULT_USE_NAMESPACES);
        context.setVariable(Variables.USE_NAMESPACES_FOR_SERVICES, DEFAULT_USE_NAMESPACES_FOR_SERVICES);
        context.setVariable(Variables.VERSION_RULE, VERSION_RULE);

        step.credentialsGeneratorSupplier = () -> credentialsGenerator;
        step.timestampSupplier = () -> DEFAULT_TIMESTAMP;
    }

    protected void prepareDescriptor(String descriptorPath) {
        DeploymentDescriptor descriptor = DescriptorTestUtil.loadDeploymentDescriptor(descriptorPath, getClass());
        context.setVariable(Variables.DEPLOYMENT_DESCRIPTOR, descriptor);
    }

    protected void prepareClient() {
        CloudDomain defaultDomain = mockDefaultDomain();
        CloudInfo info = mockInfo();

        when(client.getDefaultDomain()).thenReturn(defaultDomain);
        when(client.getCloudInfo()).thenReturn(info);
    }

    private CloudDomain mockDefaultDomain() {
        CloudDomain domain = mock(CloudDomain.class);
        when(domain.getName()).thenReturn(DEFAULT_DOMAIN);
        when(domain.getMetadata()).thenReturn(ImmutableCloudMetadata.builder()
                                                                    .guid(DEFAULT_DOMAIN_GUID)
                                                                    .build());
        return domain;
    }

    private CloudInfo mockInfo() {
        CloudInfo info = mock(CloudInfo.class);
        when(info.getAuthorizationEndpoint()).thenReturn(AUTHORIZATION_URL);
        return info;
    }

    @Override
    protected CollectSystemParametersStep createStep() {
        return new CollectSystemParametersStep();
    }

}
