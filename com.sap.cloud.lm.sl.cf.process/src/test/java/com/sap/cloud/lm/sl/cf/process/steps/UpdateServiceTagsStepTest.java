package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.sap.cloud.lm.sl.cf.core.cf.clients.ServiceInstanceGetter;
import com.sap.cloud.lm.sl.cf.core.cf.clients.ServiceUpdater;
import com.sap.cloud.lm.sl.cf.core.util.MethodExecution;
import com.sap.cloud.lm.sl.cf.core.util.MethodExecution.ExecutionState;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil;

@RunWith(Parameterized.class)
public class UpdateServiceTagsStepTest extends SyncFlowableStepTest<UpdateServiceTagsStep> {

    private static final String POLLING = "polling";
    private static final String STEP_EXECUTION = "stepExecution";

    private final StepInput stepInput;

    @Mock
    private ServiceInstanceGetter serviceInstanceGetter;
    @Mock
    protected ServiceUpdater serviceUpdater;

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
        // @formatter:off
            {
                "update-service-tags-step-input-1.json", null,
            },
        // @formatter:on
        });
    }

    public UpdateServiceTagsStepTest(String stepInput, String expectedExceptionMessage) {
        this.stepInput = JsonUtil.fromJson(TestUtil.getResourceAsString(stepInput, UpdateServiceTagsStepTest.class), StepInput.class);
    }

    @Before
    public void setUp() {
        prepareContext();
    }

    private void prepareServiceUpdater(String stepPhase) {
        MethodExecution<String> methodExec;
        switch (stepPhase) {
            case POLLING:
                methodExec = new MethodExecution<>(null, ExecutionState.FINISHED);
                break;
            case STEP_EXECUTION:
                methodExec = new MethodExecution<>(null, ExecutionState.EXECUTING);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported test phase");
        }
        Mockito.when(serviceUpdater.updateServiceTags(any(), any(), any()))
               .thenReturn(methodExec);
    }

    @Test
    public void testExecute() {
        prepareResponses(STEP_EXECUTION);
        step.execute(execution);
        assertStepPhase(STEP_EXECUTION);

        if (getExecutionStatus().equals("DONE")) {
            return;
        }
        prepareResponses(POLLING);
        step.execute(execution);
        assertStepPhase(POLLING);
        assertMethodCalls();
    }

    private void assertMethodCalls() {
        Mockito.verify(serviceUpdater, Mockito.times(1))
               .updateServiceTags(any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    private void assertStepPhase(String stepPhase) {
        Map<String, Object> stepPhaseResults = (Map<String, Object>) stepInput.stepPhaseResults.get(stepPhase);
        String expectedStepPhase = (String) stepPhaseResults.get("expectedStepPhase");
        assertEquals(expectedStepPhase, getExecutionStatus());
    }

    private void prepareContext() {
        execution.setVariable("serviceToProcess", JsonUtil.toJson(stepInput.service));
    }

    private void prepareResponses(String stepPhase) {
        prepareServiceUpdater(stepPhase);
    }

    private static class StepInput {
        SimpleService service;
        Map<String, Object> stepPhaseResults;
    }

    private static class SimpleService {
        String name;
        String label;
        String plan;
        String guid;
    }

    @Override
    protected UpdateServiceTagsStep createStep() {
        return new UpdateServiceTagsStep();
    }

}
