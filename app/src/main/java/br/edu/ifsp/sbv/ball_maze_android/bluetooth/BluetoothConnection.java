package br.edu.ifsp.sbv.ball_maze_android.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;

import br.edu.ifsp.sbv.ball_maze_android.interfaces.AsyncResponse;


/**
 * Classe para conexao bluetooth
 */
public class BluetoothConnection {

    // Variaveis de manipulacao da conexao bluetooth
    public static BluetoothDevice btDevice = null;
    public static BluetoothAdapter btAdapter = null;
    public static BluetoothSocket btSocket = null;
    public static OutputStream outStream = null;

    private Context context;
    private Activity activity;

    /**
     * Construtor
     */
    public BluetoothConnection(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        // Verifica se o bluetooth se encontra conectado ou exibe dialogo
        verificarBluetooth();
    }

    /**
     * Método utilizado para conectar o bluetooth
     *
     * @param asyncResponse interface de callback
     */
    public void conectar(AsyncResponse asyncResponse) {
        new Conectar(asyncResponse).execute();
    }

    /**
     * Metodo utilizado para desconectar do bluetooth
     *
     * @param asyncResponse interface de callback
     */
    public void desconectar(AsyncResponse asyncResponse) {
        new Desconectar(asyncResponse).execute();
    }

    /**
     * Método para verificar se módulo está habilitado ou exibir dialogo para habilitá-lo
     */
    public void verificarBluetooth() {

        // Verificar suporte ao bluetooth
        switch(verificaStatusBluetooth()) {
            case BluetoothConstants.BT_STATUS_NAO_SUPORTADO:
                Toast.makeText(context, BluetoothConstants.MSG_BLUETOOTH_NOT_SUPPORTED, Toast.LENGTH_LONG).show();
                activity.finish();
                break;
            case BluetoothConstants.BT_STATUS_DESABILITADO:
                Intent enableBtIntent = new Intent(BluetoothConnection.btAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, BluetoothConstants.REQUEST_ENABLE_BT);
                break;
        }
    }

    /**
     * Método para verificar se o bluetooth já está ligado
     * Verifica se tem suporte para bluetooth e checa se esta ligado ou pede para o usuario ligar
     *
     * @return integer se o bluetooth já está habilitado ou não
     */
    public int verificaStatusBluetooth() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            return BluetoothConstants.BT_STATUS_NAO_SUPORTADO;
        } else {
            if (!btAdapter.isEnabled()) {
                return BluetoothConstants.BT_STATUS_DESABILITADO;
            }
        }
        return BluetoothConstants.BT_STATUS_HABILITADO;
    }

    /**
     * Classe assincrona para conexão ao bluetooth remoto
     */
    public class Conectar extends AsyncTask<Void, Void, Void> {

        public AsyncResponse delegate = null;

        private ProgressDialog dialog;
        private String message = "";
        private boolean error = false;

        public Conectar (AsyncResponse asyncResponse){
            this.dialog = new ProgressDialog(context);
            delegate = asyncResponse;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(BluetoothConstants.LOG_TAG, BluetoothConstants.MSG_LOG_BLUETOOTH_CONNECTING);

            // Configura um ponto de conexão com o MAC
            btDevice = btAdapter.getRemoteDevice(BluetoothConstants.MAC_BLUETOOTH);

            try {
                Log.d(BluetoothConstants.LOG_TAG, BluetoothConstants.MSG_LOG_BLUETOOTH_MAC + btDevice.getAddress());

                // Abrindo socket da conexao bluetooth atraves do UUID especificado
                btSocket = btDevice.createRfcommSocketToServiceRecord(BluetoothConstants.UUID_BLUETOOTH);

                // Recurso de discovery é intensivo, uma vez conectado não precisamos mais deixá-lo ativado
                btAdapter.cancelDiscovery();

                // Estabelecendo a conexão efetivamente
                btSocket.connect();
                message = BluetoothConstants.MSG_BLUETOOTH_CONNECTED;

                // Cria stream de dados para transmissão de mensagens.
                outStream = btSocket.getOutputStream();

            } catch (IOException e) {

                message = BluetoothConstants.MSG_LOG_CONNECTION_ERROR + e.getMessage() + ".";
                error = true;
                Log.d(BluetoothConstants.LOG_TAG, message);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(activity, "",
                    BluetoothConstants.MSG_BLUETOOTH_CONNECTING, true);
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(!BluetoothConstants.MSG_EMPTY.equals(message)) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            if (!error) delegate.processFinish(true);
        }
    }

    /**
     * Classe assincrona para desconectar de bluetooth remoto
     */
    private class Desconectar extends AsyncTask<Void, Void, Void> {

        public AsyncResponse delegate = null;

        private ProgressDialog dialog;
        private String message = "";

        public Desconectar (AsyncResponse asyncResponse) {
            this.dialog = new ProgressDialog(context);
            delegate = asyncResponse;
        }

        protected Void doInBackground(Void... params) {

            if (btSocket != null) {
                try {
                    // Fecha o socket e libera variáveis de conexão
                    btSocket.close();
                    btSocket = null;
                    message = BluetoothConstants.MSG_BLUETOOTH_DISCONNECTED;
                } catch (IOException e) {
                    Log.e(BluetoothConstants.LOG_TAG, BluetoothConstants.MSG_LOG_DISCONNECTION_ERROR + e.getMessage());
                }
            } else {
                message = BluetoothConstants.MSG_BLUETOOTH_NO_CONNECTION;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(activity, "",
                    BluetoothConstants.MSG_BLUETOOTH_DISCONNECTING, true);
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(!BluetoothConstants.MSG_EMPTY.equals(message)) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                delegate.processFinish(true);
            }
        }
    }

    /**
     * Método para envio de dados para o bluetooth remoto
     *
     * @param message mensagem a ser enviada
     */
    public void sendData(String message) {

        Log.d(BluetoothConstants.LOG_TAG, BluetoothConstants.MSG_LOG_SENDING_DATA + message);

        try {
            message += '\r';
            byte[] msgBuffer = message.getBytes();
            outStream.write(msgBuffer);
        } catch (IOException e) {

            StringBuilder errMessage = new StringBuilder(BluetoothConstants.MSG_BLUETOOTH_FATAL_ERROR);

            if (BluetoothConstants.MAC_BLUETOOTH.equals(BluetoothConstants.MAC_BLUETOOTH_ERROR)) {
                errMessage.append(BluetoothConstants.MSG_BLUETOOTH_ERROR_RESUME);
            } else {
                errMessage.append(String.format(BluetoothConstants.MSG_BLUETOOTH_ERROR_RESUME2,
                        e.getMessage(), BluetoothConstants.UUID_BLUETOOTH.toString()));
            }

            Toast.makeText(context, errMessage.toString(), Toast.LENGTH_SHORT).show();

            activity.finish();
        }
    }

    /**
     * Método para verificar se há uma conexão ativa antes de tentar enviar um dado
     *
     * @return true se conexão está ativa
     */
    public boolean isConnected() {

        if (btSocket != null && btSocket.isConnected()) return true;
        return false;
    }

}