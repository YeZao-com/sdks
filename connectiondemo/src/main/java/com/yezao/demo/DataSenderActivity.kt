package com.yezao.demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.yezao.sdks.connections.AbsConnection
import java.io.File
import java.io.FileReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.util.*
import kotlin.math.log

/**
 * create by 朱晓龙 2021/1/12 10:21 下午
 *  蓝牙连接传输图片发送方 Demo
 *
 *
 *
 *
 */
class DataSenderActivity : AppCompatActivity() {

    val TAG = "DataSenderActivity"


    val intentFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothDevice.ACTION_FOUND)
    }


    var uriHolder: Uri? = null
    val image: ImageView by lazy {
        findViewById(R.id.image_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_sender)
    }


    fun selectPicFromlocal(v: View?) {
        val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            setType("image/*")
        }
        startActivityForResult(pickIntent, 11)
    }

    fun getImageFromResult(data: Intent?) {
        val uri = data?.data
        Log.i(TAG, "${uri}")
        uriHolder = uri
        Glide.with(this).load(uri).into(image)
    }


    fun sendImage(v: View?) {
        if (uriHolder == null) {
            return
        }




        dataHandler.post {


            if(uriHolder!!.scheme != "content"){
                return@post
            }
            val query = contentResolver.query(uriHolder!!, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            query?.moveToPrevious()
            query?.also {
                it.moveToFirst()

            }
            Utils.getPathByContentUri(this,uriHolder)
//            val path = query?.getString(query.getColumnIndex(MediaStore.Images.Media.DATA))
//            Log.i(TAG,"image  path $path")
//            val imageFile = File(path)
//            val inputStream = imageFile.inputStream()
//            val byteArray = ByteArray(1024)
//            while (inputStream.read(byteArray) != -1) {
//                dataOutputStream?.write(byteArray)
//            }
//            dataOutputStream?.flush()
//            runOnUiThread {
//                Toast.makeText(this@DataSenderActivity, "发送完成", Toast.LENGTH_LONG).show()
//            }
        }

    }


    val listenerThread = HandlerThread("listener").apply { start() }
    val listenerHandler = Handler(listenerThread.looper)

    val dataThread = HandlerThread("data").apply { start() }
    val dataHandler = Handler(dataThread.looper)

    var dataOutputStream: OutputStream? = null

    fun startBluetooth(v: View?) {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter.isEnabled.not()) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 12)
            return
        }
        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 22)


        listenerHandler.post {
            val serverCmdSocket = adapter.listenUsingRfcommWithServiceRecord("cmd", UUID.fromString(AbsConnection.CMD_UUID))
            val serverDataSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("data", UUID.fromString(AbsConnection.DATA_UUID))

//            serverCmdSocket.accept()

            Log.e(TAG,"等待设备接入")
            val accept = serverDataSocket.accept()
            dataOutputStream = accept.outputStream
            Log.e(TAG, "有设备接入")
            runOnUiThread {
                Toast.makeText(this@DataSenderActivity, "有连接接入", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult")
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                11 -> {
                    getImageFromResult(data)
                }
            }
        }
    }
}