package counter.achilles;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.annotations.Table;

@Table(keyspace = "stock", table = "account_stocks")
public class AccountStockEntity {

    @PartitionKey
    @Column("account")
    private String account;

    @Column("stock")
    private long stock;

    public AccountStockEntity() {
    }

    public AccountStockEntity(String account, long stock) {
        this.account = account;
        this.stock = stock;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }
}
