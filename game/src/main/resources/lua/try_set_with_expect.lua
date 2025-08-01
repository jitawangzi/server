--[[
脚本名称: try_set_with_expect
描述: 条件设置键值对并指定过期时间，仅当键不存在或当前值与预期值相同时才设置
参数:
  KEYS[1]: Redis键名
  ARGV[1]: 要设置的值
  ARGV[2]: 过期时间（秒）
返回: 1表示设置成功，0表示设置失败（被其他值占用）
]]--
local key = KEYS[1]
local value = ARGV[1]
local expireSeconds = tonumber(ARGV[2])

local currentValue = redis.call('GET', key)

if currentValue == false or currentValue == value then
    redis.call('SETEX', key, expireSeconds, value)
    return 1
else
    return 0
end