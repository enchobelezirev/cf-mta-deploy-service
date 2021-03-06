package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;

import com.sap.cloud.lm.sl.cf.core.Constants;
import com.sap.cloud.lm.sl.cf.core.Messages;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Named
public class MtaMetadataValidator {

    public void validateHasCommonMetadata(CloudEntity entity) {
        validateLabelsArePresent(entity);
        validateAnnotationsArePresent(entity, MtaMetadataUtil.MTA_METADATA_ANNOTATIONS);
    }

    private void validateLabelsArePresent(CloudEntity entity) {
        if (!entity.getV3Metadata()
                   .getLabels()
                   .keySet()
                   .containsAll(MtaMetadataUtil.MTA_METADATA_LABELS)) {
            throw new ContentException(Messages.MTA_METADATA_FOR_0_IS_INCOMPLETE, entity.getName());
        }
    }

    private void validateAnnotationsArePresent(CloudEntity entity, List<String> metadataAnnotations) {
        if (!entity.getV3Metadata()
                   .getAnnotations()
                   .keySet()
                   .containsAll(metadataAnnotations)) {
            throw new ContentException(Messages.MTA_METADATA_FOR_0_IS_INCOMPLETE, entity.getName());
        }
    }

    public void validate(CloudApplication application) {
        validateHasCommonMetadata(application);
        validateAnnotationsArePresent(application, MtaMetadataUtil.MTA_METADATA_APPLICATION_ANNOTATIONS);
        validateAttributeContainsName(application, MtaMetadataAnnotations.MTA_MODULE);
    }

    private void validateAttributeContainsName(CloudEntity entity, String attributeKey) {
        String mtaModule = entity.getV3Metadata()
                                 .getAnnotations()
                                 .get(attributeKey);
        Map<String, Object> mtaModuleMap = JsonUtil.convertJsonToMap(mtaModule);
        if (!mtaModuleMap.containsKey(Constants.ATTR_NAME)) {
            throw new ContentException(MessageFormat.format(Messages.METADATA_OF_0_CONTAINS_INVALID_VALUE_FOR_1, entity.getName(),
                                                            Constants.ENV_MTA_MODULE_METADATA));
        }
    }

    public void validate(CloudServiceInstance serviceInstance) {
        validateHasCommonMetadata(serviceInstance);
        validateAnnotationsArePresent(serviceInstance, MtaMetadataUtil.MTA_METADATA_SERVICE_ANNOTATIONS);
        validateAttributeContainsName(serviceInstance, MtaMetadataAnnotations.MTA_RESOURCE);
    }

}
