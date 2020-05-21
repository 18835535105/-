package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;

/**
 * description:
 * Created by Ray on 2020-05-18
 */
public interface UserService  {
    ItripUser getItripUserByUserCode(String userCode)throws Exception;
    void itriptxCreateItripUser(ItripUser itripUser)throws Exception;

    Boolean itriptxValidateSmsCode(String userCode, String smsCode)throws Exception;

    void updateItripUser(ItripUser user)throws Exception;
}
