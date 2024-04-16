package cn.lego.service.impl;

import cn.lego.dao.ProductDao;
import cn.lego.domain.Product;
import cn.lego.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDao productDao;
    @Override
    public Product findByPid(Long pid) {
        return productDao.findById(pid).get();
    }
}
