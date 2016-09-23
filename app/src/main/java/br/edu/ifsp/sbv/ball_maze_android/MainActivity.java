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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import br.edu.ifsp.sbv.ball_maze_android.interfaces.AsyncResponse;
import br.edu.ifsp.sbv.ball_maze_android.bluetooth.BluetoothConnection;
import br.edu.ifsp.sbv.ball_maze_android.bluetooth.BluetoothConstants;
import br.edu.ifsp.sbv.ball_maze_android.utils.BallMazeConstants;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Menu menu;
    private BluetoothConnection btConn;

    // Variáveis para manipulação dos sensores
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView xText;
    private TextView yText;
    private TextView zText;
    private TextView textBluetoothStatus;

    private ImageView mazeImageView;
    private ImageView bluetoothStatus;

    private Integer xOldValue = 0;
    private Integer yOldValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializa sensor acelerometro
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Tem acelerometro
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        } else {
            // Nao tem acelerometro
            Toast.makeText(getApplicationContext(), BallMazeConstants.ACCELEROMETER_NOT_SUPPORTED, Toast.LENGTH_SHORT).show();
            finish();
        }

        xText = (TextView) findViewById(R.id.textViewX);
        yText = (TextView) findViewById(R.id.textViewY);
        zText = (TextView) findViewById(R.id.textViewZ);
        textBluetoothStatus = (TextView) findViewById(R.id.textBluetoothStatus);

        mazeImageView = (ImageView) findViewById(R.id.mazeImageView);
        bluetoothStatus = (ImageView) findViewById(R.id.bluetoothStatus);

        // Verifica e cria conexão bluetooth
        btConn = new BluetoothConnection(getApplicationContext(), this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        xText.setText(String.format(BallMazeConstants.POSITION_MESSAGE, "X", x.intValue(), x));
        yText.setText(String.format(BallMazeConstants.POSITION_MESSAGE, "Y", y.intValue(), y));
        zText.setText(String.format(BallMazeConstants.POSITION_MESSAGE, "Z", z.intValue(), z));

        if (btConn.isConnected()
                && (x.intValue() != xOldValue || y.intValue() != yOldValue)) {

            btConn.sendData(String.valueOf(x.intValue() + ";" + y.intValue()));
            xOldValue = x.intValue();
            yOldValue = y.intValue();

            drawMazeImage(x.intValue(), y.intValue());
        }
    }

    /**
     * Metodo para desenhar o labirinto no aplicativo na posicao em que se encontra
     * @param x eixo X
     * @param y eixo Y
     */
    private void drawMazeImage(int x, int y) {

        int res = R.drawable.maze_front;

        if (x == 0) {
            if (y > 0) res = R.drawable.maze_down;
            if (y < 0) res = R.drawable.maze_up;
        } else if(y == 0) {
            if (x > 0) res = R.drawable.maze_left;
            if (x < 0) res = R.drawable.maze_right;
        } else if (x > 0 && y > 0) {
            res = R.drawable.maze_down_left;
        } else if (x < 0 && y < 0) {
            res = R.drawable.maze_up_right;
        } else if (x < 0 && y > 0) {
            res = R.drawable.maze_down_right;
        } else if (x > 0 && y < 0) {
            res = R.drawable.maze_up_left;
        }

        mazeImageView.setImageResource(res);
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
                            textBluetoothStatus.setText(R.string.label_conectado);
                            bluetoothStatus.setImageResource(R.drawable.connected);
                        }
                    });
                } else {
                    btConn.verificarBluetooth();
                }
                break;

            case R.id.action_desconectar:

                btConn.sendData(String.valueOf(BallMazeConstants.RESTART_POSITION));
                drawMazeImage(0,0);

                btConn.desconectar(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        menu.findItem(R.id.action_conectar).setVisible(true);
                        menu.findItem(R.id.action_desconectar).setVisible(false);
                        textBluetoothStatus.setText(R.string.label_desconectado);
                        bluetoothStatus.setImageResource(R.drawable.disconnected);
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
