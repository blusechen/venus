package com.meidusa.venus.extension.athena.delegate;

import com.meidusa.venus.extension.athena.AthenaTransactionId;
import com.meidusa.venus.extension.athena.AthenaClientTransaction;
import com.meidusa.venus.extension.athena.AthenaServerTransaction;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public class AthenaTransactionDelegate {

    private static AthenaTransactionDelegate delegate = new AthenaTransactionDelegate();

    private AthenaClientTransaction clientTransactionReporter;

    private AthenaServerTransaction serverTransactionReporter;

    private AthenaTransactionDelegate(){

    }

    public static AthenaTransactionDelegate getDelegate() {
        return delegate;
    }

    public void setClientTransactionReporter(AthenaClientTransaction clientTransactionReporter) {
        this.clientTransactionReporter = clientTransactionReporter;
    }

    public void setServerTransactionReporter(AthenaServerTransaction serverTransactionReporter) {
        this.serverTransactionReporter = serverTransactionReporter;
    }

    public AthenaTransactionId startClientTransaction(String itemName) {
        if (clientTransactionReporter != null) {
            return clientTransactionReporter.startTransaction(itemName);
        }

        return null;
    }

    public void completeClientTransaction() {
        if (clientTransactionReporter != null) {
            clientTransactionReporter.commit();
        }
    }

    public void startServerTransaction(AthenaTransactionId transactionId, String itemName) {
        if (serverTransactionReporter != null) {
            serverTransactionReporter.startTransaction(transactionId, itemName);
        }
    }

    public void completeServerTransaction(){
        if (serverTransactionReporter != null) {
            serverTransactionReporter.commit();
        }
    }

    public void setServerInputSize(long size) {
        if (serverTransactionReporter != null) {
            serverTransactionReporter.setInputSize(size);
        }
    }

    public void setServerOutputSize(long size) {
        if (serverTransactionReporter != null) {
            serverTransactionReporter.setOutputSize(size);
        }
    }

    public  void setClientOutputSize(long size) {
        if(clientTransactionReporter != null) {
            clientTransactionReporter.setOutputSize(size);
        }
    }

    public void setClientInputSize(long size) {
        if (clientTransactionReporter != null) {
            clientTransactionReporter.setInputSize(size);
        }
    }

}
