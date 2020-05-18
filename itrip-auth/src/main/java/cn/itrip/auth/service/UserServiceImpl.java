package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.Constants;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisAPI;
import cn.itrip.mapper.itripUser.ItripUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description:
 * Created by Ray on 2020-05-18
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private ItripUserMapper itripUserMapper;
    @Resource
    private SmsService smsService;
    @Resource
    private RedisAPI redisAPI;
    /**
     * 根据userCode查询用户记录
     * @param userCode 手机号
     * @return
     * @throws Exception
     */
    @Override
    public ItripUser getItripUserByUserCode(String userCode) throws Exception {
        Map<String, Object> param=new HashMap<>();
        param.put("userCode", userCode);
        List<ItripUser> list = itripUserMapper.getItripUserListByMap(param);
        if(list.size()==0){
            return null;
        }
        return list.get(0);
    }

    /**
     * 注册用户
     * @param itripUser
     * @throws Exception
     */
    @Override
    public void itriptxCreateItripUser(ItripUser itripUser) throws Exception {
        //1.写入数据库
        itripUser.setActivated(0);
        itripUser.setUserPassword(MD5.getMd5(itripUser.getUserPassword(),32));
        itripUserMapper.insertItripUser(itripUser);
        //2.发送短信验证码给用户
        int code = MD5.getRandomCode();//随机短信验证码
        int expire=1;//验证码有效期
        smsService.send(itripUser.getUserCode(),"1",new String[]{String.valueOf(code),String.valueOf(expire)});
        //3.缓存短信验证码
        redisAPI.set(Constants.PHONE_SMS_ACTIVE_PREFIX + itripUser.getUserCode(), expire * 60, String.valueOf(code));
    }

    /**
     * 验证手机短信
     * @param userCode
     * @param smsCode
     * @return
     */
    @Override
    public Boolean itriptxValidateSmsCode(String userCode, String smsCode)throws Exception {
        //验证短信验证码
        String cacheCode = redisAPI.get(Constants.PHONE_SMS_ACTIVE_PREFIX + userCode);
        ItripUser itripUser = this.getItripUserByUserCode(userCode);
        if(cacheCode!=null&&cacheCode.equals(smsCode)) {
            //成功后修改数据库中用户记录（激活）
            itripUser.setActivated(1);
            itripUser.setUserType(0);//用户类型：自注册
            itripUser.setUserName("hello");
            itripUser.setFlatID(itripUser.getId());
            itripUser.setCreationDate(new Date());
            itripUserMapper.updateItripUser(itripUser);
            return  true;
        }
        //失败删除未激活的这个用户
        itripUserMapper.deleteItripUserById(itripUser.getId());
        return false;
    }
}
