package com.example.proxyapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * VPN service that captures all device traffic and routes it through the configured proxy.
 */
class ProxyVpnService : VpnService() {
    private val TAG = "ProxyVpnService"
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "proxy_vpn_channel"
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private val running = AtomicBoolean(false)
    private val proxyConfig = AtomicReference<ProxyConfig>()
    
    private var vpnThread: Thread? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            stopSelf()
            return START_NOT_STICKY
        }
        
        // Load proxy configuration
        val config = ProxyConfig.loadFromPreferences(this)
        if (!config.isValid()) {
            Log.e(TAG, "Invalid proxy configuration")
            stopSelf()
            return START_NOT_STICKY
        }
        
        proxyConfig.set(config)
        
        // Start as a foreground service
        startForeground(NOTIFICATION_ID, createNotification(config))
        
        // Establish VPN connection
        establishVpn()
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        Log.d(TAG, "Service destroyed")
    }
    
    /**
     * Establishes the VPN connection.
     */
    private fun establishVpn() {
        if (running.get()) {
            Log.d(TAG, "VPN already running")
            return
        }
        
        try {
            // Configure the VPN interface
            val builder = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)  // Route all traffic through VPN
                .addDnsServer("8.8.8.8") // Google DNS
                .setSession("Proxy VPN")
            
            // Create the VPN interface
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN")
                stopSelf()
                return
            }
            
            running.set(true)
            
            // Start packet processing in a background thread
            vpnThread = Thread {
                try {
                    processPackets()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in VPN thread", e)
                } finally {
                    stopVpn()
                }
            }
            vpnThread?.start()
            
            Log.d(TAG, "VPN established successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error establishing VPN", e)
            stopVpn()
            stopSelf()
        }
    }
    
    /**
     * Processes packets from the VPN interface.
     * 
     * Note: In a real implementation, this would use tun2socks or a similar library
     * to handle the packet processing. This simplified implementation just demonstrates
     * the concept.
     */
    private fun processPackets() {
        val buffer = ByteBuffer.allocate(32767)
        
        try {
            val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
            val outputStream = FileOutputStream(vpnInterface?.fileDescriptor)
            
            while (running.get()) {
                // Read a packet from the VPN interface
                val length = inputStream.read(buffer.array())
                if (length > 0) {
                    // In a real implementation, we would:
                    // 1. Parse the IP packet
                    // 2. Extract the destination address and port
                    // 3. Forward the packet to the proxy server
                    // 4. Receive the response
                    // 5. Write the response back to the VPN interface
                    
                    // For demonstration purposes, we're just logging
                    Log.d(TAG, "Received packet of length $length")
                    
                    // In a real implementation, we would use tun2socks or a similar library
                    // to handle the packet processing
                    
                    // For now, just echo the packet back (this won't work in practice)
                    // outputStream.write(buffer.array(), 0, length)
                }
                
                // Sleep a bit to avoid busy-waiting
                Thread.sleep(10)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error processing packets", e)
        } catch (e: InterruptedException) {
            Log.d(TAG, "VPN thread interrupted")
        }
    }
    
    /**
     * Stops the VPN connection.
     */
    private fun stopVpn() {
        running.set(false)
        
        vpnThread?.interrupt()
        vpnThread = null
        
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        
        stopForeground(true)
        Log.d(TAG, "VPN stopped")
    }
    
    /**
     * Creates the notification channel for Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Creates the notification for the foreground service.
     */
    private fun createNotification(config: ProxyConfig): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text, config.host, config.port))
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentIntent(pendingIntent)
            .build()
    }
}

