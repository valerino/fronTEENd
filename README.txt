Q: what is it ?
A: a simple and lightweight generic emulator fronteend without the usual bloat. quickly hacked-up in a bunch of hours.

Q: why ?
A: the others i found lacked the ease of use i wanted: run fronteend, choose emulator, choose rom, play.
moreover, fronTEENd supports compressed sets (through 7zip binary, not included), merged sets (a compressed file holding more than a rom) and a simple built-in
web-browser which automatically google-search for the selected rom.

Q: why fronTEENd ?
A: it's a frontend for emulators. and emulators remind me of my teen years, when i spent hours with c64, spectrum, amiga and older computers.

Q: why JavaFX ?
A: it's portable, and who knows ..... one day it could run on android, if they port it :)

Q: how do i use it ?
A: recompile (you need the Java8 JDK and IntelliJ IDEA) or just download the jar.
run with [%path_to_java8%]/bin/java -jar ./fronTEENd.jar. the "emudefs" folder with json emulator definitions must be in the same path as the jar.
on first run, you need to specify path to the 7zip binary (not included, www.7zip.org or apt get install 7z)

Q: what's the 'Use custom parameters' checkbox for ?
A: some emulators (i.e. daphne) need customized parameters different for each romset. When such box is checked, you can edit the parameters before running the rom
without it to affect the original stored settings.

Q: may i improve/fork it ?
A: sure! just quote me as the original author/idea :)

Q: how do i add my own emulator definitions ?
A: here is a sample json, fronTEENd should work with almost any emulator supporting execution from the commandline
----cut here----
{
  // this is the display name for the emulator
  "name" : "Daphne (LaserDisc emulator)",

  // this is an optional predefined romset for emulators working with a fixed romset and
  // not having a mame-like command to retrieve the romset automatically.
  // each element of the object is a display name with an array value representing its releated romset names.
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
    "Dragon's Lair (NTSC)" : [ "lair", "lair_f", "lair_e", "lair_d", "lair_c", "lair_b", "lair_a", "lair_x" ],
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

  // emulated system (this is to refine the google search (google is searched with "romname system")
  "system" : "arcade",

  // the last rom folder used, for folder based emulator
  "lastFolder" : "",

  // path to the emulator binary (sometimes you have to set environment variables for the emulator to work: do it in a shell script/bat file
  // which sets the needed environment and run the emulator, and use that for emuBinary)
  "emuBinary" : "/home/valerino/Downloads/daphne/daphnebin.sh",

  // parameters to launch the emulator with. use %rom% for the rom name/absolute path(automatically choosen) and
  // %rompath% (or %rompathslash for it to be os-dependent-slash-terminated) for the rom folder path
  "emuParams" : "%rom% vldp -framefile /media/valerino/daphne/vldp_dl/lair/lair.txt -homedir /media/valerino/daphne -ignore_aspect_ratio -blank_searches -min_seek_delay 1000 -seek_frames_per_ms 20 -fastboot -bank 0 11011001 -bank 1 00100111 -sound_buffer 2048",

  // true to strip path from the %rom% above (%rompath% should be specified aswell)
  "stripPath": false,

  // true to not check for exitcode when executing the emulator (may hide parameter errors, doublecheck executing
  // from commandline)
  "noCheckReturn" : false
}
----cut here---

Q: may i improve/fork it ?
A: sure, just quote me for the original idea/author :)

Q: emulator <name> seems to not work with fronTEENd. WTF?!
A: solution 1: check the emulator from plain commandline first, some emulators (i.e. mame) needs some configuration files to be set before running for the first time.
solution 2: improve it yourself and contribute to the project!

Q: paths in the emulator definitions refers to your folders, and obviously do not work on my setup
A: i know. just replace the strings with "" and rerun the frontend: you will be asked for your paths


