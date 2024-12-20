--[[
脚本名称: subtract_if_non_negative
描述: 减少hash表中指定字段的数值，确保结果不为负数
参数:
  KEYS[1]: hash表的键名
  ARGV[1]: hash表的字段名
  ARGV[2]: 要减少的值
返回: 
  - 减法后的结果（如果成功）
  - nil（如果结果会小于0或key不存在）
  - false（如果存储的值不是数字）
]]--
local key = KEYS[1]
local field = ARGV[1]
local delta = tonumber(ARGV[2])

-- 获取当前值
local current = redis.call('hget', key, field)

-- 检查key是否存在
if not current then
    return nil
end

-- 转换为数字并检查
current = tonumber(current)
if not current then
    return false
end

-- 检查结果是否会小于0
if current < delta then
    return nil
end

-- 执行减法并更新
local result = current - delta
redis.call('hset', key, field, result)
return result