--[[
脚本名称: update_if_greater
描述: 如果新值大于当前值，则更新
参数:
  KEYS[1]: 要更新的键
  ARGV[1]: 新值
返回: 更新后的值
]]--
local currentValue = redis.call('get', KEYS[1])
if not currentValue or tonumber(ARGV[1]) > tonumber(currentValue) then
    redis.call('set', KEYS[1], ARGV[1])
    return tonumber(ARGV[1])
else
    return tonumber(currentValue)
end