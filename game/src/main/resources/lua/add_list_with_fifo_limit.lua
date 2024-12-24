--[[
脚本名称: add_list_with_fifo_limit
描述: 向列表添加元素，如果超过大小限制则移除最老的元素
参数:
  KEYS[1]: 列表的键名
  ARGV[1]: 要添加的元素
  ARGV[2]: 列表大小限制
返回: 
  - 被移除的元素（如果有的话）
  - nil（如果没有元素被移除）
]]--
local key = KEYS[1]
local element = ARGV[1]
local maxSize = tonumber(ARGV[2])

local size = redis.call('llen', key)
local removed = nil

if size >= maxSize then
    -- 移除最老的元素（列表左端）
    removed = redis.call('lpop', key)
end

-- 在列表右端添加新元素
redis.call('rpush', key, element)

return removed