--[[
脚本名称: increment_with_max
描述: 增加值但不超过最大值
参数:
  KEYS[1]: 要增加的键
  ARGV[1]: 增加的值
  ARGV[2]: 最大值
返回: 更新后的值
]]--
local currentValue = redis.call('get', KEYS[1]) or 0
local newValue = math.min(tonumber(currentValue) + tonumber(ARGV[1]), tonumber(ARGV[2]))
redis.call('set', KEYS[1], newValue)
return newValue