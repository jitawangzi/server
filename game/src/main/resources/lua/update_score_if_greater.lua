--[[
脚本名称: update_score_if_greater
描述: 更新有序集合中成员的分数，仅当新分数大于现有分数时
参数:
  KEYS[1]: 有序集合的键名
  ARGV[1]: 要更新的成员
  ARGV[2]: 新的分数
返回: 更新后的分数（如果更新成功），或原有的分数（如果未更新）
]]--
local key = KEYS[1]
local member = ARGV[1]  -- 保持类型
local newScore = tonumber(ARGV[2])

local currentScore = redis.call('zscore', key, member)
currentScore = currentScore and tonumber(currentScore) or nil

if not currentScore or newScore > currentScore then
    redis.call('zadd', key, newScore, member)  
    return newScore
else
    return currentScore
end