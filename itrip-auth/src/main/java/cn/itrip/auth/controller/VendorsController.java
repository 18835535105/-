package cn.itrip.auth.controller;

import cn.itrip.auth.exception.AuthException;
import cn.itrip.auth.service.TokenService;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.beans.vo.ItripWechatTokenVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.UrlUtils;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * description:
 * Created by Ray on 2020-05-21
 */
@RestController
@RequestMapping("/vendors")
public class VendorsController {
    String appId = "wx9168f76f000a0d4c";
    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;
    @GetMapping("/wechat/login")
    public void wechatLogin(HttpServletResponse response) throws IOException {

        String redirectUri = "http://localhost:8081/auth/vendors/wechat/callback";
        String url = "https://open.weixin.qq.com/connect/qrconnect?" +
                "appid=" + appId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri,"utf-8") +
                "&response_type=code" +
                "&scope=snsapi_login" +
                "&state=STATE#wechat_redirect";

        response.sendRedirect(url);
    }
    @RequestMapping("/wechat/callback")
    public Dto callback(String code, String state, HttpServletRequest request) throws Exception {
        String screat = "8ba69d5639242c3bd3a69dffe84336c1";
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" +appId+
                "&secret=" +screat+
                "&code=" +code+
                "&grant_type=authorization_code";
        //用户授权返回结果
        /*
            {
                "access_token":"ACCESS_TOKEN",
                "expires_in":7200,
                "refresh_token":"REFRESH_TOKEN",
                "openid":"OPENID",
                "scope":"SCOPE",
                "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
            }
        * */
        String json = UrlUtils.loadURL(url);
        Map map = JSON.parseObject(json, Map.class);
        String access_token = (String) map.get("access_token");
        //授权失败
        if(access_token==null){
            throw new AuthException("授权失败");
        }
        //授权成功-》创建用户记录到数据库
        String openid = (String) map.get("openid");
        ItripUser itripUser = userService.getItripUserByUserCode(openid);
        if(itripUser==null) {//判断是否第一次微信登录
            itripUser = new ItripUser();
            itripUser.setUserCode(openid);
//        itripUser.setFlatID(openid);//微信给的是个字符串，我们需要的是Long
            itripUser.setUserType(1);
            itripUser.setCreationDate(new Date());
            userService.itriptxCreateItripUser(itripUser);
        }
        //创建客户端在本站的token作为登录凭证
        ItripTokenVO tokenVO = tokenService.processToken(request.getHeader("User-Agent"), itripUser);
        //返回微信的token
        //把两个token返回客户端
        ItripWechatTokenVO wechatTokenVO=new ItripWechatTokenVO(tokenVO.getToken(),tokenVO.getExpTime(),tokenVO.getGenTime());
        wechatTokenVO.setAccessToken(access_token);
        wechatTokenVO.setExpiresIn(String.valueOf(map.get("expires_in")));
        wechatTokenVO.setOpenid(openid);
        wechatTokenVO.setRefreshToken((String) map.get("refresh_token"));
        return DtoUtil.returnDataSuccess(wechatTokenVO);
    }
    @GetMapping("/wechat/user/info")
    public Dto getUserInfo(String accessToken,String openid) throws Exception {
        String url = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" +accessToken+
                "&openid="+openid;
        String json = UrlUtils.loadURL(url);
     /*
            {
                "openid":"OPENID",
                "nickname":"NICKNAME",
                "sex":1,
                "province":"PROVINCE",
                "city":"CITY",
                "country":"COUNTRY",
                "headimgurl": "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
                "privilege":[
                    "PRIVILEGE1",
                    "PRIVILEGE2"
                    ],
                "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
              }
        */
        Map map = JSON.parseObject(json, Map.class);
        String nickname = (String) map.get("nickname");
        ItripUser user = userService.getItripUserByUserCode(openid);
        if(user==null){
            throw new AuthException("获取用户信息失败");
        }

        user.setUserName(nickname);
        userService.updateItripUser(user);//更新微信登录的用户信息

        return DtoUtil.returnDataSuccess(map);
    }
}
