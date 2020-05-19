package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.*;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * description:
 * Created by Ray on 2020-05-19
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;
    Logger logger = Logger.getLogger(LoginController.class);

    @PostMapping("/dologin")
    public Dto doLogin(@RequestParam("name")String userCode, @RequestParam("password")String userPassword, HttpServletRequest request) throws Exception {
        String agent = request.getHeader("User-Agent");
        logger.info("UserAgent----"+agent);
        //验证登录
        ItripUser user = userService.getItripUserByUserCode(userCode);
        if(user==null||!user.getUserPassword().equals(MD5.getMd5(userPassword,32))){
            return DtoUtil.returnFail("用户名或密码错误", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        //处理token
        ItripTokenVO tokenVO=tokenService.processToken(agent, user);
        return DtoUtil.returnDataSuccess(tokenVO);
    }

    @GetMapping(value = "/logout",headers = "token")//限定请求头携带token信息
    public Dto doLogout(HttpServletRequest request) throws Exception {
        String token = request.getHeader("token");
        String agent = request.getHeader("User-Agent");
        //验证token有效性
        Boolean isOk=tokenService.validateToken(token,agent);
        if (isOk) {
            //删除token缓存
            tokenService.delToken(token);
        }
        return DtoUtil.returnSuccess("退出成功！");

    }

}
