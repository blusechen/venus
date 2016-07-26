package com.meidusa.venus.extension.athena;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public interface AthenaClientTransaction extends TransactionStatistics, TransactionCommittable {

    AthenaTransactionId startTransaction(String itemName);

}
