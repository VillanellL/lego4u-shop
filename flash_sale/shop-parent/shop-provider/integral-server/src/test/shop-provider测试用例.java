import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import cn.wolfcode.mapper.UsableIntegralMapper;
import cn.wolfcode.domain.OperateIntegralVo;

public class UsableIntegralServiceImplTest {

    @InjectMocks
    private UsableIntegralServiceImpl usableIntegralService;

    @Mock
    private UsableIntegralMapper usableIntegralMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * 测试支付成功的情况
     * 确保 pay 方法在积分足够时返回 true
     */
    @Test
    public void testPaySuccess() {
        // 模拟 usableIntegralMapper.substractIntegral 方法返回正数的情况
        when(usableIntegralMapper.substractIntegral(anyString(), anyInt())).thenReturn(1);

        OperateIntegralVo vo = new OperateIntegralVo();
        vo.setPhone("123456789");
        vo.setValue(50); // 假设支付积分为50

        assertTrue(usableIntegralService.pay(vo));
    }

    /**
     * 测试支付失败的情况
     * 确保 pay 方法在积分不足时返回 false
     */
    @Test
    public void testPayFail() {
        // 模拟 usableIntegralMapper.substractIntegral 方法返回 0 或负数的情况
        when(usableIntegralMapper.substractIntegral(anyString(), anyInt())).thenReturn(0);

        OperateIntegralVo vo = new OperateIntegralVo();
        vo.setPhone("123456789");
        vo.setValue(50); // 假设支付积分为50

        assertFalse(usableIntegralService.pay(vo));
    }

    /**
     * 测试退款成功的情况
     * 确保 refund 方法能够正常执行
     */
    @Test
    public void testRefundSuccess() {
        // 模拟 usableIntegralMapper.incrIntegral 方法执行成功的情况
        doNothing().when(usableIntegralMapper).incrIntegral(anyString(), anyInt());

        OperateIntegralVo vo = new OperateIntegralVo();
        vo.setPhone("123456789");
        vo.setValue(50); // 假设退款积分为50

        usableIntegralService.refund(vo);

    }

    /**
     * 测试退款失败的情况
     * 确保 refund 方法在数据库连接失败时能够抛出异常
     */
    @Test
    public void testRefundFail() {
        // 模拟 usableIntegralMapper.incrIntegral 方法执行失败的情况
        doThrow(new RuntimeException("Database connection failed")).when(usableIntegralMapper).incrIntegral(anyString(), anyInt());

        OperateIntegralVo vo = new OperateIntegralVo();
        vo.setPhone("123456789");
        vo.setValue(50); // 假设退款积分为50

        assertThrows(RuntimeException.class, () -> usableIntegralService.refund(vo));
    }
}
