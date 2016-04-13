package com.meidusa.venus.io.packet;

/**
 * 该数据包表示发送该数据包的connection订阅通知， 并且可以接受notification的数据包。 一个notification数据包只会给同一个client id的任意一个有效的具备订阅通知的connection发送通知
 * 
 * @author Struct
 * 
 */
public class SubscribeNotificationPacket extends AbstractServicePacket {
    private static final long serialVersionUID = 1L;

    public SubscribeNotificationPacket() {
        this.type = PACKET_TYPE_NOTIFY_SUBSCRIBE;
    }
}
