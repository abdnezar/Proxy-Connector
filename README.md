# Android Proxy App

A system-wide proxy application for Android that uses VPN service to route all device traffic through a configured proxy server.

## Features

- Route all device traffic through a proxy server
- Support for HTTP and SOCKS proxies
- Optional proxy authentication
- No root access required
- Simple and intuitive user interface

## Requirements

- Android 5.0 (API level 21) or higher
- Android Studio 4.0 or higher

## Building the App

1. Clone or download this repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Build the app using the "Build > Build Bundle(s) / APK(s) > Build APK(s)" menu option
5. The APK will be generated in `app/build/outputs/apk/debug/app-debug.apk`

## Installation

1. Enable "Install from Unknown Sources" in your Android device settings
2. Transfer the APK to your device
3. Install the APK by tapping on it
4. Follow the on-screen instructions to complete installation

## Usage

1. Open the app
2. Tap on "Settings" to configure your proxy
3. Enter your proxy server details:
   - Host: The hostname or IP address of your proxy server
   - Port: The port number of your proxy server
   - Type: HTTP or SOCKS
   - Username/Password: Optional authentication credentials
4. Tap "Save" to save your settings
5. Return to the main screen and tap "Start Proxy" to enable the proxy
6. Accept the VPN connection request when prompted
7. All device traffic will now be routed through your proxy server
8. To disable the proxy, tap "Stop Proxy"

## Implementation Notes

This app uses Android's VpnService API to create a local VPN that captures all device traffic and routes it through the configured proxy server. The implementation includes:

1. **ProxyConfig**: Manages proxy configuration settings
2. **ProxyVpnService**: Implements the VPN service that captures and routes traffic
3. **MainActivity**: Provides the main user interface
4. **ProxySettingsActivity**: Allows users to configure proxy settings

### Important Note About Packet Processing

The current implementation includes a placeholder for packet processing. In a production app, you would need to use a library like `tun2socks` to handle the actual packet processing and forwarding to the proxy server.

## Limitations

- The app cannot route traffic from other VPN apps
- Some apps may detect and block VPN usage
- Performance may be affected due to the overhead of processing all network traffic

## License

This project is licensed under the MIT License - see the LICENSE file for details.

