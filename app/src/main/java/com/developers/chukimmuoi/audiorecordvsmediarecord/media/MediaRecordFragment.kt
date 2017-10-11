package com.developers.chukimmuoi.audiorecordvsmediarecord.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.developers.chukimmuoi.audiorecordvsmediarecord.handler.MediaHandler
import timber.log.Timber


/**
 * @author  : Hanet Electronics
 * @Skype   : chukimmuoi
 * @Mobile  : +84 167 367 2505
 * @Email   : muoick@hanet.com
 * @Website : http://hanet.com/
 * @Project : AudioRecordvsMediaRecord
 * Created by chukimmuoi on 29/09/2017.
 */
class MediaRecordFragment : Fragment() {

    private val REQUEST_RECORD_AUDIO_PERMISSION: Int = 200

    private lateinit var mMediaRecord: MediaHandler

    private var mRecordButton: RecordButton ?= null
    private var mPlayButton: PlayButton ?= null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted: Boolean = false
    private val permissions: Array<String> = Array(1, { _ ->  Manifest.permission.RECORD_AUDIO })

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val ll = LinearLayout(activity)
        mRecordButton = RecordButton(activity)
        ll.addView(mRecordButton,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f))
        mPlayButton = PlayButton(activity)
        ll.addView(mPlayButton,
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0f))

        return ll
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMediaRecord = MediaHandler(activity.externalCacheDir.absolutePath +
                "/audiorecordtest.3gp")
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION ->
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }

        if (!permissionToRecordAccepted) {
            Timber.e("Not permissions")
        }
    }

    inner class RecordButton(context: Context) : Button(context) {
        private var mStartRecording: Boolean = true

        private var clicker: OnClickListener = OnClickListener {
            mMediaRecord.onRecord(mStartRecording)
            text = if (mStartRecording) "Stop recoding" else "Start recoding"
            mStartRecording = !mStartRecording
        }

        init {
            text = "Start recording"
            setOnClickListener(clicker)
        }
    }

    inner class PlayButton(context: Context) : Button(context) {
        private var mStartPlaying: Boolean = true

        private var clicker: OnClickListener = OnClickListener {
            mMediaRecord.onPlay(mStartPlaying)
            text = if (mStartPlaying) "Stop playing" else "Start playing"
            mStartPlaying = !mStartPlaying
        }

        init {
            text = "Start playing"
            setOnClickListener(clicker)
        }
    }

    override fun onStop() {
        mMediaRecord.cleanUp()

        super.onStop()
    }
}