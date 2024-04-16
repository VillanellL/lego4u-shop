package cn.lego.feign;

import cn.lego.domain.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//name的名称一定要和商品服务的服务名保持一致
@FeignClient(name = "product-service",path = "/product")
public interface ProductFeignApi {
    @RequestMapping("/get")
    Product findByPid(@RequestParam("pid") Long pid);
}
