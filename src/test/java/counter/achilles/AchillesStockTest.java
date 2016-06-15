package counter.achilles;

import org.junit.Rule;
import counter.Stock;
import counter.StockTest;
import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.generated.manager.AccountStockEntity_Manager;
import info.archinnov.achilles.generated.manager.CallRecordEntity_Manager;
import info.archinnov.achilles.generated.manager.CreditRecordEntity_Manager;
import info.archinnov.achilles.junit.AchillesTestResource;
import info.archinnov.achilles.junit.AchillesTestResourceBuilder;

public class AchillesStockTest extends StockTest {

    @Rule
    public AchillesTestResource<ManagerFactory> achillesTestResource = 
            AchillesTestResourceBuilder.forJunit()
                                       .createAndUseKeyspace("stock")
                                       .entityClassesToTruncate(AccountStockEntity.class,
                                                                CallRecordEntity.class,
                                                                CreditRecordEntity.class)
                                       .truncateBeforeAndAfterTest()
                                       .build((cluster, statementsCache) -> 
                                                ManagerFactoryBuilder.builder(cluster)
                                                                     .doForceSchemaCreation(true)
                                                                     .withStatementsCache(statementsCache) // MANDATORY
                                                                     .build());

    @Override
    public Stock implementation() {
        AccountStockEntity_Manager accountStockEntityManager = achillesTestResource.getManagerFactory()
                                                                                   .forAccountStockEntity();
        CallRecordEntity_Manager callRecordEntityManager = achillesTestResource.getManagerFactory()
                                                                               .forCallRecordEntity();
        CreditRecordEntity_Manager creditRecordEntityManager = achillesTestResource.getManagerFactory()
                                                                                   .forCreditRecordEntity();
        return new AchillesStock(accountStockEntityManager, callRecordEntityManager, creditRecordEntityManager);
    }
}
