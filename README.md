# BukkitHashWarp
Made for request:
https://bukkit.org/threads/hash-warp.500574/

Warps players to random locations, the locations can always be revisited with the same warp seed though. Hashes are by design irreversible. This should be functiuonally the same. If this is to be used on a server with lots of players, a redisign should be done with usage of a database. All these files might really slow down the server otherwise.

### Commands:
warp &lt;seed&gt;\
permission: hashwarp.use DEFAULT\
Seed can be anything.

warpchangeradius &lt;world&gt; &lt;newradius&gt;\
permission: hashwarp.admin OP\
Might add deletion of all warppoints for the world in the future

warpreverse &lt;X&gt; &lt;Z&gt; [world]\
permission: hashwarp.reverse DEFAULT\
World not required if sender is a player, looking in the current world in that case.

warplist [world]\
permission: hashwarp.list OP\
World not required if sender is a player, looking in the current world in that case.


Donations:
My mods are free. If you want to buy me a cup of coffee as a thank you I would really appreciate it!
https://ko-fi.com/jonathanhoffmann
