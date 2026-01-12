-- 商品库存扣减
-- ARGV[1]: product stock key (product:stock)
-- ARGV[2]: product stock hash key ("product:" + productId)
-- ARGV[1]: 需要扣减的库存数量
-- 返回值 ：商品不存在 1  库存不足 2  成功 0

local stock_key = "product:stock"
local stock_hash_key = "product:" ..  ARGV[1]
local deduct_amount = tonumber(ARGV[2])

local current_stock = redis.call('HGET', stock_key, stock_hash_key)

if current_stock == false then
    return 1 --商品不存在
end

current_stock = tonumber(current_stock)

if current_stock < deduct_amount then
    return 2 --库存不足
end

--扣减库存
local new_stock = current_stock - deduct_amount
redis.call('HSET', stock_key, stock_hash_key, new_stock)
return 0