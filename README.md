# AudioRecordvsMediaRecord
Different AudioRecord and MediaRecord

### MediaRecord
- Được sử dụng để ghi lại âm thanh và video.
- Diagram:

![alt text](https://developer.android.com/images/mediarecorder_state_diagram.gif)

- Ví dụ:
```Kotlin
val mediaRecorder: MediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // Mã hoá âm thanh.
        mediaRecorder.setOutputFile("PATH_NAME") // Tên tệp tin đầu ra.
        mediaRecorder.prepare() // Hoàn thành việc khởi tạo.
        mediaRecorder.start()   // Bắt đầu ghi.
        ...
        mediaRecorder.stop()    // Dừng ghi.
        mediaRecorder.reset()   // Sử dụng lại object mediaRecorder nếu call reset.
        mediaRecorder.release() // Không thể sử dụng lại object mediaRecorder nếu call release.
 ```
