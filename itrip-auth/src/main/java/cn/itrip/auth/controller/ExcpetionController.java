package cn.itrip.auth.controller;

import cn.itrip.auth.AuthException;
import cn.itrip.beans.dto.Dto;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.ErrorCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * description:
 * Created by Ray on 2020-05-18
 */
@ControllerAdvice
public class ExcpetionController {
    @ExceptionHandler(AuthException.class)
    public Dto handleAuthException(Exception e){
        e.printStackTrace();
        return DtoUtil.returnFail("auth未知异常", ErrorCode.AUTH_UNKNOWN);
    }

    @ExceptionHandler(Exception.class)
    public Dto handleException(Exception e){
        e.printStackTrace();
        return DtoUtil.returnFail("系统未知异常", ErrorCode.AUTH_UNKNOWN);
    }
}
