--[[
脚本名称: zset_copy_topN
描述: 将源有序集合的前 N 名复制到目标有序集合，支持可选的 member 最小值过滤
参数:
  KEYS[1]: 源 ZSET key
  KEYS[2]: 目标 ZSET key
  ARGV[1]: topN (必填) 要扫描的个数 > 0
  ARGV[2]: clearDest (可选，默认 1) 是否在复制前清空目标 1/0
  ARGV[3]: expireSeconds (可选，默认 0) >0 则对目标 key 设置过期时间(秒)
  ARGV[4]: minMemberValue (可选) 如果存在，只有 tonumber(member) >= 此值才复制

返回: {copiedCount, totalSourceSize}
]]--

local src   = KEYS[1]
local dest  = KEYS[2]
local topN  = tonumber(ARGV[1])
local clearDest = tonumber(ARGV[2] or "1")
local expireSeconds = tonumber(ARGV[3] or "0")
-- 获取可选的筛选阈值
local minMemberVal = nil
if ARGV[4] then
    minMemberVal = tonumber(ARGV[4])
end

if topN == nil or topN <= 0 then
  return {err="topN must be > 0"}
end

-- 1. 清空目标
if clearDest == 1 then
  redis.call('DEL', dest)
end

local size = redis.call('ZCARD', src)
if size == 0 then
  if expireSeconds and expireSeconds > 0 then
    redis.call('EXPIRE', dest, expireSeconds)
  end
  return {0, 0}
end

local n = topN
if n > size then
  n = size
end

-- 2. 取前 n 名
local entries = redis.call('ZREVRANGE', src, 0, n - 1, 'WITHSCORES')

local copiedCount = 0
if #entries > 0 then
  local args = {dest}
  
  for i = 1, #entries, 2 do
    local memberStr = entries[i]
    local score  = entries[i+1]
    local shouldCopy = true

    -- 3. 如果设置了阈值，进行过滤检查
    if minMemberVal then
        local memberNum = tonumber(memberStr)
        -- 如果 member 转不成数字，或者小于阈值，则不复制
        if not memberNum or memberNum < minMemberVal then
            shouldCopy = false
        end
    end

    if shouldCopy then
        table.insert(args, score)
        table.insert(args, memberStr)
        copiedCount = copiedCount + 1
    end
  end
  
  -- 4. 执行 ZADD (只有当 args 长度大于 1，即有实际元素时)
  if #args > 1 then
    redis.call('ZADD', unpack(args))
  end
end

-- 5. 设置过期
if expireSeconds and expireSeconds > 0 then
  redis.call('EXPIRE', dest, expireSeconds)
end

return {copiedCount, size}