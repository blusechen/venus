package com.meidusa.venus.exception;

public interface VenusExceptionCodeConstant {

    int AUTHEN_EXCEPTION = 18004011; // 认证失败

    int REQUEST_ILLEGAL = 18004000; // 请求参数异常,JSON格式化失败

    int UNKNOW_EXCEPTION = 18005000; // 未知的异常

    int INVOCATION_ABORT_WAIT_TIMEOUT = 18005001; // 调用终止,超时等原因
    int SERVICE_UNAVAILABLE_EXCEPTION = 18005002; // 服务端异常
    
    int SERVICE_NOT_FOUND = 18005003; // 服务未找到
    int ENDPOINT_NOT_FOUND = 18005004; // 方法未找到
    int SERVICE_INACTIVE_EXCEPTION = 18005006; // service not active

    int SERVICE_VERSION_NOT_ALLOWD_EXCEPTION = 18005007; // service version not allow
    int SERVICE_NO_CALLBACK_EXCEPTION = 18005008; // no call back
    int SERVICE_REQUEST_HEADER_ERROR_EXCEPTION = 18005009; // no call back

    int SERVICE_RESPONSE_HEADER_ERROR_EXCEPTION = 18005010; // no call back
    
    int REMOTE_SOCKET_IO_EXCEPTION = 18005011; // 调用终止,超时等原因
    
    int SERVICE_DEFINITION_EXCEPTION = 18005012; // 服务端异常
    
    
    int PARAMETER_INVALID_EXCEPTION = 18006006; // 参数校验异常:通用
    int PARAMETER_OMITTED_EXCEPTION = 18006007; // 缺少必需的参数
    int PARAMETER_CHECK_EXCEPTION = 18006008; // 参数校验异常:范围，长度等
    int PARAMETER_CONVERT_EXCEPTION = 18006009; // 类型转换异常:如int型参数，传入字母
    
    int PACKET_DECODE_EXCEPTION = 18007001; // 数据包解码 异常
    
    int DAL_LAYER_ACCESS_EXCEPTION = 18008001; // service 内部 数据访问层异常
    
    int DAL_LAYER_SQL_EXCEPTION = 18008002; // service 内部 数据访问层异常，SQL异常

    int VENUS_CONFIG_EXCEPTION = 18009001; // venus 配置文件异常（服务端异常）
}
