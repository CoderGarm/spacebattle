# Combat and missions

Every fleet will have a mission.

The mission types are a way to define the best outcome of the fleet clash for the fleet's owner. The mission types will be the base of the decision-making for several combat
phases.

## Mission types

1. seek and destroy
    - the simplest way to attack somebody directly
    - the fleet tries to outmaneuver it's opponent in a way that the opponent will gets the maximized damage
    - the fleet will attack a hidden fleet on detection

   ###### allowed rules of entry into combat: [Protective](#protective), [Offensive](#offensive)


2. hide and sneak
    - hide from all opponents in the area
    - the fleet tries to stay in the area without being detected
    - the fleet will avoid contact and tries to escape instead of initiating a combat on detection

   ###### allowed rules of entry into combat: [Defensive](#defensive), [Offensive](#offensive)


3. peaceful presence
    - show presence but stay peaceful
    - in the fleet owner's or allied system it switches implicit to **seek and destroy**
    - in a foreign system the fleet will tries to evade and escape

   ###### allowed rules of entry into combat: [Assertive](#assertive), [Protective](#protective)

## Code of conduct or rules of entry into combat

The code of conduct defines how the captain or flag officer will present her or his fleet to possible opponents.

This will specify that the fleet will try to escape from the battlefield or will try to defeat the opponent.

###### Defensive

- the flag officer will try to evade any combat and **escape** if the opponent will start a fight

###### Assertive

- the flag officer will start an argument but not a fight
- the flag officer will try to evade any combat if the opponent will tries to start a fight

###### Protective

- the flag officer will start an argument but not a fight
- the flag officer will fight if the opponent start it

###### Offensive

- the flag officer will start an argument and, if needed, the fight

# Combat system

1. ###### enter cage outside the weapon range
    1. [fleet movement](#fleet-movement)
    2. [missile movement](#missile-movement)
    3. [incoming weapon fire](#incoming-weapon-fire)
    4. [fire weapons](#fire-weapons)

    - **repeat until one side has won**
2. ###### leaving cage

## Phase definition:

1. ### fleet movement

   ###### move fleets by initiative - the better, the later

    1. determine initiative for all moving fleets
        1. detect the best [movement motivation](#movement) to suit the [mission type](#mission-types)
            - the fleet detects incoming weapon fire, the opponent's distance and movement to state the situation
        2. perform movement

        - **repeat until every fleet has moved**


2. ### missile movement
    1. detect distance of salvo to the target
    2. detect if missiles are in **ELOKA**-range
        1. reduce salvo which are in **ELOKA**-range
    3. detect if missiles are in **COUNTER MISSILE**-range
        1. reduce salvo which are in **COUNTER MISSILE**-range
    4. move salvo towards their target

    - **repeat until every salvo has moves**


3. ### incoming weapon fire
    1. separate incoming weapon hits by weapon type
        1. detect chance to hit by fire control solution and target's last movement
            - **BEAMS**
                1. all **BEAMs** are in range for a direct hit to their target
                2. reduce all BEAMS about the missing weapons
                3. hit region defined by line of sight
            - **MISSILE**
                1. detect which missiles are in explosion range
                2. fire the target's **POINT DEFENSE** and **COUNTER MISSILE**
                3. reduce the amount of incoming missiles by the anti-missile-hits
                4. hit region defines by fire control solution
    2. detect hit region by fire control solution and target's last movement and **project damage**

    - **repeat until every weapon has processed**


4. ### fire weapons

   ###### loose off weapons by initiative - the better, the later

    1. determine initiative for all combat capable fleets
        1. the fleet detects incoming weapon fire, the opponent's distance and movement to state the situation
        2. detect which weapon systems are in alignment to the target
            - detect best fire scenario to suit the mission type
                - combat control system will allocate missile control channels between anti missile combat or offensive weapon systems
        3. create fire control solutions for every single weapon to their specific target
        4. loose off all weapon systems

        - **repeat until every fleet has fired**

### Movement

###### movement motivations:

1. escape movement
    - leaving the combat area
    - maximal reduction of the bombardment time / weapon phases
2. stay out of weapon range
    - staying in the combat area
    - actively moving out of weapon range
    - maximal reduction of the bombardment time / weapon phases
3. do not harm
    - do not lower the distance to the opponent
4. maximize damage projection
    - the fleet will evaluate their own abilities against the opponent and chose the best distance

###### movement types:

1. hold distance
2. reduce distance
3. increase distance

   **all distance moves:**
    - possibly changes the distance to the opponent
    - normal ability to fire weapon systems
    - normal chance of being hit
4. wedge protection
    - keeps the last course and speed
    - rolls and yaws the ships to put the sidewall between the incoming weapons and themselves
    - reduced ability to fire weapon systems nearly to zero
    - reduced chance of being hit nearly to zero
5. offensive roll
    - keeps the last course and speed
    - rolls and yaws the ships to put the most effective weapon systems towards the foe
    - increases ability to fire weapon systems to maximum
    - increases chance of being hit to maximum
6. evasion movement
    - possibly changes the distance to the opponent
    - reduced ability to fire weapon systems
    - reduced chance of being hit

### Decision-making

#### continue or abort a combat

