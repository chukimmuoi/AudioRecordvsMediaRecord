package com.developers.chukimmuoi.audiorecordvsmediarecord.audio

import android.content.Context
import android.media.*
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import timber.log.Timber
import java.io.*

/**
 * A placeholder fragment containing a simple view.
 */
class AudioRecordFragment : Fragment() {

    private val RECORDING_RATE: Int = 8000 // Can go up to 41000, if needed.
    private val CHANNEL_IN: Int = AudioFormat.CHANNEL_IN_MONO
    private val CHANNELS_OUT: Int = AudioFormat.CHANNEL_OUT_MONO
    private val FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE: Int = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL_IN, FORMAT)

    private val mOutputFileName: String = "audiorecordtest.3gp"
    private lateinit var mAudioManager: AudioManager
    private lateinit var mContext: Context
    private var mState: State = State.IDLE

    private var mRecordButton: RecordButton?= null
    private var mRecordingAsyncTask: AsyncTask<Void, Void, Void>? = null

    private var mPlayButton: PlayButton?= null
    private var mPlayingAsyncTask: AsyncTask<Void, Void, Void>? = null

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

        mAudioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mContext = context.applicationContext
    }

    enum class State {
        IDLE, RECORDING, PLAYING
    }

    private fun onRecord(start: Boolean) {
        if (start)
            startRecording()
        else
            stopRecording()
    }

    /**
     * Start recording from the MIC.
     * */
    private fun startRecording() {
        if (mState != State.IDLE) {
            Timber.w("Requesting to start recording while state was not IDLE")
            return
        }

        mRecordingAsyncTask = object : AsyncTask<Void, Void, Void>() {
            private lateinit var mAudioRecord: AudioRecord

            override fun onPreExecute() {
                super.onPreExecute()
                mState = State.RECORDING
            }

            override fun doInBackground(vararg p0: Void?): Void? {
                mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                        RECORDING_RATE, CHANNEL_IN, FORMAT, BUFFER_SIZE * 3)
                var bufferedOutputStream: BufferedOutputStream? = null
                try {
                    bufferedOutputStream =
                            BufferedOutputStream(mContext.openFileOutput(mOutputFileName, Context.MODE_PRIVATE))
                    var buffer = ByteArray(BUFFER_SIZE)
                    mAudioRecord.startRecording()
                    while (!isCancelled) {
                        var read = mAudioRecord.read(buffer, 0, buffer.size)
                        bufferedOutputStream.write(buffer, 0, read)
                    }
                } catch (e: IOException) {
                    Timber.e("IOException - Failed to record data: " + e)
                } catch (e: NullPointerException) {
                    Timber.e("NullPointerException - Failed to record data: " + e)
                } catch (e: IndexOutOfBoundsException) {
                    Timber.e("IndexOutOfBoundsException - Failed to record data: " + e)
                } finally {
                    try {
                        bufferedOutputStream?.close()
                    } catch (e: IOException) {
                        // ignore
                    }
                    mAudioRecord.release()
                }

                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)

                mState = State.IDLE
                mRecordingAsyncTask = null
            }

            override fun onCancelled() {
                super.onCancelled()

                if (mState == State.RECORDING) {
                    Timber.d("Stopping the recording ...")
                    mState = State.IDLE
                } else {
                    Timber.w("Requesting to stop recording while state was not RECORDING")
                }

                mRecordingAsyncTask = null
            }
        }

        mRecordingAsyncTask?.execute()
    }

    private fun stopRecording() {
        mRecordingAsyncTask?.cancel(true)
    }

    private fun onPlay(start: Boolean) {
        if (start)
            startPlaying()
        else
            stopPlaying()
    }

    /**
     * Starts playback of the recorded audio file.
     * */
    private fun startPlaying() {
        if (mState != State.IDLE) {
            Timber.w("Requesting to play while state was not IDLE")
            return
        }

        val file = File(mContext.filesDir, mOutputFileName)
        if (!file?.exists()) {
            // there is no recording to play
            return
        }
        val intSize = AudioTrack.getMinBufferSize(RECORDING_RATE, CHANNELS_OUT, FORMAT)

        mPlayingAsyncTask = object : AsyncTask<Void, Void, Void>() {
            private lateinit var mAudioTrack: AudioTrack

            override fun onPreExecute() {
                super.onPreExecute()
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
                mState = State.PLAYING
            }

            override fun doInBackground(vararg p0: Void?): Void? {
                try {
                    mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, RECORDING_RATE, CHANNELS_OUT, FORMAT, intSize, AudioTrack.MODE_STREAM)
                    var buffer = ByteArray(intSize * 2)
                    var input: FileInputStream? = null
                    var bis: BufferedInputStream? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAudioTrack.setVolume(AudioTrack.getMaxVolume())
                    }
                    mAudioTrack.play()
                    try {
                        input = mContext.openFileInput(mOutputFileName)
                        bis = BufferedInputStream(input)
                        while (!isCancelled) {
                            var read: Int = bis.read(buffer, 0, buffer.size)
                            if (read > 0) {
                                mAudioTrack.write(buffer, 0, read)
                            }
                        }
                    } catch (e: IOException) {
                        Timber.e("Failed to read the sound file into a byte array", e)
                    } finally {
                        try {
                            input?.close()
                            bis?.close()
                        } catch (e: IOException) {
                            // ignore
                        }

                        mAudioTrack.release()
                    }

                } catch (e: IllegalStateException) {
                    Timber.e("Failed to start playback", e)
                }

                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                cleanUp()
            }

            override fun onCancelled() {
                super.onCancelled()
                cleanUp()
            }

            private fun cleanUp() {
                mState = State.IDLE
                mPlayingAsyncTask = null
            }
        }

        mPlayingAsyncTask?.execute()
    }

    private fun stopPlaying() {
        mPlayingAsyncTask?.cancel(true)
    }

    inner class RecordButton(context: Context) : Button(context) {
        private var mStartRecording: Boolean = true

        private var clicker: OnClickListener = OnClickListener {
            onRecord(mStartRecording)
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
            onPlay(mStartPlaying)
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

        stopRecording()
        startPlaying()
        super.onStop()
    }
}

