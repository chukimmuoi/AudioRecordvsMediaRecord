package com.developers.chukimmuoi.audiorecordvsmediarecord.handler

import android.content.Context
import android.media.*
import android.os.AsyncTask
import android.os.Build
import com.developers.chukimmuoi.audiorecordvsmediarecord.audio.State
import timber.log.Timber
import java.io.*

/**
 * @author  : Hanet Electronics
 * @Skype   : chukimmuoi
 * @Mobile  : +84 167 367 2505
 * @Email   : muoick@hanet.com
 * @Website : http://hanet.com/
 * @Project : AudioRecordvsMediaRecord
 * Created by chukimmuoi on 11/10/2017.
 */
class AudioHandler(val mContext: Context, outputFileName: String) : IRecordHandler(outputFileName) {

    private val RECORDING_RATE: Int = 8000 // Can go up to 41000, if needed.
    private val CHANNEL_IN: Int = AudioFormat.CHANNEL_IN_MONO
    private val CHANNELS_OUT: Int = AudioFormat.CHANNEL_OUT_MONO
    private val FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE: Int = AudioRecord.getMinBufferSize(RECORDING_RATE, CHANNEL_IN, FORMAT)

    private var mAudioManager: AudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mState: State = State.IDLE

    private var mRecordingAsyncTask: AsyncTask<Void, Void, Void>? = null
    private var mPlayingAsyncTask: AsyncTask<Void, Void, Void>? = null

    /**
     * Start recording from the MIC.
     * */
    override fun startRecording(outputFileName: String) {
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
                            BufferedOutputStream(mContext.openFileOutput(outputFileName, Context.MODE_PRIVATE))
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

    override fun stopRecording() {
        mRecordingAsyncTask?.cancel(true)
    }

    /**
     * Starts playback of the recorded audio file.
     * */
    override fun startPlaying(outputFileName: String) {
        if (mState != State.IDLE) {
            Timber.w("Requesting to play while state was not IDLE")
            return
        }

        val file = File(mContext.filesDir, outputFileName)
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
                        input = mContext.openFileInput(outputFileName)
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

    override fun stopPlaying() {
        mPlayingAsyncTask?.cancel(true)
    }

}