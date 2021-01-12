package com.yezao.sdks.bluetransforlib

/**
 * 作者：zhuxiaolong
 * 创建时间: 2021/1/11 15:52
 * 描述：智能小车的传输对象  connection
 * 封装bluetooth socket
 * wifisocket
 * ble 等类型的 通信方式
 *
 * 只负责传输
 * */
abstract class AbsConnection {
    protected var isConnected = false
    protected lateinit var type: ConnectionType


    abstract fun connect()
    abstract fun disconnect()


    protected var mDataListener : OnDataListener<Any>?=null
    interface OnDataListener<E:Any>{
        fun onReceivedData(data:E)
    }
}