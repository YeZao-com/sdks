package com.yezao.demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.view.View
import com.google.android.material.tabs.TabLayout
import com.yezao.sdks.connections.CarConnection

class DataReceiverActivity : AppCompatActivity() {

    val TAG = "DataReceiverActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_receiver)

        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    val intentFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        addAction(BluetoothDevice.ACTION_FOUND)
    }

    val deviceMap = HashMap<String, BluetoothDevice>()

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {

                    val foundDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    if (deviceMap.containsKey(foundDevice?.address) || foundDevice == null||foundDevice.name ==null) {
                        return
                    }
                    Log.i(TAG, "发现设备 ：${foundDevice.name}")
                    deviceMap.put(foundDevice?.address ?: "", foundDevice)
                    if ("OO".equals(foundDevice?.name)) {

                        if (foundDevice.bondState == BluetoothDevice.BOND_BONDED) {
                            Log.i(TAG,"开始连接")
                            connection = CarConnection(foundDevice!!)
                            connection?.connect()
                        }else{
                            foundDevice.createBond()
                        }

                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED->{
                    Log.i(TAG,"")
                    val bondedDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    connection = CarConnection(bondedDevice!!)
                    connection?.connect()
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED->{
                    Log.i(TAG,"扫描开始")
                    deviceMap.clear()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                    Log.i(TAG,"扫描结束")
                }
            }
        }
    }

    var connection: CarConnection? = null
    fun search(v: View?) {
        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter.isEnabled.not()) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 2)
            return
        }

        adapter.startDiscovery()


    }

    fun connect(v: View?) {

    }


}