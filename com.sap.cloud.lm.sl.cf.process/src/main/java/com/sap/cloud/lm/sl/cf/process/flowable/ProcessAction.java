package com.sap.cloud.lm.sl.cf.process.flowable;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.flowable.engine.HistoryService;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.process.util.ClientReleaser;
import com.sap.cloud.lm.sl.cf.process.util.HistoryUtil;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;

public abstract class ProcessAction {

    protected final FlowableFacade flowableFacade;
    protected final List<AdditionalProcessAction> additionalProcessActions;
    @Inject
    private CloudControllerClientProvider clientProvider;

    @Inject
    public ProcessAction(FlowableFacade flowableFacade, List<AdditionalProcessAction> additionalProcessActions) {
        this.flowableFacade = flowableFacade;
        this.additionalProcessActions = additionalProcessActions;
    }

    protected List<String> getActiveExecutionIds(String superProcessInstanceId) {
        List<String> activeHistoricSubProcessIds = flowableFacade.getActiveHistoricSubProcessIds(superProcessInstanceId);
        activeHistoricSubProcessIds.add(0, superProcessInstanceId);
        return activeHistoricSubProcessIds;
    }

    public void execute(String user, String superProcessInstanceId) {
        for (AdditionalProcessAction additionalProcessAction : filterAdditionalActionsForThisAction()) {
            additionalProcessAction.executeAdditionalProcessAction(superProcessInstanceId);
        }

        executeActualProcessAction(user, superProcessInstanceId);
    }

    private List<AdditionalProcessAction> filterAdditionalActionsForThisAction() {
        return additionalProcessActions.stream()
                                       .filter(additionalAction -> additionalAction.getApplicableActionId()
                                                                                   .equals(getActionId()))
                                       .collect(Collectors.toList());
    }

    protected abstract void executeActualProcessAction(String user, String superProcessInstanceId);

    public abstract String getActionId();

    protected void updateUserIfNecessary(String user, String executionId) {
        HistoryService historyService = flowableFacade.getProcessEngine()
                                                      .getHistoryService();
        String currentUser = HistoryUtil.getVariableValue(historyService, executionId, Variables.USER.getName());
        if (!user.equals(currentUser)) {
            ClientReleaser clientReleaser = new ClientReleaser(clientProvider);
            clientReleaser.releaseClientFor(historyService, executionId);
            flowableFacade.getProcessEngine()
                          .getRuntimeService()
                          .setVariable(executionId, Variables.USER.getName(), user);
        }
    }
}