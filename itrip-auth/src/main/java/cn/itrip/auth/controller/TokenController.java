package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.Constants;
import cn.itrip.common.DtoUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * description:
 * Created by Ray on 2020-05-19
 */
@RestController
@RequestMapping("/api")
public class TokenController {
    @Resource
    private TokenService tokenService;
    @PostMapping(value = "/retoken",headers = "token")
    public Dto retoken(HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        //验证token
//        Boolean isOK = tokenService.validateToken(token, agent);
        //置换token
        String newToken=null;
//        if(isOK){
            newToken=tokenService.reloadToken(token,agent);
//        }
        //返回新token
        long expTime= Constants.TOKEN_EXPIRE*60*60*1000;
        long genTime=System.currentTimeMillis();
        ItripTokenVO tokenVO = new ItripTokenVO(newToken,expTime,genTime);
        return DtoUtil.returnDataSuccess(tokenVO);
    }
}
