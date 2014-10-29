[Q]: What is it ?
[A]: A simple and lightweight generic emulator fronteend without the usual
bloat, fully configurable through JSON and quickly hacked-up in a bunch of
days.

[Q]: Why ?
[A]: The others i found lacked the ease of use i wanted: run fronteend,
choose emulator, choose rom, play.
Moreover, fronTEENd supports compressed sets (through 7zip binary, not
included), merged sets (a compressed file holding more than a rom) and a
simple built-in web-browser which automatically seach Google for the selected
rom.

[Q]: Why fronTEENd ?
[A]: It's a frontend for emulators. And emulators reminds me of my teen years,
when i spent hours with c64, spectrum, amiga and older computers.

[Q]: Why JavaFX ?
[A]: It's portable, and who knows ..... one day it could run on android, if
they port it (i heard of a javafx port already implemented, but haven't tried
yet).

[Q]: How do i use it ?
[A]: Recompile (you need the Java8 JDK and IntelliJ IDEA) or just download the
prebuilt JAR, then run with [%JAVAHOME%]/bin/java -jar /path/to/fronTEENd.jar
On first run, you need to specify path to the 7zip binary (not included, go to
www.7zip.org, or use apt get install 7z for debian-alike distros, or similar).
**NOTE**: Sometimes i may forgot to commit the prebuilt binary (and, it's not
a good practice to use git to store binaries....). I do this just as a favour
for the less skilled users, you're advised to build fronTEENd yourself to
ensure having the latest version!

[Q]: What's the 'Use custom parameters' checkbox for ?
[A]: Some emulators (i.e. daphne) needs customized parameters different for
each romset. When such box is checked, you can edit the parameters before
running the rom without it to affect the emulator's stored settings.

[Q]: What's the 'Read/Write support' checkbox for ?
[A]: If the emulator uses R/W images (i.e. c64/amiga/.../generally all
emulators using writable media images like floppies), you may want them to be
effectively writable, for instance if you play games saving on their disks.
To not touch the original image (and to allow write support if the image is
run from a read-only media as a cd-rom), if the checkbox is enabled fronTEENd
copies the selected rom to its private (%HOME%/fronteend/%EMUNAME%) folder and
uses the copied image to run the emulator with.
If an emulator has been run once with 'Read/Write support' enabled, the image
is not deleted at emulator's exit, so the next time you will run the same
image you will be asked to use the copied one instead, or to delete it and use
the fresh original.
You may also use the 'Clear Read/Write folder' button to delete all the stored
image copies for the current emulator.

[Q]: How do i add my own emulator definitions ?
[A]: Here is a sample json, fronTEENd should work with almost any emulator
supporting execution from the commandline. Once you have the json for your
emulator of choice, simply copy it in your %home%/.fronteend/emudefs folder.

----cut here----
{
  // this is the display name for the emulator
  "name" : "Daphne (LaserDisc emulator)",

  // this is an optional predefined romset for emulators working with a fixed
  // romset and not having a mame-like command to retrieve the romset
  // automatically.
  // each element of the object is a display name with an array value
  // representing its releated romset names.
  "roms" : {
    "Interstellar" : [ "interstellar" ],
    "GP World" : [ "gpworld" ],
    "Dragon's Lair (Enhanced v1.1)" : [ "dle11" ],
    "Super Don Quix-ote" : [ "sdq", "sdq*" ],
    "Bega's Battle" : [ "bega", "begar1" ],
    "Dragon's Lair (NTSC prototype)" : [ "lair_n1" ],
    "Galaxy Ranger" : [ "galaxy", "galaxyp" ],
    "Cobra Command" : [ "cobra", "cobraab", "cobraconv", "cobram3" ],
    "Space Ace (PAL)" : [ "aceeuro" ],
    "Dragon's Lair (PAL)" : [ "lair_euro", "lair_d2" ],
    "Star Blazer" : [ "blazer" ],
    "Space Ace (Enhanced)" : [ "sae" ],
    "Space Ace (NTSC)" : [ "ace", "ace_a", "ace_a2" ],
    "Thayer's Quest" : [ "tq", "tq*" ],
    "Cliff Hanger" : [ "cliff", "cliffalt", "cliffalt2" ],
    "Road Blaster" : [ "roadblaster" ],
    "Dragon's Lair (NTSC)" : [ "lair", "lair_f", "lair_e", "lair_d", "lair_c",
        "lair_b", "lair_a", "lair_x" ],
    "Dragon's Lair (PAL italian)" : [ "lair_ita" ],
    "Dragon's Lair (Enhanced v2.1)" : [ "dle21" ],
    "Space Ace ('91)" : [ "ace91", "ace91_euro" ],
    "Badlands" : [ "badlands", "badlandp" ],
    "Esh's Aurunmilla" : [ "esh", "eshalt", "eshalt2" ],
    "Us Vs Them" : [ "uvt" ],
    "Dragon's Lair 2: Time warp" : [ "lair2", "lair2_*" ],
    "Astron Belt" : [ "astron", "astronp" ],
    "M.A.C.H. 3" : [ "mach3" ]
  },

  // true for mame
  "isMame" : false,

  // emulated system (this is to refine the google search. search is issued
  // using the string '"rom" system')
  "system" : "arcade",

  // the last rom folder used, for folder based emulator
  "lastFolder" : "",

  // path to the emulator binary (sometimes you have to set environment
  // variables for the emulator to work: do it in a shell script/bat file
  // which sets the needed environment and run the emulator, and use that
  // for emuBinary)
  "emuBinary" : "/home/valerino/Downloads/daphne/daphnebin.sh",

  // parameters to launch the emulator with. use %1%, %2%,... for rom
  // names/absolute paths and %path% (or %pathsep% for it to be
  // os-dependent-slash-terminated) for the roms folder path
  "emuParams" : "%1% vldp -framefile /media/valerino/daphne/vldp_dl/lair/lair.txt -homedir /media/valerino/daphne -ignore_aspect_ratio -blank_searches -min_seek_delay 1000 -seek_frames_per_ms 20 -fastboot -bank 0 11011001 -bank 1 00100111 -sound_buffer 2048",

  // true to strip directory from the full rom path
  // (%path% must be specified aswell in this case, i.e. gens)
  "stripPath": false,

  // true to not check for exitcode when executing the emulator
  // (may hide parameter errors, doublecheck executing from commandline,
  // i.e. gens)
  "noCheckReturn" : false,

  // true to enable r/w support (read the above documentation)
  "rwSupport": false,

  // true to allow multiple selection in emulators allowing
  // i.e. more than one disk to be loaded at the same time (amiga with
  // more than one disk drive). you can use %1%, %2%, ... in the params
  // string to represent the rom/sets.
  "allowMultiSelect": false
}
----cut here---

[Q]: Emulator <name> seems to not work with fronTEENd. WTF?!
[A]: Solution 1: check the emulator from plain commandline first, read its
documentation carefully (some emulators like mame needs some configuration
files to be set before running for the first time.
Solution 2: improve it yourself and contribute to the project! :)

[Q]: 'params' in the emulator definitions do not work
[A]: Probably they need to be edited with your paths
(i.e. for daphne, -homedir must be set to your daphne folder).

[Q]: Not all roms are working with emu <name>
[A]: Sometimes an emulator needs specific parameters for different roms,
try to use the Custom Parameters checkbox
(i.e. daphne needs a different framefile for each rom, read its documentation)

[Q]: May i improve/fork it ?
[A]: Sure, just quote me for the original idea/author :)
