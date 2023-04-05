# BukkitHashWarp
 
Made for request:
https://bukkit.org/threads/hash-warp.500574/

Warps players to random locations, the locations can always be revisited with the same warp seed though.
Hashes are by design irreversible. This should be functiuonally the same.
If this is to be used on a massive player, a redisign should be done with usage of a database. All these files might really slow down the server otherwise.

### Commands:
warp <seed>\
permission: hashwarp.use DEFAULT\
Seed can be anything.

warpchangeradius <world> <newradius>\
permission: hashwarp.admin OP\
Might add deletion of all warppoints for the world in the future

warpreverse <X> <Z> [world]\
permission: hashwarp.reverse DEFAULT\
World not required if sender is a player, looking in the current world in that case.

warplist [world]\
permission: hashwarp.list\
World not required if sender is a player, looking in the current world in that case.
