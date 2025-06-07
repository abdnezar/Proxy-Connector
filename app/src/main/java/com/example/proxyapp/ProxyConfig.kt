package com.example.proxyapp

import android.content.Context
import android.content.SharedPreferences
import java.net.InetSocketAddress
import java.net.Proxy

/**
 * Enum class representing different types of proxy connections.
 */
enum class ProxyType {
    HTTP,
    SOCKS
}

/**
 * Class for storing and managing proxy configuration settings.
 */
class ProxyConfig(
    val host: String,
    val port: Int,
    val type: ProxyType,
    val username: String? = null,
    val password: String? = null
) {

    /**
     * Returns the Java Proxy object based on the configuration.
     */
    fun getProxy(): Proxy {
        val proxyType = when (type) {
            ProxyType.HTTP -> Proxy.Type.HTTP
            ProxyType.SOCKS -> Proxy.Type.SOCKS
        }
        
        return Proxy(proxyType, InetSocketAddress(host, port))
    }
    
    /**
     * Saves the proxy configuration to SharedPreferences.
     */
    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_HOST, host)
            putInt(KEY_PORT, port)
            putString(KEY_TYPE, type.name)
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }
    
    /**
     * Checks if the proxy configuration is valid.
     */
    fun isValid(): Boolean {
        return host.isNotBlank() && port in 1..65535
    }
    
    companion object {
        private const val PREFS_NAME = "proxy_config"
        private const val KEY_HOST = "host"
        private const val KEY_PORT = "port"
        private const val KEY_TYPE = "type"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        
        /**
         * Loads the proxy configuration from SharedPreferences.
         */
        fun loadFromPreferences(context: Context): ProxyConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            val host = prefs.getString(KEY_HOST, "") ?: ""
            val port = prefs.getInt(KEY_PORT, 8080)
            val typeStr = prefs.getString(KEY_TYPE, ProxyType.HTTP.name)
            val type = try {
                ProxyType.valueOf(typeStr ?: ProxyType.HTTP.name)
            } catch (e: Exception) {
                ProxyType.HTTP
            }
            val username = prefs.getString(KEY_USERNAME, null)
            val password = prefs.getString(KEY_PASSWORD, null)
            
            return ProxyConfig(host, port, type, username, password)
        }
        
        /**
         * Checks if a proxy configuration exists in SharedPreferences.
         */
        fun exists(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val host = prefs.getString(KEY_HOST, "")
            return !host.isNullOrBlank()
        }
    }
}

