package br.edu.ifsp.sbv.ball_maze_android;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView xText;
    private TextView yText;
    private TextView zText;

    private Menu menu;

    public static final int BLUETOOTH_ACTIVATION_CODE = 1;
    private static final String endereco_MAC_do_Bluetooth_Remoto = "98:D3:31:40:23:26";
    private static final UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Represents a remote Bluetooth device.
    private BluetoothDevice dispositivoBluetoohRemoto;
    // Represents the local device Bluetooth adapter.
    private BluetoothAdapter meuBluetoothAdapter = null;
    // A connected or connecting Bluetooth socket.
    private BluetoothSocket bluetoothSocket = null;
    private InputStream inputStream = null;
    private OutputStream outStream = null;

    private Integer xOldValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bluetooth
        verificarConexaoBluetooth();

        // Sensor acelerometro
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Tem acelerometro
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        } else {
            // Nao tem acelerometro
        }

        xText = (TextView) findViewById(R.id.textViewX);
        yText = (TextView) findViewById(R.id.textViewY);
        zText = (TextView) findViewById(R.id.textViewZ);
    }

    public void verificarConexaoBluetooth() {
        // Get a handle to the default local Bluetooth adapter.
        meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Verifica se o celular tem Bluetooth
        if (meuBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Dispositivo não possui adaptador Bluetooth", Toast.LENGTH_LONG).show();
            // Finaliza a aplicação.
            finish();
        } else {
        // Verifica se o bluetooth está desligado. Se sim, pede permissão para ligar.
            if (!meuBluetoothAdapter.isEnabled()) {
                Intent novoIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(novoIntent, BLUETOOTH_ACTIVATION_CODE);
            }
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Float x = event.values[0];
        Float y = event.values[1];
        Float z = event.values[2];

         /*
        Os valores ocilam de -10 a 10.
        Quanto maior o valor de X mais ele ta caindo para a esquerda - Positivo Esqueda
        Quanto menor o valor de X mais ele ta caindo para a direita  - Negativo Direita
        Se o valor de  X for 0 então o celular ta em pé - Nem Direita Nem Esquerda
        Se o valor de Y for 0 então o cel ta "deitado"
         Se o valor de Y for negativo então ta de cabeça pra baixo, então quanto menor y mais ele ta inclinando pra ir pra baixo
        Se o valor de Z for 0 então o dispositivo esta reto na horizontal.
        Quanto maioro o valor de Z Mais ele esta inclinado para frente
        Quanto menor o valor de Z Mais ele esta inclinado para traz.
        */
        xText.setText("Posição X: " + x.intValue() + " Float: " + x);
        yText.setText("Posição Y: " + y.intValue() + " Float: " + y);
        zText.setText("Posição Z: " + z.intValue() + " Float: " + z);

        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {

            if (x.intValue() != xOldValue) {
                sendData(String.valueOf(x.intValue()));
                xOldValue = x.intValue();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_conectar: new Conectar().execute(); break;
            case R.id.action_desconectar: new Desconectar().execute(); break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    private class Conectar extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        private String message = "";

        protected Void doInBackground(Void... params) {

            if (BluetoothAdapter.checkBluetoothAddress(endereco_MAC_do_Bluetooth_Remoto)) {
                dispositivoBluetoohRemoto = meuBluetoothAdapter.getRemoteDevice(endereco_MAC_do_Bluetooth_Remoto);

                try {
                    bluetoothSocket = dispositivoBluetoohRemoto.createInsecureRfcommSocketToServiceRecord(MEU_UUID);
                    bluetoothSocket.connect();

                    message = "Conectado";

                    // Create a data stream so we can talk to server.
                    try {
                        outStream = bluetoothSocket.getOutputStream();
                    } catch (IOException e) {
                        message = "In onResume() and output stream creation failed:" + e.getMessage() + ".";
                    }
                } catch (IOException e) {
                    Log.e("ERRO AO CONECTAR", "O erro foi" + e.getMessage());
                    message = "Conexão não foi estabelecida";
                }
            } else {
                message = "Endereço MAC do dispositivo Bluetooth remoto não é válido";
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "",
                    "Conectando...", true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(!"".equals(message)) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                if("CONECTADO".equalsIgnoreCase(message)) {
                    MenuItem menuItemConectar = menu.findItem(R.id.action_conectar);
                    menuItemConectar.setVisible(false);

                    MenuItem menuItemDesconectar = menu.findItem(R.id.action_desconectar);
                    menuItemDesconectar.setVisible(true);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItemDesconectar = menu.findItem(R.id.action_desconectar);
        menuItemDesconectar.setVisible(false);

        return true;
    }

    private class Desconectar extends AsyncTask<String, Integer, Void> {

        private ProgressDialog dialog;
        private String message = "";

        protected Void doInBackground(String... params) {

            if (bluetoothSocket != null) {
                try {

                    // Immediately close this socket, and release all associated resources.
                    bluetoothSocket.close();
                    bluetoothSocket = null;
                    message = "Desconectado";
                } catch (IOException e) {
                    Log.e("ERRO AO DESCONECTAR", "O erro foi" + e.getMessage());

                }
            } else {
                message = "Não há nenhuma conexão estabelecida a ser desconectada";
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "",
                    "Desconectando...", true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if(!"".equals(message)) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                if("DESCONECTADO".equalsIgnoreCase(message)) {
                    MenuItem menuItemConectar = menu.findItem(R.id.action_conectar);
                    menuItemConectar.setVisible(true);

                    MenuItem menuItemDesconectar = menu.findItem(R.id.action_desconectar);
                    menuItemDesconectar.setVisible(false);
                }
            }
        }
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (endereco_MAC_do_Bluetooth_Remoto.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address to the correct address in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MEU_UUID.toString() + " exists on server.\n\n";
            errorExit("Fatal Error", msg);
        }
    }

    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }
}
