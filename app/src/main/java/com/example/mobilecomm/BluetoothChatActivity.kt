package com.example.mobilecomm

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothChatActivity : AppCompatActivity() {

    private val appUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private lateinit var listChatMessages: ListView
    private lateinit var listPairedDevices: ListView
    private lateinit var messagesAdapter: ArrayAdapter<String>
    private val messages = mutableListOf<String>()

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_chat)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        listChatMessages = findViewById(R.id.listChatMessages)
        listPairedDevices = findViewById(R.id.listPairedDevices)
        messagesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        listChatMessages.adapter = messagesAdapter

        requestBluetoothPermissions()

        findViewById<Button>(R.id.btnStartServer).setOnClickListener {
            addMessage("Waiting for a connection...")
            Thread { startServer() }.start()
        }

        findViewById<Button>(R.id.btnShowDevices).setOnClickListener {
            showPairedDevices()
        }

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val etMessage = findViewById<EditText>(R.id.etMessage)
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                addMessage("Me: $text")
                etMessage.setText("")
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 31) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 200)
        }
    }

    @Suppress("MissingPermission")
    private fun showPairedDevices() {
        val pairedDevices = bluetoothAdapter.bondedDevices.toList()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices. Pair via phone Bluetooth settings first.", Toast.LENGTH_LONG).show()
            return
        }
        val names = pairedDevices.map { "${it.name}\n${it.address}" }
        listPairedDevices.visibility = android.view.View.VISIBLE
        listPairedDevices.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        listPairedDevices.setOnItemClickListener { _, _, position, _ ->
            val device = pairedDevices[position]
            listPairedDevices.visibility = android.view.View.GONE
            addMessage("Connecting to ${device.name}...")
            Thread { connectToDevice(device) }.start()
        }
    }

    @Suppress("MissingPermission")
    private fun startServer() {
        try {
            val serverSocket: BluetoothServerSocket =
                bluetoothAdapter.listenUsingRfcommWithServiceRecord("MobileCommChat", appUuid)
            val connectedSocket = serverSocket.accept() // blocks until a client connects
            socket = connectedSocket
            serverSocket.close()
            manageConnectedSocket(connectedSocket)
        } catch (e: IOException) {
            runOnUiThread { addMessage("Server failed: ${e.message}") }
        }
    }

    @Suppress("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        try {
            val clientSocket = device.createRfcommSocketToServiceRecord(appUuid)
            bluetoothAdapter.cancelDiscovery()
            clientSocket.connect() // blocks until connected
            socket = clientSocket
            manageConnectedSocket(clientSocket)
        } catch (e: IOException) {
            runOnUiThread { addMessage("Connection failed: ${e.message}") }
        }
    }

    private fun manageConnectedSocket(connectedSocket: BluetoothSocket) {
        outputStream = connectedSocket.outputStream
        val inputStream: InputStream = connectedSocket.inputStream

        runOnUiThread { addMessage("Connected!") }

        val buffer = ByteArray(1024)
        while (true) {
            try {
                val bytes = inputStream.read(buffer)
                val receivedText = String(buffer, 0, bytes)
                runOnUiThread { addMessage("Friend: $receivedText") }
            } catch (e: IOException) {
                runOnUiThread { addMessage("Connection lost") }
                break
            }
        }
    }

    private fun sendMessage(text: String) {
        try {
            outputStream?.write(text.toByteArray())
        } catch (e: IOException) {
            addMessage("Send failed: ${e.message}")
        }
    }

    private fun addMessage(msg: String) {
        handler.post {
            messages.add(msg)
            messagesAdapter.notifyDataSetChanged()
            listChatMessages.smoothScrollToPosition(messages.size - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { socket?.close() } catch (_: IOException) {}
    }
}