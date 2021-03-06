# Starting a remix

Here are a few simple steps to start out with each time you start a new custom remix.

## 1. Picking music
![Music select button](readme/toolbar/tapalong.png)<br>
To load music, the button on the left side of the toolbar that looks like
a music note. You can load in mp3, ogg, and wav files. It is recommended
to use the ogg file format to reduce save file size.

## 2. Getting the tempo
![Tapalong button](readme/toolbar/tapalong.png)<br>
Most songs have only one tempo. Click the button with a hand touching ripples
to open the Tapalong menu. Play the remix, and start pressing **`T`**
to tap along. The predicted tempo will be shown below. You should aim
for around 50 or more inputs to get a reliable average.

Set the tempo by clicking the Tempo Change tool (or press **`3`** on the number row)
and clicking at beat zero. Scroll to change the tempo.

## 3. Moving the music start tracker
This is a **very important** step. Your remix will *probably be offbeat*
(even with the right tempo!) if you do not set this.

Most songs will have a slight gap of silence at the start, or the beat
in the song will not match the beats in the editor. You have to correct
this by moving the music start tracker *backwards*.

Move the tracker by middle clicking or pressing `CTRL+RCLICK` in the track.
If it doesn't work, make sure you are using the Selection tool (press **`1`**).
To get more precision, click the Snap setting (looks like 1/4) to increase
the fineness of movement.

You can use trial-and-error to correct it, or open up a program that
allows you to view the waveform like Audacity. To test it,
enable the Metronome (in the toolbar) and listen to the remix. If the
clicks line up to the song, you're good to go! Otherwise, keep fiddling
with the music start tracker.

![Metronome](readme/metronome.png)
>Above: The metronome button.

![Music start example](readme/musicStartExample.png)
>Above: An example of moving the music start tracker.
The tracker has been moved back 0.125 seconds.
This accounts for 0.125 seconds of silence at the start of the song.

## 4. Setting a time signature
Time signature is very important in music as it groups together certain beat
intervals. Likewise, the use of measures in Rhythm Heaven is very important,
as certain songs only work in certain time signatures, or only fit in
certain beats of a time signature.

>Note: RHRE3 only supports time signatures in quarter notes (X/4). If
your derived time signature is not X/4, **skip this step**.

The most common time signature is 4/4 time. Other songs (like waltzes)
are in 3/4. Figuring out the time signature of a piece of music is
beyond the scope of this article, but generally most songs are in 4/4.

Set a time signature by selecting the Time Signature tool (press `5`),
clicking at a whole number beat, and setting the number of quarter notes
per measure by scrolling.

![Time signature example](readme/timeSigExStarting.png)

Test playback with the Metronome once again,
and you'll hear a higher pitched click at the start of a measure.
If the higher-pitched clicks go with the song (for example, lyrical
songs tend to have sets of lyrics that start at a measure), you're set.

Note that some games work in certain time signatures only. For example,
Micro-Row's and Monkey Watch's main inputs only fall on the offbeats
of a 4/4 time signature (1 **2** 3 **4**). Most call-and-response games
(excluding Wizard Waltz) operate in a 4/4 time signature.

## 5. Add cues
Now you've come to the creative part -- adding the patterns and cues.
One of the few things to keep in mind is to make it realistic and to
strive to make it [playable][Playalong.md]. Imagine if you were presented with playing
your remix -- would it be possible? Would it be followable with the rhythm?
All these aspects are what make a remix good.

## (Optional) 6. Playalong cues
Starting with v3.17.0, you can add Playalong cues to actually add inputs
to your remix. More info [in its separate doc page.](Playalong.md)

Happy remixing!