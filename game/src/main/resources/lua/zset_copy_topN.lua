--[[
脚本名称: zset_copy_topN
描述: 将源有序集合的前 N 名(按分数从高到低)复制到目标有序集合
参数:
  KEYS[1]: 源 ZSET key
  KEYS[2]: 目标 ZSET key
  ARGV[1]: topN (必填) 要复制的个数 > 0
  ARGV[2]: clearDest (可选，默认 1) 是否在复制前清空目标 1/0
  ARGV[3]: expireSeconds (可选，默认 0) >0 则对目标 key 设置过期时间(秒)

返回: {copiedCount, totalSourceSize}
]]--

local src   = KEYS[1]
local dest  = KEYS[2]
local topN  = tonumber(ARGV[1])
local clearDest = tonumber(ARGV[2] or "1")        -- 默认 1: 清空
local expireSeconds = tonumber(ARGV[3] or "0")    -- 默认 0: 不设置过期

if topN == nil or topN <= 0 then
  return {err="topN must be > 0"}
end

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

-- 取前 n 名(高->低)
local entries = redis.call('ZREVRANGE', src, 0, n - 1, 'WITHSCORES')

if #entries > 0 then
  -- 构造 ZADD 参数: ZADD dest score member score member ...
  local args = {dest}
  for i = 1, #entries, 2 do
    local member = entries[i]
    local score  = entries[i+1]
    table.insert(args, score)
    table.insert(args, member)
  end
  redis.call('ZADD', unpack(args))
end

if expireSeconds and expireSeconds > 0 then
  redis.call('EXPIRE', dest, expireSeconds)
end

return {n, size}