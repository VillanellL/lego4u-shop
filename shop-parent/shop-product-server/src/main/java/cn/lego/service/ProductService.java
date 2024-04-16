package cn.lego.service;

import cn.lego.domain.Product;

public interface ProductService {
    public Product findByPid(Long pid);
}
