## Sneak Preview v18

Many moons have passed since the v17 release, and we had the War Harvest event in between.  
The next season will start around May, the fourth (be with you) and I'm working all the time to improve the game.

The map received some attention due to the not entirely successful wiki integration - I wrote a wikimedia module which works fine on local wikimedia-instances but which is not
allowed by
the could solution.  
So we opted for the second-best idea and started placing images in the wiki-article for a system which has just a link to the map-tool.

I started working on my next idea for the map-tool, too.   
The idea is to take the battle and campaign information from the wiki and build something what I call a 'combat theatre' out of it.  
Next to the system affiliations I want to have a strategical overview about the different stages of the manticorian-havenite war, all of them.
And of course all the smaller conflicts, the prominent pirate encounters and these kind of stuff.

There is some movement in all of these projects.  
Unfortunately, I reached a point where I need to repair the combat module of bfh.  
The main idea behind the game was ever to have a sometimes-funny, sometimes-interesting vehicle to run battles and see how a battlecruiser would play football
(or soccer, depending on your habits) with an angry tube.

Okay, that was a lengthy introduction to the simple fact that I implemented the combat module in the later part of '20 and early '21.  
As I finished that step I was extremely happy that I could have a look to other topics - it was a bloody, bloody mess.

I implemented a quite complex decision-making system to figure out how a fleet could act in order to attack another one. Or try to escape.  
And we got a combat system which do combats - somehow.

I learned a lot about 'space mechanics' - not Kepler's laws or orbital movements (I got that some years earlier at the university).
What I've learned was that the unimaginable, sheer size of a solar system is not made for a single brain. It's just too big.

And the result was, that a fleet is too fast for a single brain, too. Because a fleet live (or die) inside a solar system and of course, it must adhere to the rules.  
Originally I planned to make a small series of articles to point out what I'm currently doing on the track to improve the combat module but let me give an example:

For all the space mechanics it was pretty useful to build up a complex 'physics calculator' for the most basic thing.  
A missile accelerated in gravity-earth but your formular is for meter per second squared? Calculate it in the matching unit.  
A graser has an effective range of 400.000.000 meter and your ships driving in light-seconds per second (yes, it's c)? Calculate it in the matching unit.  
Have you ever tried to turn around a ship with a mass of 6 million tons and a velocity of 0.3 c? If you want to make the decision if it's better to fly a curve or brake to zero and
fly in the opposite direction, you have to calculate it in the matching unit before checking what is better.

And while implementing a physics calculator for all these necessary thing you are stumbling about the fact that a humans brain is analogous and a computer is not. It's digital.

Think about what you've read in the books. Ships are passing each other with Infinite Improbability Velocity, and the graser's targeting computer has to decide if the weapons
should be fired and when.  
I said a computer is digital and our brain is not. So we read this and know what is meant - but when you see ships passing each other in your simulated combat you ask yourself
why they aren't firing lasers at each other.

And then you find out that the length of your combat round allows to pass the other ship with a velocity which leads to a position which is slightly out of range an on both points.
The first and the second combat round has nearly the same distance but the ships have switched the positions with notifying their weapons that they are in range for a split of a
second.

And while you are sitting in front of your computer and proudly coding a physics engine which overshadowed the NASA you find out why the Mars Climate Orbiter crashed.

So, my short example was way too long, but I hope it's clear: The details are important.  
And there are many of them.

Until next release is finished, many moons will pass.

### But this is a sneak preview, lets make some preview stuff.

Since th focus is the combat system, it's pretty clear that I need to fix up the maps of the game, too.  
Using it was - and is until the v18 release - not very nice. With too many fleets and planets you have to search where a system is, focus it and hope that you have remembered
correctly.  
Then you need to find the destination for a move, select both and send them.

This is changed. You have a fleets and system outline to find and select your stuff.

<img src="https://media.battleforhonor.de/preview/v18/move-fleets.gif" alt="wh-1" width="350px">

You can also zoom-toggle through your selection.

<img src="https://media.battleforhonor.de/preview/v18/zoomtoggle-selected-items.gif" alt="wh-1" width="350px">

And as you noted, you can do whatever the menu before allowed now with a right-click for the context menu.  
The free space in the UI can be used better than for buttons.

<img src="https://media.battleforhonor.de/preview/v18/deselect.gif" alt="wh-1" width="350px">

#### Let's start with the interesting part.

I really don't want to present the old combat view - those with accounts know it. It's ugly

The new combat module has a lot of changes under the hood, but the most of them has visual representations.

<img src="https://media.battleforhonor.de/preview/v18/combat-arena.png" alt="wh-1" width="350px">

So, let me explain what you see.

1. The most important but probably easiest to oversee point is, that the maneuvers will follow splines.  
   I couldn't stress the impact of this change enough, but let me say that we will have maneuvers and counter-maneuvers in the future - depending on the skill level of your
   commanding officer or flag staff.
2. Next to the course plots you see that what I call a 'Bizarrometer'. Cou have probably seen a similar thing in the Jayne's Handbooks.  
   At the beginning, they are ugly and only presenting random values - but I want to make them show real sensor data which can be interpreted to learn about the other's ship class
   fittings.
3. Another new thing are the Range Auras.  
   We learned at the harsh way that a fleet in motion has way more range towards than backwards because of their own velocity. These auras represents the range of different weapon
   systems - later more.
4. And because a fleet in the honorverse has a very lazy life and then some very exciting seconds - w see the Combat Round Activity Indicator at the bottom.  
   This helps to skip the travel times on the spline and jump directly to the action - if you want.

Let's see that in action?

<img src="https://media.battleforhonor.de/preview/v18/arena-ui-demo.gif" alt="wh-1" width="350px">

There are many details which have to be adapted, changed and made pretty. I said it earlier: There are many details.

#### But what about the action? What happens?

Yeah, As I said - the space is big. Let's see what it means to fire missiles:

<img src="https://media.battleforhonor.de/preview/v18/closer-look-at-eggs.gif" alt="wh-1" width="350px">

Have you seen that the round are slowing down? Missiles happend, I promise!  
The point is, at these distances you see nothing - zoom closer and closer and you will see nothing again.

The missiles are that fast that they will leave the viewport instantly.  
And they are tiny. Really tiny.

But they are there ;)

<img src="https://media.battleforhonor.de/preview/v18/combat-log.png" alt="wh-1" width="350px">

### Conclusion

Every story has a beginning, a main part and an end.

To be honest, I assume, I'm halfway through.

Since the end of War Harvest in the middle of January I'm working on that stuff.  
Next to the optical improvements, the better usability which I want to achieve there are many details in terms of the mathematics of combat simulation open.

I want to take the shortest path to the next release, but I don't want to take bad abbreviations.

Thanks for reading, thanks for being interested and with me and the game.  
I hope I will see some of you in the next season - which starts obviously only when the combat module has finished ;)

Sincerely,  
the admin
