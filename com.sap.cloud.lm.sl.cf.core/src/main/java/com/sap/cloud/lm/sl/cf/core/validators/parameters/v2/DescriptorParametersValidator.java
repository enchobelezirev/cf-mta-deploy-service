package com.sap.cloud.lm.sl.cf.core.validators.parameters.v2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.cf.core.validators.parameters.ParameterValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.ParametersValidator;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class DescriptorParametersValidator extends ParametersValidator<DeploymentDescriptor> {

    protected final DeploymentDescriptor descriptor;

    public DescriptorParametersValidator(DeploymentDescriptor deploymentDescriptor, List<ParameterValidator> parameterValidators) {
        this(deploymentDescriptor, parameterValidators, false);
    }

    public DescriptorParametersValidator(DeploymentDescriptor deploymentDescriptor, List<ParameterValidator> parameterValidators,
                                         boolean doNotCorrect) {
        super("", "", parameterValidators, DeploymentDescriptor.class, doNotCorrect);
        this.descriptor = deploymentDescriptor;
    }

    @Override
    public DeploymentDescriptor validate() {
        DeploymentDescriptor castedDescriptor = descriptor;
        validate(castedDescriptor);
        return castedDescriptor;
    }

    protected void validate(DeploymentDescriptor descriptor) {
        Map<String, Object> parameters = validateParameters(descriptor.getParameters());
        List<Module> modules = validateModules(descriptor.getModules());
        List<Resource> resources = validateResources(descriptor.getResources());
        descriptor.setParameters(parameters);
        descriptor.setModules(modules);
        descriptor.setResources(resources);
    }

    protected List<Module> validateModules(List<Module> modules) {
        return modules.stream()
                      .map(this::validate)
                      .collect(Collectors.toList());
    }

    protected Module validate(Module module) {
        return getModuleParametersValidator(module).validate();
    }

    protected List<Resource> validateResources(List<Resource> resources) {
        return resources.stream()
                        .map(this::validate)
                        .collect(Collectors.toList());
    }

    protected Resource validate(Resource resource) {
        return getResourceParametersValidator(resource).validate();
    }

    protected ResourceParametersValidator getResourceParametersValidator(Resource resource) {
        return new ResourceParametersValidator(resource, helper);
    }

    protected ModuleParametersValidator getModuleParametersValidator(Module module) {
        return new ModuleParametersValidator(module, helper);
    }

}
