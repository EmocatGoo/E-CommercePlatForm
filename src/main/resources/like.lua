-- KEYS[1] = redis key
-- ARGV[1] = userId

local key = KEYS[1]
local userId = ARGV[1]

if redis.call("SISMEMBER", key, userId) == 1 then
    redis.call("SREM", key, userId)
    return 0  -- 取消点赞
else
    redis.call("SADD", key, userId)
    return 1  -- 点赞
end