package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode-lanxw
 */
public class UAACodeMsg extends CodeMsg {


    private UAACodeMsg(Integer code, String msg){
        super(code,msg);
    }
    public static final UAACodeMsg LOGIN_ERROR = new UAACodeMsg(500101,"账号密码有误");
    public static final UAACodeMsg REGISTER_TOPIC =new UAACodeMsg(500102,"账号已存在");
}
