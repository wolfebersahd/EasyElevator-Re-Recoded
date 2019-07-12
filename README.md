# EasyElevator Recoded

Welcome to EasyElevator Recoded - a continuation of the [continuation of the popular EasyElevator plugin!](https://www.spigotmc.org/resources/easyelevator-1-11.40360/)


# Installation

1. Place the jar in your `plugins/` folder,
2. Run the server, type in `stop` to stop it.

After the first run, a configuration file should appear in `plugins/EasyElevator/config.yml`.


# Configuration

For available strings referring to Materials in the current version (1.12), visit [The PaperMC docs](https://papermc.io/javadocs/paper/1.12/org/bukkit/Material.html)

`maxPerimeter` - Integer - maximum perimeter of a floor, expressed in the number of blocks present in the ring
`maxFloors` - Integer - maximum number of floors in a single elevator
`arrival.playSound` - Boolean - whether to play a noteblock sound on arrival
`arrival.sendMessage` - Boolean - whether to send a message to players on an elevator on arrival
`blocks.border` - String - Material to be used for the upper and lower elevator border
`blocks.floor` - String - Material to be used for rings designating floors
`blocks.outputFloor` - String - Material to be optionally used for floor rings, which will change to a redstone torch upon arrival (see tutorial)


# Permissions

`easyelevator.admin` - all permissions
`easyelevator.reload` - reload command
`easyelevator.stop.cmd` - use commands to choose a floor to stop at
`easyelevator.stop.sign` - use signs to choose a floor to stop at
`easyelevator.stop.*` - both of the above
`easyelevator.stop` - same as above
`easyelevator.call.cmd` - use commands to call an elevator
`easyelevator.call.sign` - use signs to call an elevator
`easyelevator.call.*` - both of the above
`easyelevator.call` - same as above


# Usage

For plugin usage and functionality, refer to the tutorial below (TODO):

[![Tutorial](https://img.youtube.com/vi/SBeYzoQPbu8/0.jpg)](https://www.youtube.com/watch?v=SBeYzoQPbu8)
