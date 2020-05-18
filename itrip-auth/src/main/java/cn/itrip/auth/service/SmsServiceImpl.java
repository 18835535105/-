package cn.itrip.auth.service;

import cn.itrip.common.SystemConfig;
import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Set;

/**
 * description:
 * Created by Ray on 2020-05-18
 */
@Service
public class SmsServiceImpl implements SmsService {
    @Resource(name = "systemConfig")
    private SystemConfig config;
    /**
     * 发送短信服务
     * @param to  手机号
     * @param templateId  模板id
     * @param datas  验证码和有效期
     * @throws Exception
     */
    @Override
    public void send(String to, String templateId, String[] datas) throws Exception {
        //生产环境请求地址：app.cloopen.com
        String serverIp = config.getSmsServerIP();
        //请求端口
        String serverPort = config.getSmsServerPort();
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = config.getSmsAccountSid();
        String accountToken = config.getSmsAuthToken();
        //请使用管理控制台中已创建应用的APPID
        String appId = config.getSmsAppID();
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(serverIp, serverPort);
        sdk.setAccount(accountSId, accountToken);
        sdk.setAppId(appId);
        sdk.setBodyType(BodyType.Type_JSON);
//        String to = "15010475294";
//        String templateId= "1";
//        String[] datas = {"1248","1"};
        HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            System.out.println("发送成功！");
        }else{
            //异常返回输出错误码和错误信息
//            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
            throw new Exception("错误码=" + result.get("statusCode") + " 错误信息= " + result.get("statusMsg"));
        }
    }
}
