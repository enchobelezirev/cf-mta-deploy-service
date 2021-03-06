package com.sap.cloud.lm.sl.cf.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.cloudfoundry.client.lib.ApplicationServicesUpdateCallback;
import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudControllerClientImpl;
import org.cloudfoundry.client.lib.RestLogCallback;
import org.cloudfoundry.client.lib.StartingInfo;
import org.cloudfoundry.client.lib.UploadStatusCallback;
import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudBuild;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudEvent;
import org.cloudfoundry.client.lib.domain.CloudInfo;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudRoute;
import org.cloudfoundry.client.lib.domain.CloudServiceBinding;
import org.cloudfoundry.client.lib.domain.CloudServiceBroker;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.CloudStack;
import org.cloudfoundry.client.lib.domain.CloudTask;
import org.cloudfoundry.client.lib.domain.DockerInfo;
import org.cloudfoundry.client.lib.domain.InstancesInfo;
import org.cloudfoundry.client.lib.domain.Staging;
import org.cloudfoundry.client.lib.domain.Upload;
import org.cloudfoundry.client.lib.domain.UploadToken;
import org.cloudfoundry.client.lib.rest.CloudControllerRestClient;
import org.cloudfoundry.client.v3.Metadata;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.ResponseErrorHandler;

import com.sap.cloud.lm.sl.cf.client.util.ResilientCloudOperationExecutor;

public class ResilientCloudControllerClient implements CloudControllerClient {

    private final CloudControllerClientImpl delegate;

    public ResilientCloudControllerClient(CloudControllerRestClient delegate) {
        this.delegate = new CloudControllerClientImpl(delegate);
    }

    @Override
    public void createServiceInstance(CloudServiceInstance serviceInstance) {
        executeWithRetry(() -> delegate.createServiceInstance(serviceInstance));
    }

    @Override
    public void addDomain(String domainName) {
        executeWithRetry(() -> delegate.addDomain(domainName));
    }

    @Override
    public void addRoute(String host, String domainName, String path) {
        executeWithRetry(() -> delegate.addRoute(host, domainName, path));
    }

    @Override
    public void bindServiceInstance(String applicationName, String serviceInstanceName) {
        executeWithRetry(() -> delegate.bindServiceInstance(applicationName, serviceInstanceName));
    }

    @Override
    public void bindServiceInstance(String applicationName, String serviceInstanceName, Map<String, Object> parameters,
                                    ApplicationServicesUpdateCallback applicationServicesUpdateCallback) {
        executeWithRetry(() -> delegate.bindServiceInstance(applicationName, serviceInstanceName, parameters,
                                                            applicationServicesUpdateCallback));
    }

    @Override
    public void createApplication(String applicationName, Staging staging, Integer disk, Integer memory, List<String> uris,
                                  DockerInfo dockerInfo) {
        executeWithRetry(() -> delegate.createApplication(applicationName, staging, disk, memory, uris, dockerInfo));
    }

    @Override
    public void createServiceBroker(CloudServiceBroker serviceBroker) {
        executeWithRetry(() -> delegate.createServiceBroker(serviceBroker));
    }

    @Override
    public void createUserProvidedServiceInstance(CloudServiceInstance serviceInstance, Map<String, Object> credentials) {
        executeWithRetry(() -> delegate.createUserProvidedServiceInstance(serviceInstance, credentials));
    }

    @Override
    public void deleteApplication(String applicationName) {
        executeWithRetry(() -> delegate.deleteApplication(applicationName));
    }

    @Override
    public void deleteDomain(String domainName) {
        executeWithRetry(() -> delegate.deleteDomain(domainName));
    }

    @Override
    public List<CloudRoute> deleteOrphanedRoutes() {
        return executeWithRetry(delegate::deleteOrphanedRoutes, HttpStatus.NOT_FOUND);
    }

    @Override
    public void deleteRoute(String host, String domainName, String path) {
        executeWithRetry(() -> delegate.deleteRoute(host, domainName, path));
    }

    @Override
    public void deleteServiceInstance(String serviceInstanceName) {
        executeWithRetry(() -> delegate.deleteServiceInstance(serviceInstanceName));
    }

    @Override
    public void deleteServiceInstance(CloudServiceInstance serviceInstance) {
        executeWithRetry(() -> delegate.deleteServiceInstance(serviceInstance));
    }

    @Override
    public void deleteServiceBroker(String name) {
        executeWithRetry(() -> delegate.deleteServiceBroker(name));
    }

    @Override
    public CloudApplication getApplication(String applicationName) {
        return executeWithRetry(() -> delegate.getApplication(applicationName));
    }

    @Override
    public CloudApplication getApplication(String applicationName, boolean required) {
        return executeWithRetry(() -> delegate.getApplication(applicationName, required));
    }

    @Override
    public CloudApplication getApplication(UUID appGuid) {
        return executeWithRetry(() -> delegate.getApplication(appGuid));
    }

    @Override
    public InstancesInfo getApplicationInstances(String applicationName) {
        return executeWithRetry(() -> delegate.getApplicationInstances(applicationName));
    }

    @Override
    public InstancesInfo getApplicationInstances(CloudApplication application) {
        return executeWithRetry(() -> delegate.getApplicationInstances(application));
    }

    @Override
    public List<CloudApplication> getApplications() {
        return executeWithRetry(() -> delegate.getApplications(), HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudDomain getDefaultDomain() {
        return executeWithRetry(delegate::getDefaultDomain);
    }

    @Override
    public List<CloudDomain> getDomains() {
        return executeWithRetry(delegate::getDomains, HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudDomain> getDomainsForOrganization() {
        return executeWithRetry(delegate::getDomainsForOrganization, HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudOrganization getOrganization(String organizationName) {
        return executeWithRetry(() -> delegate.getOrganization(organizationName));
    }

    @Override
    public CloudOrganization getOrganization(String organizationName, boolean required) {
        return executeWithRetry(() -> delegate.getOrganization(organizationName, required));
    }

    @Override
    public List<CloudDomain> getPrivateDomains() {
        return executeWithRetry(delegate::getPrivateDomains, HttpStatus.NOT_FOUND);
    }

    @Override
    public List<ApplicationLog> getRecentLogs(String applicationName) {
        return executeWithRetry(() -> delegate.getRecentLogs(applicationName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<ApplicationLog> getRecentLogs(UUID applicationGuid) {
        return executeWithRetry(() -> delegate.getRecentLogs(applicationGuid), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudRoute> getRoutes(String domainName) {
        return executeWithRetry(() -> delegate.getRoutes(domainName), HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name) {
        return executeWithRetry(() -> delegate.getServiceBroker(name));
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name, boolean required) {
        return executeWithRetry(() -> delegate.getServiceBroker(name, required));
    }

    @Override
    public List<CloudServiceBroker> getServiceBrokers() {
        return executeWithRetry(delegate::getServiceBrokers, HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudServiceInstance getServiceInstance(String serviceInstanceName) {
        return executeWithRetry(() -> delegate.getServiceInstance(serviceInstanceName));
    }

    @Override
    public CloudServiceInstance getServiceInstance(String serviceInstanceName, boolean required) {
        return executeWithRetry(() -> delegate.getServiceInstance(serviceInstanceName, required));
    }

    @Override
    public List<CloudServiceBinding> getServiceBindings(UUID serviceInstanceGuid) {
        return executeWithRetry(() -> delegate.getServiceBindings(serviceInstanceGuid));
    }

    @Override
    public Map<String, Object> getServiceInstanceParameters(UUID guid) {
        return executeWithRetry(() -> delegate.getServiceInstanceParameters(guid));
    }

    @Override
    public Map<String, Object> getServiceBindingParameters(UUID guid) {
        return executeWithRetry(() -> delegate.getServiceBindingParameters(guid));
    }

    @Override
    public List<CloudDomain> getSharedDomains() {
        return executeWithRetry(delegate::getSharedDomains, HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudSpace getSpace(UUID spaceGuid) {
        return executeWithRetry(() -> delegate.getSpace(spaceGuid));
    }

    @Override
    public CloudSpace getSpace(String organizationName, String spaceName) {
        return executeWithRetry(() -> delegate.getSpace(organizationName, spaceName));
    }

    @Override
    public CloudSpace getSpace(String organizationName, String spaceName, boolean required) {
        return executeWithRetry(() -> delegate.getSpace(organizationName, spaceName, required));
    }

    @Override
    public CloudSpace getSpace(String spaceName) {
        return executeWithRetry(() -> delegate.getSpace(spaceName));
    }

    @Override
    public CloudSpace getSpace(String spaceName, boolean required) {
        return executeWithRetry(() -> delegate.getSpace(spaceName, required));
    }

    @Override
    public List<CloudSpace> getSpaces() {
        return executeWithRetry(() -> delegate.getSpaces(), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudSpace> getSpaces(String organizationName) {
        return executeWithRetry(() -> delegate.getSpaces(organizationName), HttpStatus.NOT_FOUND);
    }

    @Override
    public void rename(String oldApplicationName, String newApplicationName) {
        executeWithRetry(() -> delegate.rename(oldApplicationName, newApplicationName));
    }

    @Override
    public StartingInfo restartApplication(String applicationName) {
        return executeWithRetry(() -> delegate.restartApplication(applicationName));
    }

    @Override
    public StartingInfo startApplication(String applicationName) {
        return executeWithRetry(() -> delegate.startApplication(applicationName));
    }

    @Override
    public void stopApplication(String applicationName) {
        executeWithRetry(() -> delegate.stopApplication(applicationName));
    }

    @Override
    public void unbindServiceInstance(String applicationName, String serviceInstanceName) {
        executeWithRetry(() -> delegate.unbindServiceInstance(applicationName, serviceInstanceName));
    }

    @Override
    public void unbindServiceInstance(CloudApplication application, CloudServiceInstance serviceInstance) {
        executeWithRetry(() -> delegate.unbindServiceInstance(application, serviceInstance));
    }

    @Override
    public void updateApplicationDiskQuota(String applicationName, int disk) {
        executeWithRetry(() -> delegate.updateApplicationDiskQuota(applicationName, disk));
    }

    @Override
    public void updateApplicationEnv(String applicationName, Map<String, String> env) {
        executeWithRetry(() -> delegate.updateApplicationEnv(applicationName, env));
    }

    @Override
    public void updateApplicationInstances(String applicationName, int instances) {
        executeWithRetry(() -> delegate.updateApplicationInstances(applicationName, instances));
    }

    @Override
    public void updateApplicationMemory(String applicationName, int memory) {
        executeWithRetry(() -> delegate.updateApplicationMemory(applicationName, memory));
    }

    @Override
    public void updateApplicationStaging(String applicationName, Staging staging) {
        executeWithRetry(() -> delegate.updateApplicationStaging(applicationName, staging));
    }

    @Override
    public void updateApplicationUris(String applicationName, List<String> uris) {
        executeWithRetry(() -> delegate.updateApplicationUris(applicationName, uris), HttpStatus.NOT_FOUND);
    }

    @Override
    public void updateServiceBroker(CloudServiceBroker serviceBroker) {
        executeWithRetry(() -> delegate.updateServiceBroker(serviceBroker));
    }

    @Override
    public void updateServicePlanVisibilityForBroker(String name, boolean visibility) {
        executeWithRetry(() -> delegate.updateServicePlanVisibilityForBroker(name, visibility));
    }

    @Override
    public void uploadApplication(String applicationName, File file, UploadStatusCallback callback) {
        executeWithRetry(() -> {
            try {
                delegate.uploadApplication(applicationName, file, callback);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public void uploadApplication(String applicationName, InputStream inputStream, UploadStatusCallback callback) {
        executeWithRetry(() -> {
            try {
                delegate.uploadApplication(applicationName, inputStream, callback);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public UploadToken asyncUploadApplication(String applicationName, File file, UploadStatusCallback callback) {
        return executeWithRetry(() -> {
            try {
                return delegate.asyncUploadApplication(applicationName, file, callback);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public Upload getUploadStatus(UUID packageGuid) {
        return executeWithRetry(() -> delegate.getUploadStatus(packageGuid));
    }

    @Override
    public List<CloudServiceInstance> getServiceInstances() {
        return executeWithRetry(delegate::getServiceInstances, HttpStatus.NOT_FOUND);
    }

    @Override
    public void createApplication(String applicationName, Staging staging, Integer memory, List<String> uris) {
        executeWithRetry(() -> delegate.createApplication(applicationName, staging, memory, uris));
    }

    @Override
    public CloudServiceKey createServiceKey(String serviceInstanceName, String serviceKeyName, Map<String, Object> parameters) {
        return executeWithRetry(() -> delegate.createServiceKey(serviceInstanceName, serviceKeyName, parameters));
    }

    @Override
    public void createUserProvidedServiceInstance(CloudServiceInstance serviceInstance, Map<String, Object> credentials,
                                                  String syslogDrainUrl) {
        executeWithRetry(() -> delegate.createUserProvidedServiceInstance(serviceInstance, credentials, syslogDrainUrl));
    }

    @Override
    public void deleteAllApplications() {
        executeWithRetry(delegate::deleteAllApplications);
    }

    @Override
    public void deleteAllServiceInstances() {
        executeWithRetry(delegate::deleteAllServiceInstances);
    }

    @Override
    public void deleteServiceKey(String serviceInstanceName, String serviceKeyName) {
        executeWithRetry(() -> delegate.deleteServiceKey(serviceInstanceName, serviceKeyName));
    }

    @Override
    public void deleteServiceKey(CloudServiceKey serviceKey) {
        executeWithRetry(() -> delegate.deleteServiceKey(serviceKey));
    }

    @Override
    public Map<String, String> getApplicationEnvironment(String applicationName) {
        return executeWithRetry(() -> delegate.getApplicationEnvironment(applicationName));
    }

    @Override
    public Map<String, String> getApplicationEnvironment(UUID appGuid) {
        return executeWithRetry(() -> delegate.getApplicationEnvironment(appGuid));
    }

    @Override
    public List<CloudEvent> getApplicationEvents(String applicationName) {
        return executeWithRetry(() -> delegate.getApplicationEvents(applicationName), HttpStatus.NOT_FOUND);
    }

    @Override
    public URL getCloudControllerUrl() {
        return executeWithRetry(delegate::getCloudControllerUrl);
    }

    @Override
    public CloudInfo getCloudInfo() {
        return executeWithRetry(delegate::getCloudInfo);
    }

    @Override
    public List<CloudEvent> getEvents() {
        return executeWithRetry(delegate::getEvents, HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudOrganization> getOrganizations() {
        return executeWithRetry(delegate::getOrganizations, HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudServiceKey> getServiceKeys(String serviceInstanceName) {
        return executeWithRetry(() -> delegate.getServiceKeys(serviceInstanceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudServiceKey> getServiceKeys(CloudServiceInstance serviceInstance) {
        return executeWithRetry(() -> delegate.getServiceKeys(serviceInstance), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<CloudServiceOffering> getServiceOfferings() {
        return executeWithRetry(delegate::getServiceOfferings, HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceAuditors() {
        return executeWithRetry(() -> delegate.getSpaceAuditors(), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceAuditors(String spaceName) {
        return executeWithRetry(() -> delegate.getSpaceAuditors(spaceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceAuditors(String organizationName, String spaceName) {
        return executeWithRetry(() -> delegate.getSpaceAuditors(organizationName, spaceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceAuditors(UUID spaceGuid) {
        return executeWithRetry(() -> delegate.getSpaceAuditors(spaceGuid), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceDevelopers() {
        return executeWithRetry(() -> delegate.getSpaceDevelopers(), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceDevelopers(String spaceName) {
        return executeWithRetry(() -> delegate.getSpaceDevelopers(spaceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceDevelopers(String organizationName, String spaceName) {
        return executeWithRetry(() -> delegate.getSpaceDevelopers(organizationName, spaceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceDevelopers(UUID spaceGuid) {
        return executeWithRetry(() -> delegate.getSpaceDevelopers(spaceGuid), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceManagers() {
        return executeWithRetry(() -> delegate.getSpaceManagers(), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceManagers(String spaceName) {
        return executeWithRetry(() -> delegate.getSpaceManagers(spaceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceManagers(String organizationName, String spaceName) {
        return executeWithRetry(() -> delegate.getSpaceManagers(organizationName, spaceName), HttpStatus.NOT_FOUND);
    }

    @Override
    public List<UUID> getSpaceManagers(UUID spaceGuid) {
        return executeWithRetry(() -> delegate.getSpaceManagers(spaceGuid), HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudStack getStack(String name) {
        return executeWithRetry(() -> delegate.getStack(name));
    }

    @Override
    public CloudStack getStack(String name, boolean required) {
        return executeWithRetry(() -> delegate.getStack(name, required));
    }

    @Override
    public List<CloudStack> getStacks() {
        return executeWithRetry(delegate::getStacks, HttpStatus.NOT_FOUND);
    }

    @Override
    public OAuth2AccessToken login() {
        return executeWithRetry(delegate::login);
    }

    @Override
    public void logout() {
        executeWithRetry(delegate::logout);
    }

    @Override
    public void registerRestLogListener(RestLogCallback callBack) {
        executeWithRetry(() -> delegate.registerRestLogListener(callBack));
    }

    @Override
    public void setResponseErrorHandler(ResponseErrorHandler errorHandler) {
        executeWithRetry(() -> delegate.setResponseErrorHandler(errorHandler));
    }

    @Override
    public void unRegisterRestLogListener(RestLogCallback callBack) {
        executeWithRetry(() -> delegate.unRegisterRestLogListener(callBack));
    }

    @Override
    public void uploadApplication(String applicationName, String file) {
        executeWithRetry(() -> {
            try {
                delegate.uploadApplication(applicationName, file);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public void uploadApplication(String applicationName, File file) {
        executeWithRetry(() -> {
            try {
                delegate.uploadApplication(applicationName, file);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public void uploadApplication(String applicationName, InputStream inputStream) {
        executeWithRetry(() -> {
            try {
                delegate.uploadApplication(applicationName, inputStream);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public UploadToken asyncUploadApplication(String applicationName, File file) {
        return executeWithRetry(() -> {
            try {
                return delegate.asyncUploadApplication(applicationName, file);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    @Override
    public CloudTask getTask(UUID taskGuid) {
        return executeWithRetry(() -> delegate.getTask(taskGuid));
    }

    @Override
    public List<CloudTask> getTasks(String applicationName) {
        return executeWithRetry(() -> delegate.getTasks(applicationName), HttpStatus.NOT_FOUND);
    }

    @Override
    public CloudTask runTask(String applicationName, CloudTask task) {
        return executeWithRetry(() -> delegate.runTask(applicationName, task));
    }

    @Override
    public CloudTask cancelTask(UUID taskGuid) {
        return executeWithRetry(() -> delegate.cancelTask(taskGuid));
    }

    @Override
    public CloudBuild createBuild(UUID packageGuid) {
        return executeWithRetry(() -> delegate.createBuild(packageGuid));
    }

    @Override
    public List<CloudBuild> getBuildsForPackage(UUID packageGuid) {
        return executeWithRetry(() -> delegate.getBuildsForPackage(packageGuid));
    }

    @Override
    public CloudBuild getBuild(UUID buildGuid) {
        return executeWithRetry(() -> delegate.getBuild(buildGuid));
    }

    @Override
    public void bindDropletToApp(UUID dropletGuid, UUID appGuid) {
        executeWithRetry(() -> delegate.bindDropletToApp(dropletGuid, appGuid));
    }

    @Override
    public List<CloudBuild> getBuildsForApplication(UUID applicationGuid) {
        return executeWithRetry(() -> delegate.getBuildsForApplication(applicationGuid));
    }

    @Override
    public void unbindServiceInstance(String applicationName, String serviceInstanceName,
                                      ApplicationServicesUpdateCallback applicationServicesUpdateCallback) {
        executeWithRetry(() -> delegate.unbindServiceInstance(applicationName, serviceInstanceName, applicationServicesUpdateCallback));
    }

    @Override
    public List<CloudApplication> getApplicationsByMetadataLabelSelector(String labelSelector) {
        return executeWithRetry(() -> delegate.getApplicationsByMetadataLabelSelector(labelSelector));
    }

    @Override
    public List<CloudServiceInstance> getServiceInstancesByMetadataLabelSelector(String labelSelector) {
        return executeWithRetry(() -> delegate.getServiceInstancesByMetadataLabelSelector(labelSelector));
    }

    @Override
    public void updateApplicationMetadata(UUID guid, Metadata metadata) {
        executeWithRetry(() -> delegate.updateApplicationMetadata(guid, metadata));
    }

    @Override
    public void updateServiceInstanceMetadata(UUID guid, Metadata metadata) {
        executeWithRetry(() -> delegate.updateServiceInstanceMetadata(guid, metadata));
    }

    private void executeWithRetry(Runnable operation, HttpStatus... statusesToIgnore) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, statusesToIgnore);
    }

    private <T> T executeWithRetry(Supplier<T> operation, HttpStatus... statusesToIgnore) {
        ResilientCloudOperationExecutor executor = new ResilientCloudOperationExecutor().withStatusesToIgnore(statusesToIgnore);
        return executor.execute(operation);
    }

}
