# LightShow

A program to generate LED patterns for PixelPusher.

## Build & Run

    $ ./gradlew jar
    ...
    BUILD SUCCESSFUL in 5s
    $ java -jar lighting/build/libs/lighting-1.0-SNAPSHOT.jar

## Command Server

Listens for UDP datagrams on port 8422.
Each datagram contains UTF-8 data following the table below:

Current commands:

| Command        | Description   |
| -------------- |:-------------:|
| `'`            | Switch program with random transition effect |
| `f`            | small flash   |
| `g`            | large flash   |
| `u`            | pulse, can take optional intensity float e.g: `u,0.4`         |
| `w`            | horizontal walk |
| `v`            | vertical walk |
| `s`            | Switch to Sparkle program with random transition effect |
| `e`            | Switch to Earth program with random transition effect |
| `p`            | Switch to Purp program with random transition effect |
| `i`            | Switch to Fire program with random transition effect |
| `b`            | Switch to Horizontal Gradient program with random transition effect |
| `o`            | Switch to Vertical Gradient program with random transition effect |
| `r`            | Switch to Rainbow program with random transition effect |
| `=`            | Increase program clock rate |
| `-`            | Decrease program clock rate |

You can also enter any of these commands into the programn's standard input, followed by enter.
Pardon their nonsense: they were chosen for keyboard ergonomics.
