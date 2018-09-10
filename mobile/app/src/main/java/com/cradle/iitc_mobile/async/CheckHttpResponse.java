package com.cradle.iitc_mobile.async;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cradle.iitc_mobile.IITC_Mobile;
import com.cradle.iitc_mobile.Log;

import java.lang.ref.WeakReference;

/*
 * this class parses the http response of a web page.
 * since network operations shouldn't be done on main UI thread
 * (NetworkOnMainThread exception is thrown) we use an async task for this.
 */
public class CheckHttpResponse extends AsyncTask<String, Void, Boolean> {

    private final WeakReference<Context> contextRef;
    private final WeakReference<RequestQueue> rQueueRef;
    private final WeakReference<IITC_Mobile> iitcRef; // TODO: Reconsider use of IITC_Mobile refs

    public CheckHttpResponse(final IITC_Mobile iitc) {
        contextRef = new WeakReference<>(iitc.getApplicationContext());
        rQueueRef = new WeakReference<>(iitc.getRequestQueue());
        iitcRef = new WeakReference<>(iitc);
    }

    @Override
    protected Boolean doInBackground(final String... urls) {
        final Context context = contextRef.get();
        final RequestQueue rQueue = rQueueRef.get();
        // check http responses and disable splash screen on error
        if (rQueue != null && context != null) {
            String url = urls[0];
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Handle Response
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("HTTP Error Received: url=" + urls[0] + " code=" + error.networkResponse.statusCode);
                            error.printStackTrace();
                            // Tell IITC_Mobile to .runOnUiThread() to setLoadingState to false
                        }
                    });
        }

        return false;
    }

    /*
     * TEMPORARY WORKAROUND for Google login fail
     */
    @Override
    protected void onPostExecute(final Boolean aBoolean) {
        //final Context context = contextRef.get();
        //final RequestQueue rQueue = rQueueRef.get();
        final IITC_Mobile mIitc = iitcRef.get();
        if (aBoolean) {
            Log.d("google auth error, redirecting to work-around page");
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mIitc);

            // set title
            alertDialogBuilder.setTitle("LOGIN FAILED!");

            // set dialog message
            alertDialogBuilder
                    .setMessage("This is caused by Google and hopefully fixed soon. " +
                            "To workaround this issue:\n" +
                            "• Choose 'Cancel' when asked to choose an account " +
                            "and manually enter your email address and password into the web page\n" +
                            "• If you don't see the account chooser, delete apps cache/data " +
                            "to force a new login session and handle it as described above")
                    .setCancelable(true)
                    .setNeutralButton("Reload now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            dialog.cancel();
                            mIitc.reloadIITC();
                        }
                    });

            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }
}
