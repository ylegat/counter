package counter.achilles;

import static java.lang.Math.min;
import java.util.List;
import java.util.Optional;
import counter.Stock;
import info.archinnov.achilles.generated.manager.AccountStockEntity_Manager;
import info.archinnov.achilles.generated.manager.CallRecordEntity_Manager;
import info.archinnov.achilles.generated.manager.CreditRecordEntity_Manager;
import info.archinnov.achilles.type.lightweighttransaction.LWTResultListener;

class AchillesStock implements Stock {

    private AccountStockEntity_Manager accountStockEntityManager;
    private CallRecordEntity_Manager callRecordEntityManager;
    private CreditRecordEntity_Manager creditRecordEntityManager;

    public AchillesStock(AccountStockEntity_Manager accountStockEntityManager,
                         CallRecordEntity_Manager callRecordEntityManager,
                         CreditRecordEntity_Manager creditRecordEntityManager) {
        this.accountStockEntityManager = accountStockEntityManager;
        this.callRecordEntityManager = callRecordEntityManager;
        this.creditRecordEntityManager = creditRecordEntityManager;
    }

    @Override
    public long reserveCredits(String account) throws StoreEmptyException {
        long toBook = STANDARD_BOOKING;
        do {
            AccountStockEntity accountStock = accountStockEntityManager.dsl()
                                                                       .select().stock().fromBaseTable()
                                                                       .where().account_Eq(account)
                                                                       .getOne();
            if (accountStock == null || accountStock.getStock() == 0) {
                throw new StoreEmptyException();
            }

            long stockBeforeOperation = accountStock.getStock();
            toBook = min(toBook, stockBeforeOperation);
            SimpleLWTResultListener resultListener = new SimpleLWTResultListener();
            accountStockEntityManager.dsl()
                                     .update().fromBaseTable()
                                     .stock_Set(stockBeforeOperation - toBook)
                                     .where().account_Eq(account)
                                     .ifStock_Eq(stockBeforeOperation)
                                     .withLwtResultListener(resultListener)
                                     .execute();
            if (resultListener.isSuccess()) {
                return toBook;
            }
        } while (true);
    }

    private static class SimpleLWTResultListener implements LWTResultListener {
        private boolean success = true;

        @Override
        public void onError(LWTResult lwtResult) {
            success = false;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    @Override
    public CreditRecordEntity credit(String account, long amountToProvision) {
        CreditRecordEntity creditRecord = new CreditRecordEntity(account, amountToProvision);
        creditRecordEntityManager.crud().insert(creditRecord).execute();
        creditToStock(account, amountToProvision);
        System.out.println(creditRecord);
        return creditRecord;
    }

    @Override
    public void refundRemainingCredits(String account, long amountNotConsumed) {
        creditToStock(account, amountNotConsumed);
        System.out.println("Refund : " + amountNotConsumed);
    }

    private void creditToStock(String account, long amount) {
        do {
            AccountStockEntity accountStock = accountStockEntityManager.dsl()
                                                                       .select().stock().fromBaseTable()
                                                                       .where().account_Eq(account)
                                                                       .getOne();
            SimpleLWTResultListener resultListener = new SimpleLWTResultListener();
            if (accountStock == null) {
                accountStockEntityManager.crud()
                                         .insert(new AccountStockEntity(account, amount))
                                         .ifNotExists()
                                         .withLwtResultListener(resultListener).execute();
            } else {
                long stockBeforeOperation = accountStock.getStock();
                accountStockEntityManager.dsl()
                                         .update().fromBaseTable()
                                         .stock_Set(stockBeforeOperation + amount)
                                         .where().account_Eq(account)
                                         .ifStock_Eq(stockBeforeOperation)
                                         .withLwtResultListener(resultListener).execute();
            }

            if (resultListener.isSuccess()) {
                return;
            }
        } while (true);
    }

    @Override
    public CallRecordEntity recordCall(String account, String caller, long amountConsumed) {
        CallRecordEntity callRecord = new CallRecordEntity(account, caller, amountConsumed);
        callRecordEntityManager.crud().insert(callRecord).execute();
        System.out.println(callRecord);
        return callRecord;
    }

    @Override
    public long getRemainingCredits(String account) {
        return Optional.ofNullable(accountStockEntityManager.dsl()
                                                            .select().stock().fromBaseTable()
                                                            .where().account_Eq(account)
                                                            .getOne())
                       .map(AccountStockEntity::getStock).orElse(0L);
    }

    @Override
    public List<CallRecordEntity> getCallRecords(String account) {
        return callRecordEntityManager.dsl()
                                      .select().allColumns_FromBaseTable().where().account_Eq(account)
                                      .getList();
    }

    @Override
    public List<CreditRecordEntity> getCreditRecords(String account) {
        return creditRecordEntityManager.dsl()
                                        .select().allColumns_FromBaseTable().where().account_Eq(account)
                                        .getList();
    }
}
