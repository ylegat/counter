package counter.achilles;

import java.util.UUID;
import counter.CallRecord;
import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = "stock", table = "call_records")
public class CallRecordEntity extends CallRecord {

    @PartitionKey
    @Column("account")
    private String account;

    @ClusteringColumn
    @Column("call_id")
    private UUID callId;

    @Column("caller")
    private String caller;

    @Column("consumed_credits")
    private long consumedCredits;

    public CallRecordEntity() {
    }

    public CallRecordEntity(String account, String caller, long consumedCredits) {
        this.account = account;
        this.caller = caller;
        this.consumedCredits = consumedCredits;

        this.callId = UUID.randomUUID();
    }

    @Override
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public UUID getCallId() {
        return callId;
    }

    public void setCallId(UUID callId) {
        this.callId = callId;
    }

    @Override
    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    @Override
    public long getConsumedCredits() {
        return consumedCredits;
    }

    public void setConsumedCredits(long consumedCredits) {
        this.consumedCredits = consumedCredits;
    }
}
