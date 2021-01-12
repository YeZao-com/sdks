package com.yezao.sdks.connections

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.nfc.Tag
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 作者：zhuxiaolong
 * 创建时间: 2021/1/11 16:44
 * 描述： 手机端与智能车之间的 连接
 * 1、手机端主动连接小车
 * */
class CarConnection(val remoteDevice: BluetoothDevice) : AbsConnection() {

    val TAG = "CarConnection"

    //负责 命令传输
    private val receiveThread = HandlerThread("listening").apply { start() }
    private val receiveHandler = Handler(receiveThread.looper)
    private val carCmdUUID = CMD_UUID

    //负责从socket 中获取 图像数据
    private val dataThread = HandlerThread("data").apply { start() }
    private val dataHandler = Handler(dataThread.looper)
    private val previewUUID = DATA_UUID

    //图片处理线程
    private val processThread = HandlerThread("process").apply { start() }
    private val processHandler = Handler(processThread.looper)

    val hostMac: String
    val hostName: String
    var rawSocket: BluetoothSocket? = null
    var dataSocket: BluetoothSocket? = null
    var dataReader: BufferedReader? = null

    var reader: BufferedReader? = null
    var writer: BufferedWriter? = null

    init {
        hostMac = remoteDevice.address
        hostName = remoteDevice.name
        remoteDevice.uuids
    }

    private val cmdListenTask = Runnable {
        Log.i(TAG,"listening cmd")
        while (rawSocket?.isConnected == true) {
            Log.i(TAG,"reading command")
            reader?.readLine()
        }
    }

    //解析图片任务
    private val dataListeningTask = Runnable {
        val inputStream = dataSocket?.inputStream
        val bufferByteArray = ByteArray(1024)
        val frameByteArray = ArrayList<Byte>()
        //这个循环 是让设备 持续监听 输入数据
        Log.i(TAG, "listening DATA")
        while (dataSocket?.isConnected == true) {
            //这个循环是 要获取完整的 图片数据
            Log.i(TAG, "reading data")
            frameByteArray.clear()
            while (inputStream?.read(bufferByteArray) != -1) {
                Log.i(TAG,"read... Frame ")
                //写入 到当前数据帧的集合中
                frameByteArray.addAll(bufferByteArray.asList())
            }
            //当前数据帧 读取完毕 将解析任务抛给外层
            processHandler.post {
                mDataListener?.onReceivedData(frameByteArray)
            }
        }
    }

    override fun connect() {
//        receiveHandler.post {
//
//            //等待 与服务端 建立命令通道连接
//            val createRfcommSocketToServiceRecord =
//                    remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(carCmdUUID))
//
//            rawSocket = createRfcommSocketToServiceRecord
//            reader =
//                    BufferedReader(InputStreamReader(createRfcommSocketToServiceRecord.inputStream))
//            writer =
//                    BufferedWriter(OutputStreamWriter(createRfcommSocketToServiceRecord.outputStream))
//            receiveHandler.post(cmdListenTask)
//
//        }
        //监听服务端数据通道 等待数据传输
        dataHandler.removeCallbacksAndMessages(null)
        dataHandler.post {
            Log.i(TAG,"开始建立数据连接")
            val previewSocket =
                    remoteDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(previewUUID))
            dataSocket = previewSocket
            previewSocket.connect()
            dataHandler.post(dataListeningTask)
        }
    }

    override fun disconnect() {
        dataSocket?.close()
        rawSocket?.close()

        dataHandler.removeCallbacksAndMessages(null)
        receiveHandler.removeCallbacksAndMessages(null)
        processHandler.removeCallbacksAndMessages(null)

        dataThread.quit()
        receiveThread.quit()
        processThread.quit()
    }


}