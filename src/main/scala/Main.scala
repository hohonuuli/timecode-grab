import java.time.Instant
import java.util.{Timer, TimerTask}
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

import org.docopt.Docopt
import org.mbari.vcr4j.{VideoIndex}
import org.mbari.vcr4j.commands.VideoCommands
import org.mbari.vcr4j.decorators.{SchedulerVideoIO, VCRSyncDecorator}
import org.mbari.vcr4j.jssc.JSSCVideoIO
import org.mbari.vcr4j.rs422.decorators.UserbitsAsTimeDecorator
import org.mbari.vcr4j.time.{FrameRates, Timecode}

import scala.collection.mutable
import scala.collection.JavaConverters._


object Main  {

  def main(args: Array[String]): Unit = {

    // -- Help for user
    val prog = getClass.getName
    val doc = s"""
                 | Usage: Main <commport> [options]
                 |
                 | Options:
                 |   -d DURATION, --duration=DURATION   Duration to sample in seconds [default: 10]
                 |   -i INTERVAL, --interval=INTERVAL   Sampling interval in seconds [default: 2]
                 |   -t LENGTH, --tapelength=LENGTH     Length of the tape in minutes [default: 45]
    """.stripMargin
    val doc2 = s"""
        | Usage: Main <commport>
    """.stripMargin
    val opts = new Docopt(doc).withHelp(true).parse(args.toList.asJava)

    // -- Parse args
    val portName = opts.get("<commport>").asInstanceOf[String]
    val durationSecs = opts.get("--duration")
      .asInstanceOf[String]
      .toDouble
    val intervalSecs = opts.get("--interval")
      .asInstanceOf[String]
      .toDouble
    val lengthSecs = opts.get("--tapelength")
        .asInstanceOf[String]
        .toDouble * 60D

    val durationMillis = Math.round(durationSecs * 1000)
    val intervalMillis = Math.round(intervalSecs * 1000)

    val videoIndices = collectData(portName, durationMillis, intervalMillis)

    dumpData(videoIndices, intervalSecs, lengthSecs)
    System.exit(0)

  }

  def collectData(portName: String, durationMillis: Long, intervalMillis: Long): Seq[VideoIndex] = {

    val statusIntervalMillis = Math.round(intervalMillis / 4D)
    val timecodeIntervalMillis = Math.round(intervalMillis / 3D)

    // -- Configure IO
    val rawIO = JSSCVideoIO.open(portName)
    val io = new SchedulerVideoIO(rawIO, Executors.newSingleThreadExecutor())
    val syncDecorator = new VCRSyncDecorator(io,
      statusIntervalMillis,
      timecodeIntervalMillis,
      intervalMillis)
    val timeDecorator = new UserbitsAsTimeDecorator(rawIO)

    // -- Collect data
    val videoIndices = new mutable.ArrayBuffer[VideoIndex]()
    timeDecorator.getIndexObservable
      .distinctUntilChanged()
      .forEach(vi => videoIndices += vi)

    // -- Wait until video starts playing. Sometimes the VCR take a few seconds to get rolling
    val shouldWait = new AtomicBoolean(true)
    io.getStateObservable
      .filter(_.isPlaying)
      .take(1)
      .forEach(s => shouldWait.set(false))
    io.send(VideoCommands.PLAY)

    while(shouldWait.get()) {
      Thread.sleep(200)
    }
    shouldWait.set(true)

    // -- Schedule shutdown after specified duration
    val timer = new Timer(getClass.getSimpleName)
    val stopTask = new TimerTask {
      override def run() = {
        io.send(VideoCommands.STOP)
        syncDecorator.unsubscribe()
        timeDecorator.unsubscribe()
        io.close()
        shouldWait.set(false)
      }
    }
    timer.schedule(stopTask, durationMillis)


    while(shouldWait.get()) {
      Thread.sleep(200)
    }

    videoIndices
  }

  def dumpData(videoIndices: Seq[VideoIndex],
               intervalSecs: Double,
               lengthSecs: Double): Unit = {
    // -- Write out data as file
    import scilube.Matlib._
    val ind = videoIndices
      .filter(vi => vi.getTimecode.isPresent && vi.getTimestamp.isPresent)
      .map(vi => (vi.getTimecode.get(), vi.getTimestamp.get()))
      .toArray

    // Convert everything to seconds
    val tcSecs = ind.map(i => new Timecode(i._1.toString, FrameRates.NTSC))
      .map(_.getSeconds)
    val timeSecs = ind.map(i => i._2.getEpochSecond).map(_.toDouble)
    val outputTcSecs = (tcSecs.min to (tcSecs.min + lengthSecs) by intervalSecs).toArray

    val (tcs, ia, ic) = unique(tcSecs)
    val ts = subset(timeSecs, ia)

    // interp
    val outputTimeSecs = Functions.extrap1(tcs, ts, outputTcSecs).map(Math.round)

    // write out data
    val outputFrames = outputTcSecs.map(_ * FrameRates.NTSC)
    val outputTc = outputFrames.map(new Timecode(_, FrameRates.NTSC))
    val outputInstant = outputTimeSecs.map(Instant.ofEpochSecond)
    println("timecode\ttimestamp\tepochseconds")
    for (i <- outputTc.indices) {
      println(s"${outputTc(i)}\t${outputInstant(i)}\t${outputTimeSecs(i)}")
    }

  }



}