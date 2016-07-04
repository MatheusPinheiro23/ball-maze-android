package br.edu.ifsp.sbv.ball_maze_android.utils;

import java.util.UUID;

/**
 * Classe com as constantes da conexao bluetooth
 */
public abstract class BluetoothConstants {

    // UUID Bluetooth
    public static final UUID UUID_BLUETOOTH = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Mac Address do bluetooth
    public static final String MAC_BLUETOOTH = "98:D3:31:40:23:26";

    // Codigo de requisicao sensor bluetooth
    public static final int REQUEST_ENABLE_BT = 1;

    // Status de conexao
    public static final int BT_STATUS_NAO_SUPORTADO = 0;
    public static final int BT_STATUS_HABILITADO = 1;
    public static final int BT_STATUS_DESABILITADO = 2;

    // Mensagens
    public static final String MSG_EMPTY = "";
    public static final String MSG_BLUETOOTH_NOT_SUPPORTED = "Bluetooth não é suportado.";

    public static final String MSG_BLUETOOTH_CONNECTING = "Conectando ao arduino...";
    public static final String MSG_BLUETOOTH_DISCONNECTING = "Desconectando do arduino...";
    public static final String MSG_BLUETOOTH_CONNECTED = "Conectado.";
    public static final String MSG_BLUETOOTH_DISCONNECTED = "Desconectado.";
    public static final String MSG_BLUETOOTH_NO_CONNECTION = "Não existe conexão estabelecida para se desconectar.";

    // Mensagens de LOG
    public static final String LOG_TAG = "[LOG - BLUETOOTH] ";
    public static final String MSG_LOG_SENDING_DATA = "Enviando dados: ";
    public static final String MSG_LOG_BLUETOOTH_CONNECTING = "Conectando ao bluetooth...";
    public static final String MSG_LOG_BLUETOOTH_MAC = "MAC: ";
    public static final String MSG_LOG_CONNECTION_ERROR= "Erro de conexão: ";
    public static final String MSG_LOG_DISCONNECTION_ERROR= "Erro ao desconectar: ";
}
