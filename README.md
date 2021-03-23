University Gatherer Game
=====================================

The game was made to meet a set of requirements, the main aspects that I added that wasn't in the spec were:

- AI path finding (A-star)
- RPG elements (attacking enemies, ST, HP, etc)
- Sound and music
- Raybased collision checking
- Map loading/saving, an ingame map editor and biome changer
- Enemies lol
- Among many other things

The specification didn't give me free reign to do whatever I wanted with the project so the cooler parts of the project were there just because I could fit them around the spec or they made my life easier when making the game itself.

I shall warn you that the game was never refactored and the code could definetly do with some optimizations and general code cleanup! This is just here for those that would like to make it better or use it in their future endeavours 


Get Started
------------
IMPORTANT NOTE: Before attempting to run the game, please place all the contents of this github into a folder called "main", this is required from processing, since the main entry point is called "main" in the project.

The game was made in a Java rendering system called "Processing" so you'll need at least [Processing](https://processing.org/download/) in order to run the program. Once you have accuired the program, simply open the "main" folder (See the above note if you're missing the folder) in the IDE and click run. Everything should be in order.


If you wish to run the game using visual studio code then get the  [Processing Language](https://marketplace.visualstudio.com/items?itemName=Tobiah.language-pde) extension and link it to the downloaded processing-java file (not needed if installed in PATH, or on linux/mac? maybe). Then you can start the game from inside of visual studio code and skip the Processing IDE (cos it's a little lacklustre)


Usage
----

The included readme will give you the controls and the main rundown of what to expect from the game. However, here are some extra controls to control the debug mode:

First enable debug mode in the game options  
-- B to change biome in the map editor  
-- Left click / right click to control the times  
-- Press Enter? to save the map. Tbh I haven't opened the game in a long time 



Contributing
------------

1. Check the open issues or open a new issue to start a discussion around
   your feature idea or the bug you found
2. Fork the repository and make your changes
3. Open a new pull request

Feel free to message me on discord about PR wait times or general queries: Bone#6969

