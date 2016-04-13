/**
 * 
 */
package com.meidusa.venus.backend;

public class RequestInfo {

    /**
	 * 
	 */
    private int clientId;

    /**
	 * 
	 */
    private String remoteIp;

    /**
	 * 
	 */
    private String protocolVersion;

    /**
	 * 
	 */
    private Protocol protocol;

    /**
	 * 
	 */
    private String accept;

    public static enum Protocol {
        HTTP, SOCKET
    }

    /**
     * 
     * @return
     */
    public String getRemoteIp() {
        return remoteIp;
    }

    /**
     * 
     * @param remoteIp
     */
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    /**
     * @param protocolVersion the protocolVersion to set
     */
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * @return the protocolVersion
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the protocol
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * @param accept the accept to set
     */
    public void setAccept(String accept) {
        this.accept = accept;
    }

    /**
     * @return the accept
     */
    public String getAccept() {
        return accept;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
    }

}
