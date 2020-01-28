package smarshare.coreservice.write.sagas.constants;

public enum KafkaConstants {


    SAGA_LOCK_TOPIC( "sagaLock" ), LOCK( "lock" ), UN_LOCK( "unlock" ), SAGA_ACCESS_TOPIC( "sagaAccess" ), CREATE( "create" ), DELETE( "delete" );

    private String kafkaParams;

    KafkaConstants(String kafkaParams) {
        this.kafkaParams = kafkaParams;
    }

    public String valueOf() {
        return this.kafkaParams;
    }
}
