package com.yyblcc.ecommerceplatforms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.yyblcc.ecommerceplatforms.config.AliPayConfig;
import com.yyblcc.ecommerceplatforms.domain.Enum.OrderStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.Enum.PayStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.PaymentVO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import com.yyblcc.ecommerceplatforms.domain.po.Payment;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.mapper.OrderMapper;
import com.yyblcc.ecommerceplatforms.mapper.PaymentMapper;
import com.yyblcc.ecommerceplatforms.service.PaymentService;
import com.yyblcc.ecommerceplatforms.util.id.PaySnGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImplement extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {
    private final AliPayConfig alipayConfig;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final PaySnGenerator paySnGenerator;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    /**
     * 初始化支付宝配置
     */
    private void initAlipayClient() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = alipayConfig.getGATEWAY_HOST();
        config.signType = "RSA2";
        config.appId = alipayConfig.getAPP_ID();
        config.merchantPrivateKey = alipayConfig.getAPP_PRIVATE_KEY();
        config.alipayPublicKey = alipayConfig.getALIPAY_PUBLIC_KEY();
        config.notifyUrl = alipayConfig.getNOTIFY_URL();

        Factory.setOptions(config);
    }

    @Override
    // 1. 查询所有订单信息
    // 2. 验证所有订单属于同一用户
    // 3. 计算总支付金额和订单数量
    // 4. 生成合并支付单号
    // 5. 创建支付记录
    // 6. 保存支付记录
    // 7. 更新订单的payId和paySn
    // 8. 返回合并支付单号
    @Transactional(rollbackFor = Exception.class)
    public Result createPayment(List<String> orderSn) throws InterruptedException {
        if (CollUtil.isEmpty(orderSn)) {
            return Result.error("订单号列表不能为空");
        }
        String lockKey = "lock:mergePayment:" + orderSn.hashCode();
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.tryLock(0, 30, TimeUnit.SECONDS)) {
            return Result.error("正在处理中，请勿重复提交");
        }
        try {
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .in(Order::getOrderSn, orderSn)
                    .eq(Order::getPayStatus, PayStatusEnum.PENDING.getCode())
            );
            if (orders.isEmpty() || orders.size() != orderSn.size()) {
                return Result.error("未找到待支付订单");
            }

            Long userId = orders.getFirst().getUserId();
            if (orders.stream().anyMatch(order -> !order.getUserId().equals(userId))) {
                return Result.error("订单不属于同一用户，无法合并支付");
            }

            Payment existPayment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getUserId, userId)
                    .eq(Payment::getPayStatus, PayStatusEnum.PENDING.getCode())
                    .gt(Payment::getExpireTime, LocalDateTime.now())
                    .orderByDesc(Payment::getCreateTime));
            if (existPayment != null) {
                String cacheKey = "order:payment:" + userId;
                Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(cacheKey);
                String qrCode = map.get("qrCodeBase64").toString();
                if (StringUtils.isNotBlank(qrCode)) {
                    return Result.success(PaymentVO.builder()
                            .paymentSn(existPayment.getMergePaySn())
                            .qrCodeBase64(qrCode)
                            .build());
                }
                return regenerateQrCode(existPayment);
            }

            BigDecimal totalAmount = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            String mergePaySn = paySnGenerator.generatePaySn(userId);

            AlipayTradePrecreateResponse response = createAlipayPreOrder(mergePaySn, totalAmount);
            if (!ResponseChecker.success(response)) {
                log.error("支付宝预下单失败 code={} subCode={} msg={} subMsg={}",
                        response.getCode(), response.getSubCode(), response.getMsg(), response.getSubMsg());
                return Result.error("支付出现异常，请稍后重试");
            }
            String qrCodeBase64 = generateQRCodeBase64(response.getQrCode());

            Payment payment = Payment.builder()
                    .mergePaySn(mergePaySn)
                    .userId(userId)
                    .totalAmount(totalAmount)
                    .orderCount(orders.size())
                    .payStatus(PayStatusEnum.PENDING.getCode())
                    .createTime(LocalDateTime.now())
                    .expireTime(LocalDateTime.now().plusMinutes(2))
                    .build();
            paymentMapper.insert(payment);

            orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .in(Order::getOrderSn, orderSn)
                    .set(Order::getPayId, payment.getId())
                    .set(Order::getPaySn, mergePaySn)
                    .set(Order::getPaymentMethod, 0));

            String cacheKey = "order:payment:" + userId;
            Map<String, String> cacheMap = Map.of("mergePaySn", mergePaySn, "qrCodeBase64", qrCodeBase64);
            stringRedisTemplate.opsForHash().putAll(cacheKey, cacheMap);
            stringRedisTemplate.expire(cacheKey, Duration.ofMinutes(2));

            log.info("创建支付成功，合并支付单号：{}, 订单数量：{}, 总金额：{}", mergePaySn, orders.size(), totalAmount);
            return Result.success(PaymentVO.builder()
                    .qrCodeBase64(qrCodeBase64)
                    .paymentSn(mergePaySn)
                    .build());

        } catch (Exception e) {
            log.error("订单合并支付异常", e);
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 抽离支付宝调用（便于重试和复用）
    private AlipayTradePrecreateResponse createAlipayPreOrder(String mergePaySn, BigDecimal amount) throws Exception {
        initAlipayClient();
        AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace()
                .preCreate("非遗技艺传承电商平台订单支付", mergePaySn, amount.toPlainString());
        return response;
    }

    // 复用支付单时重新生成二维码（极少用）
    private Result<PaymentVO> regenerateQrCode(Payment payment) throws Exception {
        AlipayTradePrecreateResponse resp = createAlipayPreOrder(payment.getMergePaySn(), payment.getTotalAmount());
        if (ResponseChecker.success(resp)) {
            String qrCode = generateQRCodeBase64(resp.getQrCode());
            stringRedisTemplate.opsForHash().put("order:payment:" + payment.getUserId(), "qrCodeBase64", qrCode);
            return Result.success(PaymentVO.builder()
                    .paymentSn(payment.getMergePaySn())
                    .qrCodeBase64(qrCode)
                    .build());
        }
        return Result.error("支付单存在但二维码生成失败，请重新下单");
    }

    /**
     * 将二维码内容转换为 Base64 编码的图片
     */
    private String generateQRCodeBase64(String qrCodeContent) throws Exception {
        try {
            int width = 300;
            int height = 300;
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            log.error("生成二维码 Base64 失败：{}", e.getMessage(), e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    @Override
    public Result queryPaymentStatus(List<String> orderSn) {
        if (CollUtil.isEmpty(orderSn)) {
            return Result.error("订单号列表不能为空");
        }

        try {
            // 1. 查询订单 + 严格校验
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .in(Order::getOrderSn, orderSn)
                    .select(Order::getOrderSn, Order::getPaySn, Order::getPayStatus, Order::getOrderStatus)
            );

            if (orders.isEmpty()) {
                return Result.error("订单不存在");
            }
            if (orders.size() != orderSn.size()) {
                return Result.error("部分订单不存在");
            }

            // 2. 校验所有订单属于同一支付单
            String paySn = orders.getFirst().getPaySn();
            if (StringUtils.isBlank(paySn)) {
                return Result.error("订单未创建支付");
            }
            if (orders.stream().anyMatch(o -> !paySn.equals(o.getPaySn()))) {
                return Result.error("订单不属于同一支付单");
            }

            // 3. 查询支付记录（加锁防并发）
            Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getMergePaySn, paySn)
                    .last("FOR UPDATE")
            );

            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            // 4. 判断是否已支付成功（幂等）
            if (Integer.valueOf(PayStatusEnum.PAYED.getCode()).equals(payment.getPayStatus())) {
                return Result.success("支付成功");
            }

            // 5. 判断是否已过期（修复你原来的超级大 Bug！）
            if (LocalDateTime.now().isAfter(payment.getExpireTime())) {
                // 过期 → 关闭支付单和订单
                closePaymentAndOrders(payment, orders);
                return Result.error("订单已过期");
            }

            // 6. 调用支付宝查询真实状态
            AlipayTradeQueryResponse aliResp = queryAlipayStatus(paySn);
            if (aliResp == null) {
                // 支付宝接口异常 → 不改状态，只返回“查询中”
                return Result.success("支付处理中，请稍后查询");
            }

            if (!ResponseChecker.success(aliResp)) {
                log.warn("支付宝查询失败，但不关闭订单：code={} msg={}", aliResp.getCode(), aliResp.getSubMsg());
                return Result.success("支付处理中，请稍后查询");
            }

            // 7. 支付宝返回成功 → 更新状态
            if ("TRADE_SUCCESS".equals(aliResp.getTradeStatus())
                    || "TRADE_FINISHED".equals(aliResp.getTradeStatus())) {

                // 更新支付单
                payment.setPayStatus(PayStatusEnum.PAYED.getCode());
                payment.setPayTime(LocalDateTime.now());
                paymentMapper.updateById(payment);

                // 批量更新订单（性能高 + 事务一致）
                orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                        .in(Order::getOrderSn, orderSn)
                        .set(Order::getPayStatus, PayStatusEnum.PAYED.getCode())
                        .set(Order::getOrderStatus, OrderStatusEnum.DISPATCH.getCode())
                );

                log.info("支付成功，支付宝交易号：{}，合并支付单：{}", aliResp.getTradeNo(), paySn);

                return Result.success("支付成功");
            }

            // 8. 其他状态（WAIT_BUYER_PAY 等）
            return Result.success("支付处理中");

        } catch (Exception e) {
            log.error("查询支付状态异常，orderSn={}", orderSn, e);
            return Result.error("系统异常");
        }
    }

    // 抽离支付宝查询（便于重试）
    private AlipayTradeQueryResponse queryAlipayStatus(String paySn) {
        try {
            initAlipayClient();
            return Factory.Payment.Common().query(paySn);
        } catch (Exception e) {
            log.error("调用支付宝查询接口异常，paySn={}", paySn, e);
            return null;
        }
    }

    // 过期关闭逻辑（可复用）
    private void closePaymentAndOrders(Payment payment, List<Order> orders) {
        payment.setPayStatus(PayStatusEnum.CANCEL.getCode());
        paymentMapper.updateById(payment);

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .in(Order::getOrderSn, orders.stream().map(Order::getOrderSn).toList())
                .set(Order::getPayStatus, PayStatusEnum.CANCEL.getCode())
                .set(Order::getOrderStatus, OrderStatusEnum.CANCEL.getCode())
        );
    }


    @Override
    public void pendingOrder(Long userId, Order order) {

    }

    @Override
    public boolean isOrderAlreadyPending(Long userId) {
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 1. 查询订单信息
    // 2. 验证所有订单属于同一支付记录
    // 3. 调用支付宝退款API
    // 4. 更新订单状态为已退款
    // 5. 更新支付状态为已退款
    public Result<String> refund(List<String> orderSn) {
        if (CollUtil.isEmpty(orderSn)) {
            return Result.error("订单号列表不能为空");
        }
        try{
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .in(Order::getOrderSn, orderSn)
                    .eq(Order::getPayStatus, PayStatusEnum.PAYED.getCode())
            );

            if (orders.isEmpty()) {
                return Result.error("未找到可退款订单");
            }

            String paySn = orders.getFirst().getPaySn();
            if (orders.stream().anyMatch(order -> !order.getPaySn().equals(paySn))) {
                return Result.error("订单不属于同一支付记录，无法批量退款");
            }

            // 查询支付记录获取退款金额
            Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getMergePaySn, paySn)
                    .eq(Payment::getPayStatus, PayStatusEnum.PAYED.getCode())
                    .last("FOR UPDATE")
            );

            if (payment == null) {
                return Result.error("支付记录不存在或已退款");
            }

            BigDecimal refundAmount = orders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (refundAmount.compareTo(payment.getTotalAmount()) != 0){
                return Result.error("退款金额异常");
            }
            String refundSn = "REFUND" + "-" + paySn;
            initAlipayClient();
            String refundReason = "用户申请退款";
            AlipayTradeRefundResponse response = Factory.Payment.Common()
                    .optional("refund_reason",refundReason)
                    .optional("out_request_no",refundSn)
                    .refund(paySn,refundAmount.toPlainString());

            if (!ResponseChecker.success(response)) {
                log.error("支付宝退款失败: paySn={},refundSn={},code={},subCode={},msg={}",
                        paySn, refundSn,response.getCode(), response.getSubCode(), response.getSubMsg());
                return Result.error("退款失败：" + response.getSubMsg());
            }

            payment.setPayStatus(PayStatusEnum.REFUND.getCode());
            paymentMapper.updateById(payment);

            orderMapper.update(null,new LambdaUpdateWrapper<Order>()
                    .in(Order::getOrderSn, orderSn)
                    .set(Order::getPayStatus, PayStatusEnum.REFUND.getCode())
                    .set(Order::getOrderStatus, OrderStatusEnum.REFUND.getCode())
                    .set(Order::getCancelTime, LocalDateTime.now())
                    .set(Order::getCancelReason,refundReason));

            log.info("退款成功 paySn={} refundSn={} amount={} orders={}",
                    paySn, refundSn, refundAmount, orderSn);

            return Result.success("退款成功,预计1-7个工作日内到账");

        }catch (Exception e){
            log.error("退款异常 orderSn={}", orderSn, e);
            throw new RuntimeException("退款处理失败",e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> closeOrder(List<String> orderSn) {
        if (CollUtil.isEmpty(orderSn)) {
            return Result.error("订单号列表不能为空");
        }
        try{
            // 1. 查询订单信息
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .in(Order::getOrderSn, orderSn)
                    .eq(Order::getPayStatus, PayStatusEnum.PENDING.getCode())
                    .last("FOR UPDATE")
            );

            if (orders.isEmpty()) {
                return Result.error("未找到待支付订单");
            }
            if (orders.size() != orderSn.size()) {
                return Result.error("部分订单不可取消");
            }

            String paySn = orders.getFirst().getPaySn();
            if (StringUtils.isBlank(paySn)) {
                return Result.error("订单未创建支付，无法取消");
            }

            if (orders.stream().anyMatch(order -> !order.getPaySn().equals(paySn))) {
                return Result.error("订单不属于同一支付记录，无法批量关闭");
            }
            Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getMergePaySn, paySn)
                    .eq(Payment::getPayStatus,PayStatusEnum.PENDING.getCode())
                    .last("FOR UPDATE"));
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            boolean aliPayClosed = true;

            if (StringUtils.isNotBlank(paySn)) {
                try {
                    initAlipayClient();
                    AlipayTradeCloseResponse response = Factory.Payment.Common().close(paySn);

                    if (!ResponseChecker.success(response)) {
                        log.error("支付宝关闭订单失败: {},code = {}",response.getMsg(),response.getCode());
                        return Result.error("支付宝订单出现异常，取消失败");
                    }
                    log.info("支付宝关闭订单成功: {} ,code = {}",response.getMsg(),response.getCode());
                } catch (Exception e) {
                    log.error("支付宝接口异常 paySn={}",paySn, e);
                    aliPayClosed = false;
                }
            }
            if (!aliPayClosed) {
                throw new RuntimeException("支付宝订单关闭失败，取消终止");
            }
            payment.setPayStatus(PayStatusEnum.CANCEL.getCode());
            paymentMapper.updateById(payment);

            orders.forEach(order -> {
                order.setPayStatus(PayStatusEnum.CANCEL.getCode());
                order.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("用户主动取消订单");
                orderMapper.updateById(order);
            });
            log.info("订单取消成功 paySn={} orders={}",paySn,orders);
            return Result.success("取消成功");
        }catch (Exception e){
            log.error("支付宝关闭订单异常：{}", e.getMessage());
            throw new RuntimeException("取消失败",e);
        }
    }
}
