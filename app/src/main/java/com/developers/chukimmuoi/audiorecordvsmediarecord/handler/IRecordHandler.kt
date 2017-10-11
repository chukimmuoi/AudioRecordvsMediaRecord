package com.developers.chukimmuoi.audiorecordvsmediarecord.handler

/**
 * @author  : Hanet Electronics
 * @Skype   : chukimmuoi
 * @Mobile  : +84 167 367 2505
 * @Email   : muoick@hanet.com
 * @Website : http://hanet.com/
 * @Project : AudioRecordvsMediaRecord
 * Created by chukimmuoi on 11/10/2017.
 */
abstract class IRecordHandler constructor(private val outputFileName: String){

    fun onRecord(start: Boolean) {
        if (start) startRecording(outputFileName) else stopRecording()
    }

    abstract fun startRecording(outputFileName: String)

    abstract fun stopRecording()

    fun onPlay(start: Boolean) {
        if (start) startPlaying(outputFileName) else stopPlaying()
    }

    abstract fun startPlaying(outputFileName: String)

    abstract fun stopPlaying()

    fun cleanUp() {
        stopRecording()
        stopPlaying()
    }
}