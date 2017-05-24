# VTM
VTM - system for Verdensteatret ( www.verdensteatret.com )
This code is in a state of severe alpha and early development. That means that restructuring the class tree, and renaming, adding and removing methods/classes may – and will – happen.

## requirements

* sc3-plugins
* UnitTesting quark
* API quark
* JoshMisc quark


## install VTM

### osx

* Install [SuperCollider](http://supercollider.github.io/download)
* Install [SC3-plugins](https://github.com/supercollider/sc3-plugins)
* Start SuperCollider and run `Quarks.install("UnitTesting")` and `Quarks.install("API")` and `Quarks.install("JoshMisc")`
* `git clone https://github.com/blacksound/VTM.git`
* In SuperCollider Preferences / Interpreter menu, include the path to the VTM / Classes folder

### raspberry pi

* Install SuperCollider from <https://github.com/redFrik/supercolliderStandaloneRPI2> (sc3-plugins are included)
* Start sclang and do `Quarks.install("UnitTesting")` and `Quarks.install("API")`
* `git clone https://github.com/blacksound/VTM.git`
* Add the VTM / Classes folder path to under `- includePaths` in the `sclang_conf.yaml` file.
  - Run `Platform.userAppSupportDir` in SuperCollider to see where this file is located.
  - for supercolliderStandaloneRPI2: `nano ~/supercolliderStandaloneRPI2/sclang.yaml` and add `- /home/pi/VTM/Classes` under includePaths

for more detailed instructions see <https://github.com/blacksound/VTM/wiki/Raspberry-Pi-Instructions>

## examples

see <http://github.com/blacksound/VTMSketches>

### misc

![alt text](https://oddodd.org/lib/VTM/VTM.png "VTM")
![alt text](https://oddodd.org/lib/VTM/VTM_old.png "VTM")
