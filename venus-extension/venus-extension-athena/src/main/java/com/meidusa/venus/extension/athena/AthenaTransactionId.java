package com.meidusa.venus.extension.athena;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public class AthenaTransactionId {

    private String rootId;
    private String parentId;
    private String messageId;

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
