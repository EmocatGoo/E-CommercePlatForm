package com.yyblcc.ecommerceplatforms.service.impl;

import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch._types.Script;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.Enum.OrderStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.Enum.RefundEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.*;
import com.yyblcc.ecommerceplatforms.domain.message.CartDeleteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.OrderQuery;
import com.yyblcc.ecommerceplatforms.domain.query.RefundQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.*;
import com.yyblcc.ecommerceplatforms.service.*;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import com.yyblcc.ecommerceplatforms.util.id.OrderGroupSnGenerator;
import com.yyblcc.ecommerceplatforms.util.id.OrderRefundSnGenerator;
import com.yyblcc.ecommerceplatforms.util.id.OrderSnGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImplement extends ServiceImpl<OrderMapper, Order> implements OrderService {
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final CraftsmanService craftsmanService;
    private final OrderCommentService orderCommentService;
    private static final String lockKey = "order:create:lock:user:";
    private final StringRedisTemplate stringRedisTemplate;
    private final ProductMapper productMapper;
    private final OrderSnGenerator orderSnGenerator;
    private final OrderGroupSnGenerator ogSnGenerator;
    private final OrderRefundSnGenerator orSnGenerator;
    private final RocketMQTemplate rocketMQTemplate;
    private final OrderCommentMapper orderCommentMapper;
    private static final String DELETE_CART_TOPIC = "cart-delete-topic";
    private static final String ORDER_REFUND_TOPIC = "order-refund-topic";
    private final OrderItemMapper orderItemMapper;
    private final RefundMapper refundMapper;
    private static final DefaultRedisScript<Long> PRODUCTSTOCK_SCIPT;
    private final PaymentMapper paymentMapper;
    static{
        PRODUCTSTOCK_SCIPT = new DefaultRedisScript<>();
        PRODUCTSTOCK_SCIPT.setResultType(Long.class);
        PRODUCTSTOCK_SCIPT.setLocation(new ClassPathResource("product_stock.lua"));
    }



    @Override
    public Result<PageBean<OrderAdminVO>> pageList(OrderQuery orderQuery) {
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(orderQuery.getPage(), orderQuery.getPageSize()), new LambdaQueryWrapper<Order>()
                .like(orderQuery.getOrderSn() != null, Order::getOrderSn, orderQuery.getOrderSn())
                .like(orderQuery.getConsignee() != null, Order::getConsignee, orderQuery.getConsignee())
                .eq(orderQuery.getOrderStatus() != null,Order::getOrderStatus, orderQuery.getOrderStatus())
                .between(orderQuery.getBeginTime() != null && orderQuery.getEndTime() != null, Order::getCreateTime, orderQuery.getBeginTime(), orderQuery.getEndTime())
                .orderByDesc(Order::getCreateTime));
        
        List<OrderAdminVO> orderAdminVOList = orderPage.getRecords().stream()
                .map(this::convertToOrderAdminVO)
                .collect(Collectors.toList());
        
        PageBean<OrderAdminVO> pageBean = new PageBean<>(orderPage.getTotal(),orderAdminVOList);
        return Result.success(pageBean);
    }

    @Override
    public Result<PageBean<OrderCraftsmanVO>> craftsmanPageList(OrderQuery orderQuery) {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(orderQuery.getPage(), orderQuery.getPageSize()),
                        new LambdaQueryWrapper<Order>()
                        .eq(Order::getCraftsmanId, craftsmanId)
                        .like(orderQuery.getOrderSn() != null, Order::getOrderSn, orderQuery.getOrderSn())
                        .like(orderQuery.getConsignee() != null, Order::getConsignee, orderQuery.getConsignee())
                        .eq(orderQuery.getOrderStatus() != null,Order::getOrderStatus, orderQuery.getOrderStatus())
                        .between(orderQuery.getBeginTime() != null && orderQuery.getEndTime() != null, Order::getCreateTime, orderQuery.getBeginTime(), orderQuery.getEndTime())
                        .orderByDesc(Order::getCreateTime));
        List<OrderCraftsmanVO> orderCraftsmanVOList = orderPage.getRecords().stream()
                .map(this::convertToOrderCraftsmanVO)
                .toList();
        PageBean<OrderCraftsmanVO> pageBean = new PageBean<>(orderPage.getTotal(),orderCraftsmanVOList);
        return Result.success(pageBean);
    }

    @Override
    public Result<PageBean<OrderUserVO>> pageUserOrders(OrderQuery orderQuery) {
        Long userId = StpKit.USER.getLoginIdAsLong();

        int page = orderQuery.getPage();
        int pageSize = orderQuery.getPageSize();
        int offset = (page - 1) * pageSize;
        List<UserPageGroupDTO> groupList = orderMapper.selectOrderGroupPage(userId,
                orderQuery.getOrderStatus(),
                orderQuery.getBeginTime(),
                orderQuery.getEndTime(),
                offset,
                pageSize);
        if (CollUtil.isEmpty(groupList)){
            return Result.success(new PageBean<>(0L,new ArrayList<>()));
        }

        List<String> groupSnList = groupList.stream().map(UserPageGroupDTO::getOrderGroupSn).toList();

        long total = orderMapper.countOrderGroup(userId,
                orderQuery.getOrderStatus(),
                orderQuery.getBeginTime(),
                orderQuery.getEndTime());

        List<Order> orderList = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getOrderGroupSn, groupSnList)
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime));

        Map<String, List<Order>> groupMap = orderList.stream()
                .collect(Collectors.groupingBy(Order::getOrderGroupSn));

        List<OrderUserVO> voList = new ArrayList<>();

        for (String groupSn : groupSnList) {
            List<Order> orders = groupMap.get(groupSn);
            if (CollUtil.isEmpty(orders)) {
                continue;
            }

            Order first = orders.getFirst();

            OrderUserVO vo = OrderUserVO.builder()
                    .orderGroupSn(groupSn)
                    .orderStatus(first.getOrderStatus())
                    .consignee(first.getConsignee())
                    .shippingAddress(first.getShippingAddress())
                    .phone(first.getPhone())
                    .expressNo(first.getExpressNo())
                    .expressCompany(first.getExpressCompany())
                    .paymentMethod(first.getPaymentMethod())
                    .payTime(first.getPayTime())
                    .createTime(first.getCreateTime())
                    .build();

            // 总金额（子订单求和）
            vo.setTotalAmount(
                    orders.stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            // 子订单号
            List<String> orderSnList = orders.stream()
                    .map(Order::getOrderSn)
                    .toList();
            vo.setOrderSn(orderSnList);

            // 查询订单项
            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .in(OrderItem::getOrderSn, orderSnList)
            );

            vo.setItems(
                    orderItems.stream()
                            .map(this::convertToOrderItemUserVO)
                            .toList()
            );

            voList.add(vo);
        }

        return Result.success(new PageBean<>(total, voList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ExceptionHandler(value = Exception.class)
    @UpdateBloomFilter
    public Result<CreateOrderVO> createOrder(OrderDTO orderDTO) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        String key = lockKey + userId;
        if (Boolean.FALSE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(10)))) {
            throw new RuntimeException("请勿重复提交订单!");
        }
        try {
            List<OrderItemDTO> items = orderDTO.getOrderItemList();
            if(items == null || items.isEmpty()){
                throw new BusinessException("请添加需要购买的商品!");
            }

            // 按工匠ID分组商品
            Map<Long, List<OrderItemDTO>> craftsmanItemsMap = new HashMap<>();
            List<Long> productIds = new ArrayList<>();
            
            for (OrderItemDTO itemDTO : items) {
                productIds.add(itemDTO.getProductId());
            }
            
            // 批量查询商品信息
            List<Product> products = productMapper.selectBatchIds(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, product -> product));
            
            // 验证商品并按工匠分组
            for (OrderItemDTO itemDTO : items) {
                Product product = productMap.get(itemDTO.getProductId());
                if(product == null || product.getStatus() != 1){
                    throw new BusinessException("商品不存在或已下架");
                }
                if (product.getStock() < itemDTO.getQuantity()) {
                    throw new BusinessException("商品[" + product.getProductName() + "]库存不足");
                }
                
                // 按工匠ID分组
                Long craftsmanId = product.getCraftsmanId();
                craftsmanItemsMap.computeIfAbsent(craftsmanId, k -> new ArrayList<>()).add(itemDTO);
            }
            
            // 为每个工匠创建独立的订单
            BigDecimal overallTotalAmount = BigDecimal.ZERO;
            List<String> orderSnList = new ArrayList<>();

            String orderGroupSn = ogSnGenerator.generateOrderGroupSn();
            for (Map.Entry<Long, List<OrderItemDTO>> entry : craftsmanItemsMap.entrySet()) {
                Long craftsmanId = entry.getKey();
                List<OrderItemDTO> craftsmanItems = entry.getValue();
                
                // 生成订单号
                String orderSn = orderSnGenerator.generateOrderSn();
                orderSnList.add(orderSn);
                
                BigDecimal orderTotalAmount = BigDecimal.ZERO;
                BigDecimal craftsmanTotalAmount = BigDecimal.ZERO;
                List<OrderItem> orderItemList = new ArrayList<>();
                
                // 构建订单项
                for (OrderItemDTO itemDTO : craftsmanItems) {
                    Product product = productMap.get(itemDTO.getProductId());
                    BigDecimal price = product.getPrice();
                    int quantity = itemDTO.getQuantity();
                    
                    BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                    BigDecimal commission = itemTotal.multiply(new BigDecimal("0.05"));
                    BigDecimal craftsmanAmount = itemTotal.multiply(new BigDecimal("0.95"));
                    
                    orderTotalAmount = orderTotalAmount.add(itemTotal);
                    craftsmanTotalAmount = craftsmanTotalAmount.add(craftsmanAmount);
                    
                    orderItemList.add(OrderItem.builder()
                            .productId(product.getId())
                            .productName(product.getProductName())
                            .productImage(product.getImageUrl().getFirst())
                            .price(price)
                            .quantity(quantity)
                            .totalAmount(itemTotal)
                            .platformCommission(commission)
                            .craftsmanAmount(craftsmanAmount)
                            .craftsmanId(craftsmanId)
                            .build());
                }
                
                overallTotalAmount = overallTotalAmount.add(orderTotalAmount);
                
                // 创建订单
                String shippingAddress = orderDTO.getProvince() + orderDTO.getCity() + 
                                        orderDTO.getDistrict() + orderDTO.getDetailAddress();
                Order order = Order.builder()
                        .orderSn(orderSn)
                        .orderGroupSn(orderGroupSn)
                        .userId(userId)
                        .craftsmanId(craftsmanId)
                        .totalAmount(orderTotalAmount)
                        .craftsmanAmount(craftsmanTotalAmount)
                        .platformCommission(orderTotalAmount.subtract(craftsmanTotalAmount))
                        .orderStatus(0)
                        .payStatus(0)
                        .consignee(orderDTO.getConsignee())
                        .phone(orderDTO.getPhone())
                        .shippingAddress(shippingAddress)
                        .remark(orderDTO.getRemark())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                
                orderMapper.insert(order);
                
                // 设置订单项关联信息
                for (OrderItem item : orderItemList) {
                    item.setOrderId(order.getId());
                    item.setOrderSn(orderSn);
                    item.setOrderGroupSn(orderGroupSn);
                    item.setUserId(userId);
                    item.setCreateTime(LocalDateTime.now());
                    item.setUpdateTime(LocalDateTime.now());
                }
                
                orderItemService.saveBatch(orderItemList);

                for (OrderItem item : orderItemList) {
                    Long result = stringRedisTemplate.execute(
                            PRODUCTSTOCK_SCIPT,
                            Collections.emptyList(),
                            item.getProductId().toString(),
                            item.getQuantity().toString()
                    );
                    if(result == 1){
                        throw new BusinessException("商品[" + item.getProductName() + "]不存在，请重新下单");
                    }
                    else if (result == 2) {
                        throw new BusinessException("商品[" + item.getProductName() + "]库存不足，请重新下单");
                    }
                    else if(result == 0){
                        productMapper.update(new LambdaUpdateWrapper<Product>()
                                .eq(Product::getId, item.getProductId())
                                .setSql("stock = stock - " + item.getQuantity()));
                    }
                }
            }
            
            // MQ异步删除购物车内容 - 使用fromCart字段明确判断订单来源
            try {
                if (orderDTO.getFromCart() != null && orderDTO.getFromCart() == 1) {
                    CartDeleteMessage message = CartDeleteMessage.builder()
                            .userId(userId)
                            .productIds(productIds)
                            .build();
                    
                    SendResult sendResult = rocketMQTemplate.syncSend(DELETE_CART_TOPIC, message);
                    log.info("发送删除购物车消息成功: {}, 订单号列表: {}", sendResult.getMsgId(), orderSnList);
                } else {
                    log.info("订单非购物车结算来源，不发送删除购物车消息, 订单号列表: {}", orderSnList);
                }
            } catch (Exception e) {
                // 记录异常但不影响订单创建流程
                log.error("发送删除购物车消息失败", e);
            }
            
            return Result.success(CreateOrderVO.builder()
                    .orderSn(orderSnList)
                    .orderGroupSn(orderGroupSn)
                    .totalAmount(overallTotalAmount)
                    .createTime(LocalDateTime.now())
                    .build());
        } catch (BusinessException e) {
            log.error("创建订单失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return Result.error("创建订单失败，请稍后重试");
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> userUpdateOrderStatus(OrderStatsuDTO orderStatsuDTO) {
        String paySn = orderStatsuDTO.getPaySn();
        Long userId = AuthContext.getUserId();

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getPaySn, paySn)
                .in(Order::getOrderStatus,
                        OrderStatusEnum.RECEIPT.getCode(),OrderStatusEnum.BEEVALUATED.getCode())
                .orderByDesc(Order::getCreateTime)
                .last("FOR UPDATE"));

        if (CollUtil.isEmpty(orders)) {
            throw new BusinessException("订单状态异常或无权操作");
        }

        //防越权修改
        for (Order order : orders) {
            if (orderStatsuDTO.getStatus().equals(OrderStatusEnum.BEEVALUATED.getCode())) {
                if (!order.getOrderStatus().equals(OrderStatusEnum.RECEIPT.getCode())) {
                    throw new RuntimeException("不是已发货订单不可确认收货哦");
                }
            } else if (orderStatsuDTO.getStatus().equals(OrderStatusEnum.EVALUATED.getCode())) {
                if (!order.getOrderStatus().equals(OrderStatusEnum.BEEVALUATED.getCode())) {
                    throw new RuntimeException("不是已收货订单不可进行评价哦");
                }
            }
        }

        orders.forEach(order -> {
            order.setOrderStatus(orderStatsuDTO.getStatus());
            orderMapper.updateById(order);
        });

        return Result.success();
    }

    @Override
    public Result<String> updateOrderStatus(OrderStatsuDTO orderStatsuDTO) {
        String paySn = orderStatsuDTO.getPaySn();
        Long craftsmanId = AuthContext.getUserId();
        String expressNo = orderStatsuDTO.getExpressNo();
        String expressCompany = orderStatsuDTO.getExpressCompany();

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getCraftsmanId, craftsmanId)
                .eq(Order::getPaySn, paySn)
                .eq(Order::getOrderStatus, OrderStatusEnum.DISPATCH.getCode())
                .orderByDesc(Order::getCreateTime)
                .last("FOR UPDATE"));

        if (CollUtil.isEmpty(orders)) {
            throw new BusinessException("订单状态异常或无权操作");
        }

        for (Order order : orders) {
            if (orderStatsuDTO.getStatus().equals(OrderStatusEnum.RECEIPT.getCode())) {
                if (!order.getOrderStatus().equals(OrderStatusEnum.DISPATCH.getCode())) {
                    throw new RuntimeException("非待发货订单不可修改发货状态");
                }
            }
        }

        orders.forEach(order -> {
            order.setOrderStatus(orderStatsuDTO.getStatus());
            order.setExpressNo(expressNo);
            order.setExpressCompany(expressCompany);
            orderMapper.updateById(order);
        });

        return Result.success("订单状态更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> signUpRefund(UserSignUpRefundDTO userSignUpRefundDTO) {
        //用户通过自己的订单记录选择退款项目，指定退款物，可以获得需退款商品的productId;会给前端返回paySn，通过paySn找到订单集合orders，通过orders再查找orderItem，具体是利用ordetItemMapper，指定eq为userId,productId,in（orders）。
        //orderItem下有refund实体需要的orderId,orderSn,userId,craftsmanId,productId,productName,productImage,quantity,totalAmount->refundAmount
        String paySn = userSignUpRefundDTO.getPaySn();
        Long userId = StpKit.USER.getLoginIdAsLong();
        Long productId = userSignUpRefundDTO.getProductId();
        
        if (productId == null) {
            throw new BusinessException("请选择需要退款的商品");
        }
        
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getPaySn, paySn)
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
                .last("FOR UPDATE"));

        if (CollUtil.isEmpty(orders)) {
            throw new BusinessException("订单查询发生异常，退款失败");
        }
        
        //退款情况，1、未发货 2、未收货（运输中）3、待评价（已收货）4、已评价 ，也就是均可发起退款申请，但是具体需要匠人审核
        List<String> orderSnList = orders.stream().map(Order::getOrderSn).toList();

        if (CollUtil.isEmpty(orderSnList)) {
            throw new BusinessException("未找到对应订单，退款失败");
        }

        // 查询所有选中的订单项
        OrderItem orderItem = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getUserId, userId)
                .eq(OrderItem::getProductId, productId)
                .in(OrderItem::getOrderSn, orderSnList)
                .last("FOR UPDATE"));

        if (orderItem == null) {
            return Result.error("未找到订单内容");
        }

        if (!productId.equals(orderItem.getProductId())){
            return Result.error("非订单商品");
        }

        String refundSn = orSnGenerator.generateOrderRefundSn();
        refundMapper.insert(Refund.builder()
                .refundSn(refundSn)
                .orderId(orderItem.getOrderId())
                .orderSn(orderItem.getOrderSn())
                .orderItemId(orderItem.getId())
                .userId(orderItem.getUserId())
                .craftsmanId(orderItem.getCraftsmanId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productImage(orderItem.getProductImage())
                .quantity(orderItem.getQuantity())
                .refundAmount(orderItem.getTotalAmount())
                .applyReason(userSignUpRefundDTO.getRefundReason())
                .applyDesc(userSignUpRefundDTO.getRefundDesc())
                .applyImages(userSignUpRefundDTO.getRefundImage())
                .refundType(userSignUpRefundDTO.getRefundType())
                .refundStatus(RefundEnum.APPLY.getCode())
                .paySn(paySn)
                .build());

        return Result.success("已申请退款，请等待商家审核");
    }

    @Override
    public Result reviewOrder(OrderReviewDTO orderReviewDTO) {
        Long craftsmanId = AuthContext.getUserId();

        if (craftsmanId == null) {
            throw new BusinessException("请先登录");
        }

        //查询退款申请是否存在
        List<Refund> refunds = refundMapper.selectList(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getOrderSn, orderReviewDTO.getOrderSn())
                .eq(Refund::getCraftsmanId, craftsmanId)
                .eq(Refund::getRefundStatus, RefundEnum.APPLY.getCode())
                .last("FOR UPDATE"));

        if (refunds == null) {
            throw new BusinessException("退款申请不存在");
        }

        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderSn, orderReviewDTO.getOrderSn())
                .last("FOR UPDATE"));

        Integer status = orderReviewDTO.getStatus();

        if (status.equals(RefundEnum.AGREE.getCode())){
            if (order.getOrderStatus().equals(OrderStatusEnum.RECEIPT.getCode()) ||
                    order.getOrderStatus().equals(OrderStatusEnum.BEEVALUATED.getCode())) {
                refunds.forEach(refund -> {
                    refund.setRefundStatus(RefundEnum.AGREE.getCode());
                    refund.setRefundType(RefundEnum.BACKGOODS.getCode());
                    refund.setAgreeTime(LocalDateTime.now());
                    refundMapper.updateById(refund);

                    orderItemMapper.update(new LambdaUpdateWrapper<OrderItem>()
                            .eq(OrderItem::getOrderSn, orderReviewDTO.getOrderSn())
                            .eq(OrderItem::getCraftsmanId, craftsmanId)
                            .set(OrderItem::getRefundStatus, OrderStatusEnum.APPROVE.getCode())
                            .set(OrderItem::getRefundAmount,order.getTotalAmount())
                            .last("FOR UPDATE"));

                });
                return Result.success("同意退款，请等待用户退货");
            }else{
                refunds.forEach(refund -> {
                    refund.setRefundStatus(RefundEnum.SUCCESS.getCode());
                    refund.setRefundType(RefundEnum.RETURN.getCode());
                    refund.setAgreeTime(LocalDateTime.now());
                    refundMapper.updateById(refund);
                    rocketMQTemplate.syncSend(ORDER_REFUND_TOPIC, refund);
                });
                return Result.success("退款处理中，预计1-7个工作日到账");
            }
        }else if (status.equals(RefundEnum.REFUSE.getCode())){
            refunds.forEach(refund -> {
                refund.setRefundStatus(RefundEnum.REFUSE.getCode());
                refund.setRefuseReason(orderReviewDTO.getRejectReason());
                refundMapper.updateById(refund);

                orderItemMapper.update(new LambdaUpdateWrapper<OrderItem>()
                        .eq(OrderItem::getOrderSn, orderReviewDTO.getOrderSn())
                        .eq(OrderItem::getCraftsmanId, craftsmanId)
                        .set(OrderItem::getRefundStatus, OrderStatusEnum.REFUSE.getCode()));
            });
            return Result.success("已拒绝退款");
        }

        return Result.error("操作失败");
    }

    @Override
    public Result<PageBean<RefundFVO>> refundList(RefundQuery refundQuery) {
        boolean isCraftsmanQuery = refundQuery.getCraftsmanId() != null;
        if (isCraftsmanQuery){
            Page<Refund> refundPage = refundMapper.selectPage(new Page<>(refundQuery.getPage(), refundQuery.getPageSize()),
                    new LambdaQueryWrapper<Refund>()
                            .eq(Refund::getCraftsmanId, refundQuery.getCraftsmanId())
                            .eq(refundQuery.getOrderStatus() != null,Refund::getRefundStatus, refundQuery.getOrderStatus())
                            .between(refundQuery.getBeginTime() != null && refundQuery.getEndTime() != null, Refund::getCreateTime, refundQuery.getBeginTime(), refundQuery.getEndTime())
                            .like(refundQuery.getOrderSn() != null, Refund::getOrderSn, refundQuery.getOrderSn())
                            .orderByDesc(Refund::getCreateTime));
            List<RefundVO> refundVOList = refundPage.getRecords().stream().map(this::convertToRefundVO).toList();
            Map<String,List<RefundVO>> groupByPaySn = refundVOList.stream().collect(Collectors.groupingBy(RefundVO::getPaySn));
            List<RefundFVO> refundFVOS = new ArrayList<>();
            groupByPaySn.forEach((paySn, refundVOs) -> {
                RefundFVO refundCfVO = RefundFVO
                        .builder().paySn(paySn)
                        .refundVOList(refundVOs)
                        .build();
                refundFVOS.add(refundCfVO);
            });
            PageBean<RefundFVO> pageBean = new PageBean<>((long) groupByPaySn.size(), refundFVOS);
            return Result.success(pageBean);
        }else {
            Page<Refund> refundPage = refundMapper.selectPage(new Page<>(refundQuery.getPage(), refundQuery.getPageSize()),
                    new LambdaQueryWrapper<Refund>()
                            .eq(refundQuery.getOrderStatus() != null,Refund::getRefundStatus, refundQuery.getOrderStatus())
                            .between(refundQuery.getBeginTime() != null && refundQuery.getEndTime() != null, Refund::getCreateTime, refundQuery.getBeginTime(), refundQuery.getEndTime())
                            .like(refundQuery.getOrderSn() != null, Refund::getOrderSn, refundQuery.getOrderSn())
                            .orderByDesc(Refund::getCreateTime));
            List<RefundVO> refundVOList = refundPage.getRecords().stream().map(this::convertToRefundVO).toList();
            Map<String,List<RefundVO>> groupByPaySn = refundVOList.stream().collect(Collectors.groupingBy(RefundVO::getPaySn));
            List<RefundFVO> refundFVOS = new ArrayList<>();
            groupByPaySn.forEach((paySn, refundVOs) -> {
                RefundFVO refundCfVO = RefundFVO
                        .builder().paySn(paySn)
                        .refundVOList(refundVOs)
                        .build();
                refundFVOS.add(refundCfVO);
            });
            PageBean<RefundFVO> pageBean = new PageBean<>((long) groupByPaySn.size(), refundFVOS);
            return Result.success(pageBean);
        }
    }

    @Override
    public Result<Long> countCraftsmanOrders() {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getCraftsmanId, craftsmanId));
        return Result.success(count);
    }

    @Override
    public Result<Long> getOrderCounts() {
        Long count = orderMapper.selectCount(null);
        return Result.success(count);
    }


    @Override
    public Result<BigDecimal> getSalesAmount() {
        List<Order> orders = orderMapper.selectList(null);
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Result.success(totalAmount);
    }

    @Override
    public Result<List<OrderAdminVO>> getAllOrders() {
        List<Order> orders = orderMapper.selectList(null);
        List<OrderAdminVO> orderList = orders.stream().map(this::convertToOrderAdminVO).toList();
        return Result.success(orderList);
    }

    @Override
    public Result<String> comment(OrderCommentDTO orderDTO) {
        OrderComment comment = new OrderComment();
        BeanUtils.copyProperties(orderDTO, comment);
        comment.setCreateTime(LocalDateTime.now()).setUpdateTime(LocalDateTime.now());
        int row = orderCommentMapper.insert(comment);
        if (row > 0){
            Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderSn, orderDTO.getOrderSn()));
            order.setOrderStatus(OrderStatusEnum.EVALUATED.getCode());
            orderMapper.update(order, new LambdaUpdateWrapper<Order>().eq(Order::getOrderSn, orderDTO.getOrderSn()));
            return Result.success("评论成功");
        }
        return Result.error("评论失败");
    }

    @Override
    public Result<BigDecimal> getCraftsmanSalesAmount(Long craftsmanId) {
        return Result.success(orderMapper.calculateOrderSaleAmount(craftsmanId));
    }

    @Override
    public Result<List<OrderUserVO>> myOrder() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        List<OrderUserVO> orders = list(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)).stream().map(this::convertToOrderUserVO).toList();
        if (!CollUtil.isEmpty(orders)){
            return Result.success(orders);
        }
        return Result.success();
    }


    private OrderUserVO convertToOrderUserVO(Order order) {
        List<OrderItemUserVO> itemVOList = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId()))
                .stream()
                .map(this::convertToOrderItemUserVO)
                .toList();
        return OrderUserVO.builder()
                .orderId(order.getId())
                .expressNo(order.getExpressNo())
                .expressCompany(order.getExpressCompany())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .consignee(order.getConsignee())
                .phone(order.getPhone())
                .createTime(order.getCreateTime())
                .payTime(order.getPayTime())
                .paymentMethod(order.getPaymentMethod())
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
                .paymentSn(order.getPaySn())
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
                .paySn(order.getPaySn())
                .userNickname(orderUser.getNickname())
                .craftsmanAmount(order.getCraftsmanAmount())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
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
     * 将OrderItem转换为OrderItemUserVO
     * @param orderItem
     * @return
     */
    private OrderItemUserVO convertToOrderItemUserVO(OrderItem orderItem) {
        return OrderItemUserVO.builder()
                .productId(orderItem.getProductId())
                .craftsmanId(orderItem.getCraftsmanId())
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
                .commentContent(orderComment == null ? "" : orderComment.getContent())
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

    private RefundVO convertToRefundVO(Refund refund) {
        RefundVO refundVO = new RefundVO();
        BeanUtils.copyProperties(refund, refundVO);
        Craftsman craftsman = craftsmanService.query().eq("id", refund.getCraftsmanId()).one();
        refundVO.setCraftsmanName(craftsman.getName());
        return refundVO;
    }



}
