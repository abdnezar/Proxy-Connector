package com.example.proxyapp

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQUEST_CODE = 100
    
    private lateinit var tvStatus: TextView
    private lateinit var tvProxyInfo: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnSettings: Button
    
    private var isVpnRunning = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI elements
        tvStatus = findViewById(R.id.tvStatus)
        tvProxyInfo = findViewById(R.id.tvProxyInfo)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnSettings = findViewById(R.id.btnSettings)
        
        // Set up button click listeners
        btnStart.setOnClickListener { startVpn() }
        btnStop.setOnClickListener { stopVpn() }
        btnSettings.setOnClickListener { openSettings() }
        
        // Check if proxy configuration exists
        if (!ProxyConfig.exists(this)) {
            // No configuration exists, open settings
            openSettings()
        }
        
        updateUI()
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
    
    /**
     * Updates the UI based on the current state.
     */
    private fun updateUI() {
        val config = ProxyConfig.loadFromPreferences(this)
        
        // Update proxy info
        if (config.isValid()) {
            tvProxyInfo.text = "${config.host}:${config.port} (${config.type})"
            btnStart.isEnabled = true
        } else {
            tvProxyInfo.text = getString(R.string.error_invalid_host)
            btnStart.isEnabled = false
        }
        
        // Update status
        if (isVpnRunning) {
            tvStatus.text = getString(R.string.proxy_status, getString(R.string.status_connected))
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        } else {
            tvStatus.text = getString(R.string.proxy_status, getString(R.string.status_disconnected))
            btnStart.isEnabled = config.isValid()
            btnStop.isEnabled = false
        }
    }
    
    /**
     * Starts the VPN service.
     */
    private fun startVpn() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            // VPN permission not yet granted, request it
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
        } else {
            // Permission already granted, start service
            launchVpnService()
        }
    }
    
    /**
     * Stops the VPN service.
     */
    private fun stopVpn() {
        val serviceIntent = Intent(this, ProxyVpnService::class.java)
        serviceIntent.action = "STOP"
        startService(serviceIntent)
        
        isVpnRunning = false
        updateUI()
    }
    
    /**
     * Opens the proxy settings activity.
     */
    private fun openSettings() {
        val intent = Intent(this, ProxySettingsActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * Launches the VPN service.
     */
    private fun launchVpnService() {
        val serviceIntent = Intent(this, ProxyVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        isVpnRunning = true
        updateUI()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                launchVpnService()
            } else {
                Toast.makeText(this, R.string.error_vpn_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

