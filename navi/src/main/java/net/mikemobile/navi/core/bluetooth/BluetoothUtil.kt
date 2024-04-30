package net.mikemobile.navi.bluetooth

import java.util.UUID

object BluetoothUtil {


    // Name for the SDP record when creating server socket
    val NAME_SECURE = "BluetoothChatSecure"
    val NAME_INSECURE = "BluetoothChatInsecure"

    // Unique UUID for this application
    val MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")


    enum class SOCKET_TYPE {
        SECURE, INSECURE, NONE
    }

    enum class CONNECT_TYPE {
        NONE,
        CREATE_SOCKET,
        CONNECT_WAIT,
        CONNECTION,
        CONNECT_FAILD,
        START_SERVER,
        CONNECTED,
        CANCEL,
        DISCONNECT,
        STOP_SERVER,
        CONNECT_LOST,
        ERROR,
        DISABLE
    }

}
