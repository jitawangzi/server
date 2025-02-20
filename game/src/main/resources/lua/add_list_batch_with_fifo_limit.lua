--[[
脚本名称: add_list_batch_with_fifo_limit
描述: 向列表批量添加元素，如果超过大小限制则移除最老的元素
参数:
  KEYS[1]: 列表的键名
  ARGV[1]: 列表大小限制
  ARGV[2+]: 要添加的元素列表
返回: 
  被移除的元素数组（JSON格式）
]]--
local key = KEYS[1]
local maxSize = tonumber(ARGV[1])
local removed = {}

local currentSize = redis.call('llen', key)
local toAddCount = #ARGV - 1  -- 减去maxSize参数

-- 计算需要移除多少个元素
local removeCount = math.max(0, currentSize + toAddCount - maxSize)

-- 移除旧元素
for i = 1, removeCount do
    local removedElement = redis.call('lpop', key)
    if removedElement then
        table.insert(removed, removedElement)
    end
end

-- 添加新元素
for i = 2, #ARGV do
    redis.call('rpush', key, ARGV[i])
end

-- 将移除的元素数组转换为JSON字符串
return cjson.encode(removed)