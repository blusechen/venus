package com.meidusa.venus.extension.athena;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public interface AthenaServerTransaction extends TransactionStatistics, TransactionCommittable {

    void startTransaction(AthenaTransactionId transactionId, String itemName);

}
