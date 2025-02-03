package com.denicks21.recorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.denicks21.recorder.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    var mFileName: File? = null
    private var posicioAudio = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val fil = Thread {runOnUiThread({
            if (mPlayer != null) {
                posicioAudio = mPlayer!!.currentPosition
                if (posicioAudio >= mPlayer!!.duration) {
                    binding.idTVstatusFile.setTextColor(Color.CYAN)
                    binding.idTVstatusFile.text = "Ended..."
                }
            }

        })}

        fil.start()

        binding.btnRecord.setOnClickListener {
            startRecording()
        }

        binding.btnStop.setOnClickListener {
            endRecording()
        }

        binding.btnPlay.setOnClickListener {
            playAudio()
        }

        binding.btnStopPlay.setOnClickListener {
            pausePlaying()
        }
    }

    private fun startRecording() {

        // Check permissions
        if (CheckPermissions()) {
            val drawable = AppCompatResources.getDrawable(applicationContext, R.drawable.btn_rec_start)
            drawable!!.setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY)
            binding.btnRecord.setBackgroundDrawable(drawable)

            // Save file
            mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

            // If file exists then increment counter
            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

            // Initialize the class MediaRecorder
            mRecorder = MediaRecorder()

            // Set source to get audio
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)

            // Set the format of the file
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

            // Set the audio encoder
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Set the save path
            mRecorder!!.setOutputFile(mFileName)
            try {
                // Preparation of the audio file
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }
            // Start the audio recording
            mRecorder!!.start()
            binding.idTVstatus.setTextColor(Color.CYAN)
            binding.idTVstatus.text = "Recording..."
            binding.idTVstatusFile.setTextColor(Color.RED)
            binding.idTVstatusFile.text = "File not loaded."

        } else {
            // Request permissions
            RequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions accepted ->
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {

                    startRecording()
                    // Message
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()

                } else {

                    // Message
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun CheckPermissions(): Boolean {

        // Check permissions
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {

        // Request permissions
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    fun playAudio() {


        // Use the MediaPlayer class to listen to recorded audio files

        if (mFileName != null) {
            if (mPlayer == null) {
                mPlayer = MediaPlayer()
                try {
                    // Preleva la fonte del file audio
                    mPlayer!!.setDataSource(mFileName.toString())

                    // Fetch the source of the mPlayer
                    mPlayer!!.prepare()

                    // Start the mPlayer
                    mPlayer!!.start()
                } catch (e: IOException) {
                    Log.e("TAG", "prepare() failed")
                }
            } else {
                mPlayer!!.seekTo(posicioAudio)
                if (posicioAudio >= mPlayer!!.duration) {
                    mPlayer!!.seekTo(0)
                }
                mPlayer!!.start()
            }
            binding.idTVstatusFile.setTextColor(Color.CYAN)
            binding.idTVstatusFile.text = "Playing..."
        } else {
            binding.idTVstatusFile.setTextColor(Color.RED)
            binding.idTVstatusFile.text = "You dont have any audio to play."
        }




    }

    fun endRecording() {

        // Stop recording
        if (mFileName == null || mRecorder == null) {
            // Message
            binding.idTVstatus.setTextColor(Color.RED)
            binding.idTVstatus.text = "Save Failed... Start recording before saving."
            Toast.makeText(getApplicationContext(), "Registration not started", Toast.LENGTH_LONG).show()

        } else {
            val drawable = AppCompatResources.getDrawable(applicationContext, R.drawable.btn_rec_start)
            drawable!!.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
            binding.btnRecord.setBackgroundDrawable(drawable)

            mRecorder!!.stop()

            // Message to confirm save file
            val savedUri = Uri.fromFile(mFileName)
            val msg = "File saved: " + savedUri!!.lastPathSegment
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()

            // Release the class mRecorder
            mRecorder!!.release()
            mRecorder = null
            binding.idTVstatus.setTextColor(Color.GREEN)
            binding.idTVstatus.text = "Successfully recorded... File saved. Ready to play."
            binding.idTVstatusFile.setTextColor(Color.GREEN)
            binding.idTVstatusFile.text = "File loaded successfully."
        }
    }

    fun pausePlaying() {
        if (mPlayer != null)  {
            posicioAudio = mPlayer!!.currentPosition
            binding.idTVstatusFile.setTextColor(Color.YELLOW)
            binding.idTVstatusFile.text = "Paused..."
            mPlayer!!.pause()
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}