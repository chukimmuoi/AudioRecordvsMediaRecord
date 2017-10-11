package com.developers.chukimmuoi.audiorecordvsmediarecord.handler

import android.media.MediaPlayer
import android.media.MediaRecorder
import timber.log.Timber
import java.io.IOException

/**
 * @author  : Hanet Electronics
 * @Skype   : chukimmuoi
 * @Mobile  : +84 167 367 2505
 * @Email   : muoick@hanet.com
 * @Website : http://hanet.com/
 * @Project : AudioRecordvsMediaRecord
 * Created by chukimmuoi on 11/10/2017.
 */
class MediaHandler(outputFileName: String) : IRecordHandler(outputFileName) {


    private var mMediaRecord: MediaRecorder?= null

    private var mMediaPlayer: MediaPlayer?= null

    override fun startRecording(outputFileName: String) {
        mMediaRecord = MediaRecorder()
        mMediaRecord?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecord?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mMediaRecord?.setOutputFile(outputFileName)
        mMediaRecord?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mMediaRecord?.prepare()
        } catch (e: IOException) {
            Timber.e("MediaRecord prepare failed.")
        }

        mMediaRecord?.start()
    }

    override fun stopRecording() {
        mMediaRecord?.stop()
        mMediaRecord?.release()
        mMediaRecord = null
    }

    override fun startPlaying(outputFileName: String) {
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer?.setDataSource(outputFileName)
            mMediaPlayer?.prepare()
            mMediaPlayer?.start()
        } catch (e: IOException) {
            Timber.e(e.message)
        }
    }

    override fun stopPlaying() {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }
}