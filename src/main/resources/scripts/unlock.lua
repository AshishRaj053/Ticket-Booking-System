local current = redis.call("GET", KEYS[1])

if current == ARGV[1] then
    redis.call("DEL", KEYS[1])
    return 1
end

return 0