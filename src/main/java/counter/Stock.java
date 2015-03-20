package counter;

public interface Stock {

    void book(long amount) throws Exception;

    void provision(long amount);

    long remainingAmount();

}
