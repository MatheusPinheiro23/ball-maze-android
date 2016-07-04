package br.edu.ifsp.sbv.ball_maze_android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import br.edu.ifsp.sbv.ball_maze_android.interfaces.AsyncResponse;
import br.edu.ifsp.sbv.ball_maze_android.utils.BluetoothConnection;
import br.edu.ifsp.sbv.ball_maze_android.utils.BluetoothConstants;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Menu menu;
    private BluetoothConnection btConn;

    // Variáveis para manipulação dos sensores
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView xText;
    private TextView yText;
    private TextView zText;

    private Integer xOldValue = 0;
    private Integer yOldValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Verifica e cria coneão bluetooth
        btConn = new BluetoothConnection(getApplicationContext(), this);

        // Inicializa sensor acelerometro
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Tem acelerometro
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        } else {
            // Nao tem acelerometro
            Toast.makeText(getApplicationContext(), "Não existe suporte para o sensor acelerômetro", Toast.LENGTH_SHORT).show();
        }

        xText = (TextView) findViewById(R.id.textViewX);
        yText = (TextView) findViewById(R.id.textViewY);
        zText = (TextView) findViewById(R.id.textViewZ);
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

        if (btConn.isConnected()
                && (x.intValue() != xOldValue || y.intValue() != yOldValue)) {

                btConn.sendData(String.valueOf(x.intValue() + ";" + y.intValue()));
                xOldValue = x.intValue();
                yOldValue = y.intValue();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_conectar:

                if (btConn.verificaStatusBluetooth() == BluetoothConstants.BT_STATUS_HABILITADO) {

                    btConn.conectar(new AsyncResponse() {
                        @Override
                        public void processFinish(Object output) {
                            menu.findItem(R.id.action_conectar).setVisible(false);
                            menu.findItem(R.id.action_desconectar).setVisible(true);
                        }
                    });
                } else {
                    btConn.verificarBluetooth();
                }
                break;

            case R.id.action_desconectar:

                btConn.sendData(String.valueOf("-10;-10"));

                btConn.desconectar(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        menu.findItem(R.id.action_conectar).setVisible(true);
                        menu.findItem(R.id.action_desconectar).setVisible(false);
                    }
                });
                break;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItemDesconectar = menu.findItem(R.id.action_desconectar);
        menuItemDesconectar.setVisible(false);

        return true;
    }
}
