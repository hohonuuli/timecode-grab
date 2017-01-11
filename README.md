![MBARI logo](src/site/resources/images/logo-mbari-3b.png)
# timecode-grab

Simple app for grabbing timecode and userbits date from VCRs that support Sony's 9-pin interface. It assumes that you are writing date to userbits as epoch seconds. 

It generates a text file with three columns: timecode, timestamp and epochseconds. _Timecode_ is the tape timecode (assumed to be NTSC (29.97 fps)). _Timestamp_ is the date that that frame of video was recorded. _Epoch seconds_ is the same info as timestamp, just encoded as UNIX time. 

The way it works is that it starts playing the tape and sampling the timecode/timestamp at some interval that you specify (using `-i` flag). The sampling will continue for some duration (specified with the `-d` flag). You also provide a tape length in minutes (using `-t` flag). This program assumes that the tape was recorded continously without stopping and starting in the middle. From your tape sample it will extrapolate the timecode/timestamp to the end of the tape and dump the output to the console. This output can be loaded into a datastore for lookup later.

## Build

This project is built using [SBT](http://www.scala-sbt.org/). To build just run:

`sbt assembly`

The executable jar file will be located in `target/scala-2.12/`

## Usage

To display help run:

`java -jar timecode-grab-assembly-[version].jar`

This example will connect using the commport `/dev/cu.RS422 Deck Control` and sample the tape for 10 seconds at 2 second intervals. The results will have samples every 2 seconds for 45 minutes.

```
java -jar timecode-grab-assembly-[version].jar \
    "/dev/cu.RS422 Deck Control" \
    -d 10 -i 2 -t 45
```



