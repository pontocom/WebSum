package pt.iscte.daam.websum;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final OkHttpClient okHttpClient = new OkHttpClient();


    EditText etVA, etVB;
    TextView tvR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etVA = (EditText) findViewById(R.id.etValorA);
        etVB = (EditText) findViewById(R.id.etValorB);
        tvR = (TextView) findViewById(R.id.tvResultado);


    }

    /* this is used to update the components on the UI thread, since the okHttpClient runs on a background thread*/
    private void mySetText(final TextView textView, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(value);
            }
        });
    }

    /* calling the service using the OkHttp library */
    public void webSumGoOkHTTP(View v) {
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        RequestBody formBody = new FormBody.Builder()
                .add("va", etVA.getText().toString())
                .add("vb", etVB.getText().toString())
                .build();

        Request request = new Request.Builder()
                .url("http://www.carlosserrao.net/test/add.php")
                .post(formBody)
                .build();

        dialog.setMessage("Sending values to server with okHTTP, waiting for answer...");
        dialog.show();

        /* this way, okHTTP will make and async call */
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog.dismiss();
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Some error occured while communicating with the service.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                dialog.dismiss();
                if (!response.isSuccessful()) {
                    Log.i("WebSum", "Unexpected code: " + response);
                    Toast.makeText(MainActivity.this, "Some error occured while communicating with the service.", Toast.LENGTH_SHORT).show();
                } else {
                    String result = response.body().string();
                    Log.i("WebSum", "Response = " + result);

                    mySetText(tvR, result);
                }

            }

        });
    }

    /* calling the service using the Volley library */
    public void webSumGoVolley(View v) {
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Sending values to server with Volley, waiting for answer...");
        dialog.show();

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, "http://www.carlosserrao.net/test/add.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        tvR.setText(response);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("va", etVA.getText().toString());
                params.put("vb", etVB.getText().toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
