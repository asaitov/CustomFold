# CustomFold
Arduino IDE 1.8 plugin to manually specify foldable code blocks

## Build
Prerequisites:
* Installed Arduino IDE 1.8
* Java 8 or above.
```shell
ARDUINO_PATH=[path_to_arduino_ide] ./dist.sh
```

## Install
Unzip contents of `bin/CustomFold.zip` to `tools` subdirectory of Arduino IDE installation directory.

## Use
* Select multiple lines of code and press `Ctrl+Alt+F` to make this block foldable.
* Put cursor on a line within a foldable block and press `Ctrl+Alt+F` to make it unfoldable back.

