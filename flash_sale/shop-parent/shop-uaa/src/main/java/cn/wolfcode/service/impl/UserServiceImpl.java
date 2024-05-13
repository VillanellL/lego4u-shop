package cn.wolfcode.service.impl;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.domain.UserResponse;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.domain.LoginLog;
import cn.wolfcode.domain.UserLogin;
import cn.wolfcode.mapper.UserMapper;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.redis.CommonRedisKey;
import cn.wolfcode.redis.UaaRedisKey;
import cn.wolfcode.service.IUserService;
import cn.wolfcode.util.MD5Util;
import cn.wolfcode.web.msg.UAACodeMsg;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.UUID;

/**
 * Created by wolfcode-lanxw
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired(required=false)
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    private UserLogin getUser(String phone){
        UserLogin userLogin;
        String userLoginKey = UaaRedisKey.USERLOGIN_STRING.getRealKey(phone);
        String objStr = redisTemplate.opsForValue().get(userLoginKey);
        if(!StringUtils.hasText(objStr)){
            //缓存中并没有，从数据库中查询
            userLogin = userMapper.selectUserLoginByPhone(phone);
            //把用户的登录信息存储到Hash结构中.
            if(userLogin!=null){
                redisTemplate.opsForValue().set(userLoginKey,JSON.toJSONString(userLogin),UaaRedisKey.USERLOGIN_STRING.getExpireTime(),UaaRedisKey.USERLOGIN_STRING.getUnit());
            }
        }else{
            //缓存中有这个key
            userLogin = JSON.parseObject(objStr,UserLogin.class);
        }
        return userLogin;
    }
    @Override
    public UserResponse login(String phone, String password, String ip) {
        //无论登录成功还是登录失败,都需要进行日志记录
        LoginLog loginLog = new LoginLog(phone,ip,new Date());
        //根据用户手机号码查询用户对象
        UserLogin userLogin = this.getUser(phone);
        //进行密码加盐比对
        if(userLogin==null || !userLogin.getPassword().equals(MD5Util.encode(password,userLogin.getSalt()))){
            //进入这里说明登录失败
            loginLog.setState(LoginLog.LOGIN_FAIL);
            //往MQ中发送消息,登录失败
            rocketMQTemplate.sendOneWay(MQConstant.LOGIN_TOPIC,loginLog);
            //同事抛出异常，提示前台账号密码有误
            throw new BusinessException(UAACodeMsg.LOGIN_ERROR);
        }
        UserInfo userInfo = this.getUserInfo(phone);
        String token = createToken(phone);
        rocketMQTemplate.sendOneWay(MQConstant.LOGIN_TOPIC,loginLog);
        return new UserResponse(token,userInfo);
    }
    @Override
    public UserResponse register(String phone, String password, String ip) throws Exception {

        //根据用户手机号码查询用户对象
        UserLogin userLogin = this.getUser(phone);

        if(userLogin!=null ){
            //进入这里说明注册失败
            //同事抛出异常，提示前台账号有误
            throw new BusinessException(UAACodeMsg.REGISTER_TOPIC);
        }
        userLogin = new UserLogin();
        userLogin.setPhone(phone);
        userLogin.setPassword(password);
        userLogin.setSalt("1a2b3c4d5e");
        Connection conn = getConn();
        String sql = "insert into t_user_login(phone, password,salt)values(?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userLogin.getPhone());
        pstmt.setString(2, MD5Util.encode(userLogin.getPassword(),userLogin.getSalt()));
        pstmt.setString(3, userLogin.getSalt());
        pstmt.addBatch();
        pstmt.executeBatch();
        pstmt.close();
        conn.close();
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(phone.toString());
        userInfo.setPhone(userLogin.getPhone());
        userInfo.setBirthDay("1990-01-01");
        userInfo.setHead("1.jpg");
        userInfo.setInfo("这个人很懒，什么都没留下");
        Connection conn2 = getConn();
        String sql2 = "insert into t_user_info(phone,nickname,head,birthDay,info)values(?,?,?,?,?)";
        PreparedStatement pstmt2 = conn2.prepareStatement(sql2);
        pstmt2.setString(1, userInfo.getPhone());
        pstmt2.setString(2, userInfo.getNickName());
        pstmt2.setString(3, userInfo.getHead());
        pstmt2.setString(4, userInfo.getBirthDay());
        pstmt2.setString(5, userInfo.getInfo());
        pstmt2.addBatch();
        pstmt2.executeBatch();
        pstmt2.close();
        conn2.close();

        String token = createToken(phone);
        return new UserResponse(token,userInfo);
    }
    @Override
    public UserInfo getUserInfo(String phone) {
        String userInfoKey = UaaRedisKey.USERINFO_STRING.getRealKey(phone);
        String objStr = redisTemplate.opsForValue().get(userInfoKey);
        UserInfo userInfo = null;
        if(!StringUtils.hasText(objStr)){
            userInfo = userMapper.selectUserInfoByPhone(phone);
            redisTemplate.opsForValue().set(userInfoKey,JSON.toJSONString(userInfo),UaaRedisKey.USERINFO_STRING.getExpireTime(),UaaRedisKey.USERINFO_STRING.getUnit());
        }else{
            userInfo = JSON.parseObject(objStr,UserInfo.class);
        }
        return userInfo;
    }
    public static Connection getConn() throws Exception{
        String url = "jdbc:mysql://localhost:3306/shop-uaa?serverTimezone=GMT%2B8&useSSL=false";
        String username = "root";
        String password = "123456";
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url,username, password);
    }
    private String createToken(String phone) {
        //token创建
        String token = UUID.randomUUID().toString().replace("-","");
        //把user对象存储到redis中
        String key = CommonRedisKey.USER_TOKEN.getRealKey(token);
        redisTemplate.opsForValue().set(key, phone, CommonRedisKey.USER_TOKEN.getExpireTime(),CommonRedisKey.USER_TOKEN.getUnit());
        return token;
    }
}
