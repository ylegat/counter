package counter.achilles;

import java.util.UUID;
import counter.CreditRecord;
import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = "stock", table = "credit_records")
public class CreditRecordEntity extends CreditRecord {

    @PartitionKey
    @Column("account")
    private String account;

    @ClusteringColumn
    @Column("provisioning_id")
    private UUID provisioningId;

    @Column
    private long provisionedCredits;

    public CreditRecordEntity() {
    }

    public CreditRecordEntity(String account, long provisionedCredits) {
        this.account = account;
        this.provisionedCredits = provisionedCredits;

        this.provisioningId = UUID.randomUUID();
    }

    @Override
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public UUID getProvisioningId() {
        return provisioningId;
    }

    public void setProvisioningId(UUID provisioningId) {
        this.provisioningId = provisioningId;
    }

    @Override
    public long getProvisionedCredits() {
        return provisionedCredits;
    }

    public void setProvisionedCredits(long provisionedCredits) {
        this.provisionedCredits = provisionedCredits;
    }
}
