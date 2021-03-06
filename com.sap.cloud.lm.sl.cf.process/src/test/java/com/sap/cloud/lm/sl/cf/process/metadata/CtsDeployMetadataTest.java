package com.sap.cloud.lm.sl.cf.process.metadata;

import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.cf.web.api.model.OperationMetadata;

public class CtsDeployMetadataTest extends MetadataBaseTest {

    @Override
    protected OperationMetadata getMetadata() {
        return CtsDeployMetadata.getMetadata();
    }

    @Override
    protected String getDiagramId() {
        return Constants.CTS_DEPLOY_SERVICE_ID;
    }

    @Override
    protected String[] getVersions() {
        return new String[] { Constants.SERVICE_VERSION_1_0 };
    }

    @Override
    protected String[] getParametersIds() {
        return new String[] {
            // @formatter:off
                Variables.APP_ARCHIVE_ID.getName(),
                Variables.EXT_DESCRIPTOR_FILE_ID.getName(),
                Variables.NO_START.getName(),
                Variables.START_TIMEOUT.getName(),
                Variables.USE_NAMESPACES.getName(),
                Variables.USE_NAMESPACES_FOR_SERVICES.getName(),
                Variables.VERSION_RULE.getName(),
                Variables.DELETE_SERVICES.getName(),
                Variables.DELETE_SERVICE_KEYS.getName(),
                Variables.DELETE_SERVICE_BROKERS.getName(),
                Variables.FAIL_ON_CRASHED.getName(),
                Variables.MTA_ID.getName(),
                Variables.KEEP_FILES.getName(),
                Variables.NO_RESTART_SUBSCRIBED_APPS.getName(),
                Variables.GIT_URI.getName(),
                Variables.GIT_REF.getName(),
                Variables.GIT_REPO_PATH.getName(),
                Variables.GIT_SKIP_SSL.getName(),
                Variables.NO_FAIL_ON_MISSING_PERMISSIONS.getName(),
                Variables.ABORT_ON_ERROR.getName(),
                Variables.NO_CONFIRM.getName(),
                Variables.CTS_PROCESS_ID.getName(),
                Variables.FILE_LIST.getName(),
                Variables.DEPLOY_URI.getName(),
                Variables.CTS_USERNAME.getName(),
                Variables.CTS_PASSWORD.getName(),
                Variables.APPLICATION_TYPE.getName(),
                Variables.TRANSFER_TYPE.getName(),
                Variables.GIT_REPOSITORY_LIST.getName(),
                Variables.VERIFY_ARCHIVE_SIGNATURE.getName(),
                Variables.DEPLOY_STRATEGY.getName()
            // @formatter:on
        };
    }
}
