package com.developers.chukimmuoi.audiorecordvsmediarecord.audio

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.developers.chukimmuoi.audiorecordvsmediarecord.handler.AudioHandler
import timber.log.Timber

/**
 * A placeholder fragment containing a simple view.
 */
class AudioRecordFragment : Fragment() {

    private lateinit var mAudioRecord: AudioHandler
    private var mRecordButton: RecordButton?= null
    private var mPlayButton: PlayButton?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        mAudioRecord = AudioHandler(context.applicationContext, "audiorecordtest.3gp")
    }

    inner class RecordButton(context: Context) : Button(context) {
        private var mStartRecording: Boolean = true

        private var clicker: OnClickListener = OnClickListener {
            mAudioRecord.onRecord(mStartRecording)
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
            mAudioRecord.onPlay(mStartPlaying)
            text = if (mStartPlaying) "Stop playing" else "Start playing"
            mStartPlaying = !mStartPlaying
        }

        init {
            text = "Start playing"
            setOnClickListener(clicker)
        }
    }

    override fun onStop() {
        Timber.d("cleanup() is called")
        mAudioRecord.cleanUp()

        super.onStop()
    }
}

