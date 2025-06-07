package com.example.proxyapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class ProxySettingsActivity : AppCompatActivity() {
    private lateinit var etHost: TextInputEditText
    private lateinit var etPort: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var spinnerType: Spinner
    private lateinit var btnSave: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxy_settings)
        
        // Initialize UI elements
        etHost = findViewById(R.id.etHost)
        etPort = findViewById(R.id.etPort)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        spinnerType = findViewById(R.id.spinnerType)
        btnSave = findViewById(R.id.btnSave)
        
        // Set up proxy type spinner
        val proxyTypes = arrayOf(
            getString(R.string.proxy_type_http),
            getString(R.string.proxy_type_socks)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, proxyTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
        
        // Load existing configuration
        loadConfig()
        
        // Set up save button
        btnSave.setOnClickListener { saveConfig() }
    }
    
    /**
     * Loads the existing proxy configuration.
     */
    private fun loadConfig() {
        val config = ProxyConfig.loadFromPreferences(this)
        
        etHost.setText(config.host)
        etPort.setText(config.port.toString())
        etUsername.setText(config.username ?: "")
        etPassword.setText(config.password ?: "")
        
        // Set spinner selection based on proxy type
        spinnerType.setSelection(when (config.type) {
            ProxyType.HTTP -> 0
            ProxyType.SOCKS -> 1
        })
    }
    
    /**
     * Saves the proxy configuration.
     */
    private fun saveConfig() {
        val host = etHost.text.toString().trim()
        val portStr = etPort.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        // Validate host
        if (host.isEmpty()) {
            Toast.makeText(this, R.string.error_invalid_host, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate port
        val port = try {
            portStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, R.string.error_invalid_port, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (port < 1 || port > 65535) {
            Toast.makeText(this, R.string.error_invalid_port, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get proxy type
        val type = when (spinnerType.selectedItemPosition) {
            0 -> ProxyType.HTTP
            1 -> ProxyType.SOCKS
            else -> ProxyType.HTTP
        }
        
        // Create and save proxy configuration
        val config = ProxyConfig(
            host = host,
            port = port,
            type = type,
            username = if (username.isEmpty()) null else username,
            password = if (password.isEmpty()) null else password
        )
        
        config.saveToPreferences(this)
        
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
}

