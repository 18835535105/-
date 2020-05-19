package cn.itrip.auth.service;

import cn.itrip.auth.exception.AuthException;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.*;
import com.alibaba.fastjson.JSON;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * description:
 * Created by Ray on 2020-05-19
 */
@Service
public class TokenServiceImpl implements TokenService {
    @Resource
    private RedisAPI redisAPI;
    /**
     * 生成token串
     * @param agent
     * @param itripUser
     * @return
     * @throws Exception
     */
    @Override
    public String generateToken(String agent, ItripUser itripUser) throws Exception {
        //token:客户端标识-USERCODE-USERID-CREATIONDATE-RONDEM[6位]
        //token:PC-3066014fa0b10792e4a762-23-20170531133947-4f6496
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.TOKEN_PRIFIX);
        //为什么区分客户端？移动端有效期不限，PC端自定义有效期
//        boolean b = UserAgentUtil.CheckAgent(agent);
        //获取客户端类型
        DeviceType deviceType = UserAgent.parseUserAgentString(agent).getOperatingSystem().getDeviceType();
        if(deviceType.getName().equals(DeviceType.MOBILE)){//移动终端
            sb.append("MOBILE");
        }else{//其他
            sb.append("PC");
        }
        sb.append("-");
        sb.append(MD5.getMd5(itripUser.getUserCode(),32));
        sb.append("-");
        sb.append(itripUser.getId());
        sb.append("-");
        sb.append(LocalDateTime.now(ZoneOffset.of("+8")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        sb.append("-");
        sb.append(MD5.getMd5(agent, 6));
        return sb.toString();
    }

    /**
     * 缓存token
     * @param token
     * @throws Exception
     */
    @Override
    public void saveToken(String token,ItripUser user) throws Exception {
        String userJson = JSON.toJSONString(user);
        if (token.startsWith(Constants.TOKEN_PRIFIX + "MOBILE")) {
            redisAPI.set(token, userJson);
        }else{
            redisAPI.set(token, Constants.TOKEN_EXPIRE * 60 * 60, userJson);//2小时有效期
        }
    }

    /**
     * 处理token：生成-缓存-返回
     * @param agent
     * @param user
     * @return
     * @throws Exception
     */
    @Override
    public ItripTokenVO processToken(String agent, ItripUser user) throws Exception {
        //生成token
        String token = this.generateToken(agent, user);
        //缓存token
        this.saveToken(token, user);
        //返回token数据
        long expTime= Constants.TOKEN_EXPIRE*60*60*1000;
        long genTime=System.currentTimeMillis();
        ItripTokenVO tokenVO = new ItripTokenVO(token,expTime,genTime);
        return tokenVO;
    }

    /**
     * 验证token有效性
     * @param token
     * @param agent
     * @return
     * @throws Exception
     */
    @Override
    public Boolean validateToken(String token, String agent) throws Exception {
        if(EmptyUtils.isEmpty(token)){
            //未携带token信息
            throw new AuthException("未携带token信息");
        }
        //判断是否同一个客户端
        //token:PC-3066014fa0b10792e4a762-23-20170531133947-4f6496
        String[] tokenArr = token.split("-");
        if (!tokenArr[4].equals(MD5.getMd5(agent, 6))) {
            //不是原来客户端了
            throw new AuthException("不是同一个客户端，未登录");
        }
        String genTimeStr = tokenArr[3];//token生成时间
        LocalDateTime genTime = LocalDateTime.parse(genTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long time = genTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        if(System.currentTimeMillis()-time>Constants.TOKEN_EXPIRE*60*60*1000){//超过两个小时的有效期
            //token过期，未登录状态
            throw new AuthException("token过期失效，已退出");
        }
        return true;
    }

    /**
     * 删除token
     * @param token
     * @throws Exception
     */
    @Override
    public void delToken(String token) throws Exception {
        redisAPI.delete(token);
    }

    /**
     * 置换token
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    public String reloadToken(String token,String agent) throws Exception {

        String userJson = redisAPI.get(token);
        ItripUser itripUser = JSON.parseObject(userJson, ItripUser.class);

        String s = token.split("-")[3];
        LocalDateTime time = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long genTime = time.toInstant(ZoneOffset.of("+8")).toEpochMilli();

        //判断是否在保护期(1小时保护期)
        if(System.currentTimeMillis()-genTime<Constants.TOKEN_PROTECT_TIME*60*60*1000){
            throw new AuthException("保护期，不允许置换");
        }
        //生成新token
        String newToken = this.generateToken(agent, itripUser);
        this.saveToken(newToken, itripUser);//保存新token
        //修改旧token有效期2分钟后失效
        redisAPI.set(token, 2 * 60, userJson);
        return newToken;
    }
}
