--[[
脚本名称: add_set_batch_with_limit
描述: 向集合批量添加元素，但限制集合大小不超过指定值
参数:
  KEYS[1]: 集合的键名
  ARGV[1]: 集合大小限制
  ARGV[2+]: 要添加的元素列表
返回: 
  - 成功添加的元素数量
  - -1: 如果添加这些元素会超出限制
]]--
local key = KEYS[1]
local maxSize = tonumber(ARGV[1])
local currentSize = redis.call('scard', key)
local newElements = #ARGV - 1  -- 减去maxSize参数

if (currentSize + newElements) > maxSize then
    return -1
end

local added = 0
for i = 2, #ARGV do
    added = added + redis.call('sadd', key, ARGV[i])
end
return added