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
run with [%path_to_java8%]/bin/java -jar ./fronTEENd.jar
on first run, you need to specify path to the 7zip binary (not included, www.7zip.org or apt get install 7z)

Q: what's the 'Use custom parameters' checkbox for ?
A: some emulators (i.e. daphne) need customized parameters different for each romset. When such box is checked, you can edit the parameters before running the rom
without it to affect the original stored settings.

Q: what's the 'Read/Write support' checkbox for ?
A: if the emulator uses r/w images (i.e. c64/amiga/... emulators which uses floppy images), you may want them to be read/write.
To not touch the original image, fronTEENd copies the selected rom/set to its private (%HOME%/fronteend/%EMUNAME%) folder
and uses the copied image to run the emulator. If an emulator is run with 'Read/Write support' enabled, the image is not
deleted at emulator's exit, so the next time you will run the same image the copied one will be used instead. Use the
'Clear Read/Write folder' button to delete the copy.

Q: may i improve/fork it ?
A: sure! just quote me as the original author/idea :)

Q: how do i add my own emulator definitions ?
A: here is a sample json, fronTEENd should work with almost any emulator supporting execution from the commandline.
Once you have the json for your emulator of choice, simply copy it in your %home%/.fronteend/emudefs folder

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

  // parameters to launch the emulator with. use %1%, %2%,... for the rom names/absolute paths and
  // %path% (or %pathsep% for it to be os-dependent-slash-terminated) for the roms folder path
  "emuParams" : "%1% vldp -framefile /media/valerino/daphne/vldp_dl/lair/lair.txt -homedir /media/valerino/daphne -ignore_aspect_ratio -blank_searches -min_seek_delay 1000 -seek_frames_per_ms 20 -fastboot -bank 0 11011001 -bank 1 00100111 -sound_buffer 2048",

  // true to strip path from the rom path (%path% should be specified aswell)
  "stripPath": false,

  // true to not check for exitcode when executing the emulator (may hide parameter errors, doublecheck executing
  // from commandline)
  "noCheckReturn" : false,

  // true to enable r/w support : if the emulator uses r/w images (i.e. c64/amiga/... emulators which uses floppy images),
  // you may want them to be read/write.to not touch the original images, fronTEENd copies the selected roms/sets to its
  // private (%HOME%/.fronteend/%EMUNAME%) folder and uses the copied image in the emulator.
  // if an emulator is run with 'Read/Write support' checkbox enabled and its rwSupport is true, the image/ss is/are not
  // deleted at emulator's exit and will be available at next run.
  "rwSupport": false,

  // true to allow multiple selection in emulators allowing i.e. more than one disk to be loaded
  // at the same time (amiga, multiple drives). you can use %1%, %2%, ... in the params string to represent
  // the rom/sets.
  "allowMultiSelect": false
}
----cut here---

Q: may i improve/fork it ?
A: sure, just quote me for the original idea/author :)

Q: emulator <name> seems to not work with fronTEENd. WTF?!
A: solution 1: check the emulator from plain commandline first, read its documentation (some emulators (i.e. mame) needs some configuration files to be set before running for the first time).
solution 2: improve it yourself and contribute to the project!

Q: params in the emulator definitions do not work
A: probably they need to be edited with your paths (i.e. for daphne, -homedir must be set to your daphne folder).

Q: not all roms are working with emu <name>
A: sometimes an emulator needs specific parameters for different roms, try to use the custom parameters checkbox (i.e. daphne needs a different framefile for each rom, read its documentation)

