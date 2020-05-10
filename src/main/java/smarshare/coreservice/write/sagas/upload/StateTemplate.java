package smarshare.coreservice.write.sagas.upload;

import smarshare.coreservice.write.sagas.constants.UploadStateNames;
import smarshare.coreservice.write.sagas.dto.SagaEventWrapper;

public class StateTemplate {


    private StateTemplate successState;
    private StateTemplate.Builder successStateBuilder;
    private StateTemplate failureState;
    private StateTemplate.Builder failureStateBuilder;
    private UploadStateNames nextState;
    private Boolean isTerminalState;
    private StateTemplate.Builder terminalStateBuilder;
    private UploadStateNames currentStateName;
    private UploadOperationsOnObjects taskToBeDoneInThisState;

    public StateTemplate(Builder builder) {


        this.successStateBuilder = builder.successState;
        this.successState = (null != builder.successState) ? builder.successState.build() : null;
        this.failureStateBuilder = builder.failureState;
        this.failureState = (null != builder.failureState) ? builder.failureState.build() : null;
        this.isTerminalState = builder.isTerminalState;
        this.currentStateName = builder.currentStateName;
        this.taskToBeDoneInThisState = builder.taskToBeDoneInThisState;
        this.terminalStateBuilder = builder.terminalState;
    }

    public Builder getTerminalStateBuilder() {
        return terminalStateBuilder;
    }

    public StateTemplate getSuccessState() {
        return successState;
    }

    public StateTemplate.Builder getSuccessStateBuilder() {
        return successStateBuilder;
    }

    public StateTemplate getFailureState() {
        return failureState;
    }

    public StateTemplate.Builder getFailureStateBuilder() {
        return failureStateBuilder;
    }

    public UploadStateNames getNextState() {
        return nextState;
    }

    public String getCurrentStateName() {
        return currentStateName.valueOf();
    }

    public Boolean isTerminalState() {
        return isTerminalState;
    }


    SagaEventWrapper handleTask(SagaEventWrapper taskInput) {
        try {
            if (this.isTerminalState && null == this.taskToBeDoneInThisState) {
                return taskInput.setRecentSuccessfulState( getCurrentStateName() );
            } else {
                if (null == this.taskToBeDoneInThisState) {
                    this.nextState = (null != getSuccessState()) ? getSuccessState().currentStateName : null;
                    return taskInput.setRecentSuccessfulState( getCurrentStateName() );
                } else {
                    if (this.taskToBeDoneInThisState.apply( taskInput )) {
                        this.nextState = (null != getSuccessState()) ? getSuccessState().currentStateName : null;
                        return taskInput.setRecentSuccessfulState( getCurrentStateName() );
                    }
                }
            }
        } catch (Exception e) {
            this.nextState = (null != getFailureState()) ? getFailureState().currentStateName : null;

        }
        this.nextState = (null != getFailureState()) ? getFailureState().currentStateName : null;
        return taskInput;
    }

    @Override
    public String toString() {
        return "StateTemplate{" +
                "successState=" + successState +
                ", successStateBuilder=" + successStateBuilder +
                ", failureState=" + failureState +
                ", failureStateBuilder=" + failureStateBuilder +
                ", nextState=" + nextState +
                ", isTerminalState=" + isTerminalState +
                ", terminalStateBuilder=" + terminalStateBuilder +
                ", currentStateName='" + currentStateName + '\'' +
                ", taskToBeDoneInThisState=" + taskToBeDoneInThisState +
                '}';
    }

    public static class Builder {

        private Builder successState;
        private Builder failureState;
        private Boolean isTerminalState;
        private Builder terminalState;
        private UploadStateNames currentStateName;
        private UploadOperationsOnObjects taskToBeDoneInThisState;


        Builder() {
        }

        Builder(Boolean isTerminalState, UploadStateNames currentStateName) {
            this.isTerminalState = isTerminalState;
            this.currentStateName = currentStateName;

        }

        private Builder stateCreation(Boolean isTerminalState, UploadStateNames currentStateName) {
            return new Builder( isTerminalState, currentStateName );
        }

        private Builder copyFieldsFromExistingState(StateTemplate alreadyExistingState) {

            this.successState = alreadyExistingState.getSuccessStateBuilder();
            this.failureState = alreadyExistingState.getFailureStateBuilder();
            this.isTerminalState = alreadyExistingState.isTerminalState;
            this.currentStateName = alreadyExistingState.currentStateName;
            return this;
        }

        private Builder stateCreation(StateTemplate alreadyExistingState) {

            return new Builder().copyFieldsFromExistingState( alreadyExistingState );
        }

        public Builder successState(Boolean isTerminalState, UploadStateNames currentStateName) {
            this.successState = stateCreation( isTerminalState, currentStateName );
            return this;
        }

        public Builder successState(StateTemplate alreadyExistingState) {
            this.successState = stateCreation( alreadyExistingState );
            return this;
        }

        public Builder failureState(Boolean isTerminalState, UploadStateNames currentStateName) {
            this.failureState = stateCreation( isTerminalState, currentStateName );
            return this;
        }

        public Builder failureState(StateTemplate alreadyExistingState) {
            this.failureState = stateCreation( alreadyExistingState );
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "successState=" + successState +
                    ", failureState=" + failureState +
                    ", isTerminalState=" + isTerminalState +
                    ", currentStateName='" + currentStateName + '\'' +
                    '}';
        }

        public StateTemplate build() {
            if (this.isTerminalState) {
                this.successState = null;
                this.failureState = null;
                this.terminalState = this;

            }
            return new StateTemplate( this );
        }

        public Builder taskToBeDoneInThisState(UploadOperationsOnObjects operationsOnObjects) {
            this.taskToBeDoneInThisState = operationsOnObjects;
            return this;
        }
    }
}
