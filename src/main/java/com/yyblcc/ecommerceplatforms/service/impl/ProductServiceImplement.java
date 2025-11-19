package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.Enum.ProductStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductAdminVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductDetailVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.ProductQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import com.yyblcc.ecommerceplatforms.service.CategoryService;
import com.yyblcc.ecommerceplatforms.service.CraftsmanService;
import com.yyblcc.ecommerceplatforms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CraftsmanService craftsmanService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CategoryService categoryService;

    @Override
    public PageBean<ProductAdminVO> adminPage(Integer page, Integer pageSize) {
        Page<Product> pages = new Page<>(page,pageSize);
        Page<Product> productPage = productMapper.selectPage(pages,null);

        List<ProductAdminVO> productAdminVOList = productPage.getRecords().stream().map(this::convertToVO).toList();
        PageBean<ProductAdminVO> pageBean = new PageBean<>(productPage.getTotal(), productAdminVOList);
        stringRedisTemplate.opsForValue().set("product:page:"+page+":"+pageSize, JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        return pageBean;
    }

    private ProductAdminVO convertToVO(Product product) {
        ProductAdminVO productAdminVO = new ProductAdminVO();
        BeanUtils.copyProperties(product,productAdminVO);
        productAdminVO.setCraftsmanName(craftsmanService.query().eq("id",product.getCraftsmanId()).one().getName());
        productAdminVO.setCategoryName(categoryService.query().eq("id",product.getCategoryId()).one().getCategoryName());
        return productAdminVO;
    }

    @Override
    @Transactional
    public void review(ProductDTO reviewDTO) {
        Product product = checkProductExists(reviewDTO.getId());
        if (!ProductStatusEnum.PENDING.getCode().equals(product.getReviewStatus())) {
            throw new BusinessException("只有待审核状态的商品才能被审核");
        }
        if (ProductStatusEnum.REJECTED.getCode().equals(reviewDTO.getStatus())){
            product.setRejectReason(reviewDTO.getRejectReason());
        }
        product.setReviewStatus(reviewDTO.getStatus());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("product:page:*").forEach(key -> stringRedisTemplate.delete(key));
    }

    @Override
    @Transactional
    public void changeStatus(Long productId, Integer status) {
        Product product = checkProductExists(productId);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("product:page:*").forEach(key -> stringRedisTemplate.delete(key));
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        if(productMapper.deleteByIds(ids) > 0){
            return true;
        }
        return false;
    }


    @Override
    @Transactional
    public void craftsmanSave(ProductDTO dto, Long craftsmanId) {

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setCraftsmanId(craftsmanId);
        // 提交即待审核
        product.setReviewStatus(ProductStatusEnum.PENDING.getCode());
        product.setStatus(ProductStatusEnum.OFFLINE.getCode());
        product.setIsDeleted(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    @Override
    @Transactional
    public void craftsmanUpdate(ProductDTO dto, Long craftsmanId) {
        Product product = checkProductExists(dto.getId());
        if (!product.getCraftsmanId().equals(craftsmanId.intValue())) {
            throw new BusinessException("只能修改自己的商品");
        }
        if (product.getStatus().equals(ProductStatusEnum.LISTED.getCode())) {
            throw new BusinessException("已上架商品不可修改，如需调整请先下架");
        }
        BeanUtils.copyProperties(dto, product);
        // 修改后重新进入待审核
        product.setReviewStatus(ProductStatusEnum.PENDING.getCode());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("product:page:*").forEach(key -> stringRedisTemplate.delete(key));
    }

    @Override
    public Result<PageBean> myPage(Integer page, Integer pageSize, Long craftsmanId) {
        try{
            String craftsmanProductStr = stringRedisTemplate.opsForValue().get("myproduct:page:" + page + ":" + pageSize);
            if (craftsmanProductStr.equals("")) {
                return Result.success();
            }
            return Result.success(JSON.parseObject(craftsmanProductStr, PageBean.class));
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        Page<Product> pages = new Page<>(page, pageSize);
        Page<Product> productPage = productMapper.selectPage(pages,
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCraftsmanId,craftsmanId)
                        .orderByDesc(Product::getCreateTime));
        PageBean<Product> pageBean = new PageBean<>(productPage.getTotal(),productPage.getRecords());
        stringRedisTemplate.opsForValue().set("myproduct:page:"+page+":"+pageSize, JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        return Result.success(pageBean);
    }

    @Override
    @Transactional
    public void craftsmanOffline(Long productId, Long craftsmanId) {
        Product product = checkProductExists(productId);
        if (!product.getCraftsmanId().equals(craftsmanId.intValue())) {
            throw new BusinessException("只能操作自己的商品");
        }
        if (!ProductStatusEnum.LISTED.getCode().equals(product.getStatus())) {
            throw new BusinessException("只有已上架的商品才能下架");
        }
        product.setStatus(ProductStatusEnum.OFFLINE.getCode());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("myproduct:page:*").forEach(key -> stringRedisTemplate.delete(key));
    }

    @Override
    public ProductDetailVO getDetail(Long id) {
        Product product = checkProductExistsAndOnSale(id);
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        // TODO: 可关联查询匠人信息、工作室信息、文化传承人头衔等
        return vo;
    }

    @Override
    public PageBean<ProductListVO> frontPage(ProductQuery search) {
        Page<Product> page = productMapper.selectPage(new Page<>(search.getPage(), search.getPageSize()), new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode())
                .eq(search.getCategoryId() != null, Product::getCategoryId, search.getCategoryId())
                .like(search.getKeyword() != null, Product::getProductName, search.getKeyword())
                .orderByDesc(Product::getCreateTime));

        Page<ProductListVO> productListVOPage = new Page<>();
        BeanUtils.copyProperties(page, productListVOPage, "records");
        productListVOPage.setRecords(page.getRecords().stream().map(p -> {
            ProductListVO vo = new ProductListVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).toList());
        PageBean pageBean = new PageBean(productListVOPage.getTotal(), productListVOPage.getRecords());
        stringRedisTemplate.opsForValue().set("frontproduct:page:"+search.getPage()+":"+search.getPageSize(), JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        return pageBean;
    }

    @Override
    public List<ProductListVO> recommend(Integer size) {
        return productMapper.selectList(Wrappers.lambdaQuery(Product.class)
                        .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode())
                        .orderByDesc(Product::getCreateTime)
                        .last("LIMIT " + size))
                .stream()
                .map(p -> {
                    ProductListVO vo = new ProductListVO();
                    BeanUtils.copyProperties(p, vo);
                    return vo;
                }).toList();
    }

    @Override
    public List<ProductListVO> listByCraftsman(Long craftsmanId) {
        return productMapper.selectList(Wrappers.lambdaQuery(Product.class)
                        .eq(Product::getCraftsmanId, craftsmanId)
                        .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode()))
                .stream()
                .map(p -> {
                    ProductListVO vo = new ProductListVO();
                    BeanUtils.copyProperties(p, vo);
                    return vo;
                }).toList();
    }

    private Product checkProductExists(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    private Product checkProductExistsAndOnSale(Long id) {
        Product product = checkProductExists(id);
        if (!ProductStatusEnum.LISTED.getCode().equals(product.getStatus())) {
            throw new BusinessException("商品已下架或不存在");
        }
        return product;
    }
}
