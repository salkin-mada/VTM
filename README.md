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

## general raspberry pi instructions

### install jessie on raspberry pi and amend settings

* burn image or etch .zip to SD card <https://www.etcher.io/>
* boot raspberry ...
* `sudo raspi-config` - go to advanced options and select update
* enable VNC (in raspi-config)
* set gpu_mem to `192` via the desktop environment or edit in `/boot/config.txt`

### how to solve problem with mounting a usb drive:

* `lsusb` - should do the trick
* if not then ..
* mounting usb
  - `ls /dev/`
  - `sudo mkdir /media/usb`
  - `sudo mount -t vfat -o uid=pi,gid=pi /dev/sda1 /media/usb`
  - `ls /media/usb`


### shutdown.py for raspberry pi

* use the following python script
```python
#!/bin/python
import RPi.GPIO as GPIO
import os
pin= 3
GPIO.setmode(GPIO.BCM)
GPIO.setup(pin, GPIO.IN)
try:
    GPIO.wait_for_edge(pin, GPIO.FALLING)
    os.system("sudo halt -p")
except:
    pass
GPIO.cleanup()
```

* then edit crontab
  - `crontab -e`
  - #and add the following…
  - `@reboot python /home/pi/shutdown.py`

### additional

##### if needed.. build and include sc3-plugins on raspberry pi
* if not already done, install cmake
  - `sudo apt-get update && sudo apt-get upgrade`
  - then `sudo apt-get install cmake`
* see <https://github.com/redFrik/supercolliderStandaloneRPI2/blob/master/BUILDING_NOTES.md>
  - `git clone --recursive git://github.com/supercollider/supercollider --depth 1`
  - `git clone --recursive https://github.com/supercollider/sc3-plugins.git --depth 1`
  - `cd sc3-plugins`
  - `mkdir build && cd build`
  - `export CC=/usr/bin/gcc-4.8`
  - `export CXX=/usr/bin/g++-4.8`
  - `cmake -L -DCMAKE_BUILD_TYPE="Release" -DCMAKE_C_FLAGS="-march=armv7-a -mtune=cortex-a8 -mfloat-abi=hard -mfpu=neon"`
  - `-DCMAKE_CXX_FLAGS="-march=armv7-a -mtune=cortex-a8 -mfloat-abi=hard -mfpu=neon" -DSC_PATH=../../supercollider/`
  - `-DCMAKE_INSTALL_PREFIX=~/supercolliderStandaloneRPI2/share/user/Extensions/sc3-plugins ..`
  - `make -j 4` leave out flag ~~-j 4~~ on single core rpi models _(zero,1,2)_
  - `sudo make install`
  - `cd ~/supercolliderStandaloneRPI2/share/system/Extensions/`
  - `sudo chown -R pi SC3plugins`
  - `sudo chgrp -R pi SC3plugins`
  - `mkdir SC3plugins/bin`
  - `mv SC3plugins/lib/SuperCollider/plugins/*.so SC3plugins/bin/`
  - `mv SC3plugins/share/SuperCollider/Extensions/SC3plugins/* SC3plugins/`
  - `rm -rf SC3plugins/lib`
  - `rm -rf SC3plugins/share`
  - `rm -rf SC3plugins/local`

### misc

![alt text](https://oddodd.org/lib/VTM/VTM.png "VTM")
![alt text](https://oddodd.org/lib/VTM/VTM_old.png "VTM")
