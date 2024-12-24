--[[
脚本名称: add_set_with_limit
描述: 向集合添加元素，但限制集合大小不超过指定值
参数:
  KEYS[1]: 集合的键名
  ARGV[1]: 要添加的元素
  ARGV[2]: 集合大小限制
返回: 
  - 1: 添加成功
  - 0: 由于达到限制而添加失败
]]--
local key = KEYS[1]
local member = ARGV[1]
local maxSize = tonumber(ARGV[2])

local size = redis.call('scard', key)
if size >= maxSize then
    return 0
end

return redis.call('sadd', key, member)