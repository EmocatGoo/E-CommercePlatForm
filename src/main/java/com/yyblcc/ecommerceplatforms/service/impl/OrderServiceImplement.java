package com.yyblcc.ecommerceplatforms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.OrderItemDTO;
import com.yyblcc.ecommerceplatforms.domain.VO.*;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.OrderItemMapper;
import com.yyblcc.ecommerceplatforms.mapper.OrderMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import com.yyblcc.ecommerceplatforms.service.*;

import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImplement extends ServiceImpl<OrderMapper, Order> implements OrderService {
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final CraftsmanService craftsmanService;
    private final OrderCommentService orderCommentService;
    private final ProductService productService;
    private final OrderItemMapper orderItemMapper;
    private static final String lockKey = "order:create:lock:user:";
    private final StringRedisTemplate stringRedisTemplate;
    private final ProductMapper productMapper;


    @Override
    public Result<PageBean> pageList(OrderQuery orderQuery) {
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(orderQuery.getPage(), orderQuery.getPageSize()), new LambdaQueryWrapper<Order>()
                .like(orderQuery.getOrderSn() != null, Order::getOrderSn, orderQuery.getOrderSn())
                .orderByAsc(Order::getCreateTime));
        
        List<OrderAdminVO> orderAdminVOList = orderPage.getRecords().stream()
                .map(this::convertToOrderAdminVO)
                .collect(Collectors.toList());
        
        PageBean<OrderAdminVO> pageBean = new PageBean<>(orderPage.getTotal(),orderAdminVOList);
        return Result.success(pageBean);
    }

    @Override
    public Result<PageBean> craftsmanPageList(OrderQuery orderQuery) {
        //TODO记得删除
//        Long craftsmanId = AuthContext.getUserId();
        Long craftsmanId = 2L;
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(orderQuery.getPage(), orderQuery.getPageSize()),
                        new LambdaQueryWrapper<Order>()
                        .eq(Order::getCraftsmanId, craftsmanId)
                        .like(orderQuery.getOrderSn() != null, Order::getOrderSn, orderQuery.getOrderSn())
                        .orderByAsc(Order::getCreateTime));
        List<OrderCraftsmanVO> orderCraftsmanVOList = orderPage.getRecords().stream()
                .map(this::convertToOrderCraftsmanVO)
                .toList();
        PageBean<OrderCraftsmanVO> pageBean = new PageBean<>(orderPage.getTotal(),orderCraftsmanVOList);
        return Result.success(pageBean);
    }

    @Override
    public Result<PageBean> pageUserOrders(OrderQuery orderQuery) {
//        Long userId = AuthContext.getUserId();
        Long userId = 4L;
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(orderQuery.getPage(), orderQuery.getPageSize()),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .like(orderQuery.getOrderSn() != null, Order::getOrderSn, orderQuery.getOrderSn())
                        .orderByAsc(Order::getCreateTime));
        List<OrderUserVO> orderUserVOList = orderPage.getRecords().stream()
                .map(this::convertToOrderUserVO)
                .toList();
        PageBean<OrderUserVO> pageBean = new PageBean<>(orderPage.getTotal(),orderUserVOList);
        return Result.success(pageBean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createOrder(OrderDTO orderDTO) {
//        Long userId = AuthContext.getUserId();
        //TODO 记得删除
        Long userId = 4L;
        String key = lockKey + userId;
        if (Boolean.FALSE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(10)))) {
            throw new RuntimeException("请勿重复提交订单!");
        }
        try{
            List<OrderItemDTO> items = orderDTO.getOrderItemList();
            if(items == null || items.isEmpty()){
                throw new BusinessException("请添加需要购买的商品!");
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal craftsmanTotalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItemList = new ArrayList<>();

            for (OrderItemDTO orderItemDTO : items) {
                Product product = productService.query().eq("id",orderItemDTO.getProductId()).one();
                if(product == null || product.getStatus() != 1){
                    throw new BusinessException("商品[" + product.getProductName() + "]不存在或已下架");
                }
                if (product.getStock() < orderItemDTO.getQuantity()) {
                    throw new BusinessException("商品[" + product.getProductName() + "]库存不足");
                }

                BigDecimal price = product.getPrice();
                int quantity =  orderItemDTO.getQuantity();

                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                BigDecimal commission = itemTotal.multiply(new BigDecimal("0.05"));
                BigDecimal craftsmanAmount = itemTotal.multiply(new BigDecimal("0.95"));
                totalAmount = totalAmount.add(craftsmanAmount);
                craftsmanTotalAmount = craftsmanTotalAmount.add(craftsmanAmount);

                orderItemList.add(OrderItem.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .productImage(product.getImageUrl())
                        .price(price)
                        .quantity(quantity)
                        .totalAmount(itemTotal)
                        .platformCommission(commission)
                        .craftsmanAmount(craftsmanAmount)
                        .build());
            }
            //暂时先这样设置，待会研究一下雪花算法
            String orderSn = UUID.randomUUID().toString();
            String shippingAddress = orderDTO.getProvince()+orderDTO.getCity()+orderDTO.getDistrict()+orderDTO.getDetailAddress();
            Order order = Order.builder()
                    .orderSn(orderSn)
                    .userId(userId)
                    .craftsmanId(orderItemList.get(0).getCraftsmanId())
                    .totalAmount(totalAmount)
                    .platformCommission(totalAmount.subtract(craftsmanTotalAmount))
                    .orderStatus(0)
                    .payStatus(0)
                    .consignee(orderDTO.getConsignee())
                    .phone(orderDTO.getPhone())
                    .shippingAddress(shippingAddress)
                    .remark(orderDTO.getRemark())
                    .build();
            orderMapper.insert(order);
            for (OrderItem item : orderItemList) {
                item.setOrderId(order.getId());
                item.setOrderSn(orderSn);
                item.setUserId(userId);
            }
            orderItemService.saveBatch(orderItemList);
            //预扣减库存
            for (OrderItem item : orderItemList) {
                int rows = productMapper.update(new LambdaUpdateWrapper<Product>().eq(Product::getId, item.getProductId()).setSql("stock = stock - " + item.getQuantity()));
            }
            //mq异步删除购物车内容（待补充）

            return Result.success(CreateOrderVO.builder()
                    .orderSn(orderSn)
                    .totalAmount(totalAmount)
                    .createTime(LocalDateTime.now())
                    .build());
        }catch (Exception e){
            return Result.error(e.getMessage());
        }finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    private OrderUserVO convertToOrderUserVO(Order order) {
        List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        List<OrderItemUserVO> itemVOList = orderItems.stream()
                .map(this::convertToOrderItemUserVO)
                .toList();

        return OrderUserVO.builder()
                .orderSn(order.getOrderSn())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .expressCompany(order.getExpressCompany())
                .expressNo(order.getExpressNo())
                .items(itemVOList)
                .build();
    }

    /**
     * 将Order转换为OrderAdminVO
     */
    private OrderAdminVO convertToOrderAdminVO(Order order) {
        List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
        .eq(OrderItem::getOrderId, order.getId()));
        
        List<OrderItemAdminVO> itemVOList = orderItems.stream()
                .map(this::convertToOrderItemAdminVO)
                .toList();
        
        User orderUser = userService.query().eq("id",order.getUserId()).one();
        Craftsman orderCraftsman = craftsmanService.query().eq("id",order.getCraftsmanId()).one();
        return OrderAdminVO.builder()
                .id(order.getId())
                .orderSn(order.getOrderSn())
                .userId(order.getUserId())
                .userPhone(orderUser.getPhone())
                .craftsmanId(order.getCraftsmanId())
                .craftsmanName(orderCraftsman.getName())
                .craftsmanPhone(orderCraftsman.getPhone())
                .totalAmount(order.getTotalAmount())
                .craftsmanAmount(order.getCraftsmanAmount())
                .orderStatus(order.getOrderStatus())
                .payStatus(order.getPayStatus())
                .paymentMethod(order.getPaymentMethod())
                .payTime(order.getPayTime())
                .consignee(order.getConsignee())
                .phone(order.getPhone())
                .shippingAddress(order.getShippingAddress())
                .expressCompany(order.getExpressCompany())
                .expressNo(order.getExpressNo())
                .createTime(order.getCreateTime())
                .remark(order.getRemark())
                .items(itemVOList)
                .build();
    }

    /**
     * 将Order转换为OrderAdminVO
     */
    private OrderCraftsmanVO convertToOrderCraftsmanVO(Order order) {
        List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));

        List<OrderItemCraftsmanVO> itemVOList = orderItems.stream()
                .map(this::convertToOrderItemCraftsmanVO)
                .toList();

        User orderUser = userService.query().eq("id",order.getUserId()).one();
        return OrderCraftsmanVO.builder()
                .orderSn(order.getOrderSn())
                .userNickname(orderUser.getNickname())
                .craftsmanAmount(order.getCraftsmanAmount())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .payTime(order.getPayTime())
                .consignee(order.getConsignee())
                .phone(orderUser.getPhone())
                .shippingAddress(order.getShippingAddress())
                .expressCompany(order.getExpressCompany())
                .expressNo(order.getExpressNo())
                .createTime(order.getCreateTime())
                .remark(order.getRemark())
                .items(itemVOList)
                .build();
    }
    /**
     * 将OrderItem转换为OrderItemUserVO
     * @param orderItem
     * @return
     */
    private OrderItemUserVO convertToOrderItemUserVO(OrderItem orderItem) {
        return OrderItemUserVO.builder()
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .isCommented(orderItem.getIsCommented())
                .refundStatus(orderItem.getRefundStatus())
                .build();
    }

    /**
     * 将OrderItem转换为OrderItemCraftsmanVO
     * @param orderItem
     * @return
     */
    private OrderItemCraftsmanVO convertToOrderItemCraftsmanVO(OrderItem orderItem) {
        OrderComment orderComment = orderCommentService.query().eq("id", orderItem.getCommentId()).one();
        return OrderItemCraftsmanVO.builder()
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .craftsmanAmount(orderItem.getCraftsmanAmount())
                .isCommented(orderItem.getIsCommented())
                .commentContent(orderComment.getContent())
                .build();
    }

    /**
     * 将OrderItem转换为OrderAdminItemVO
     */
    private OrderItemAdminVO convertToOrderItemAdminVO(OrderItem orderItem) {
        return OrderItemAdminVO.builder()
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .totalAmount(orderItem.getTotalAmount())
                .craftsmanAmount(orderItem.getCraftsmanAmount())
                .refundStatus(orderItem.getRefundStatus())
                .build();
    }



}
