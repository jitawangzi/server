--[[
脚本名称: update_hash_conditional
描述: 只有当当前值等于期望值时才进行加减操作
参数:
  KEYS[1]: hash表的键名
  ARGV[1]: hash表的字段名
  ARGV[2]: 期望的当前值
  ARGV[3]: 要增加的值（正数为加，负数为减）
返回: 
  - 更新后的新值（如果更新成功）
  - nil（如果当前值不等于期望值）
]]--
local key = KEYS[1]
local field = ARGV[1]
local expectedValue = tonumber(ARGV[2])
local delta = tonumber(ARGV[3])

local current = redis.call('hget', key, field)
current = tonumber(current)

if current == expectedValue then
    local newValue = current + delta
    redis.call('hset', key, field, newValue)
    return newValue
else
    return nil
end