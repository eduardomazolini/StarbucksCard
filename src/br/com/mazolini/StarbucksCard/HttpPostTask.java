package br.com.mazolini.StarbucksCard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HttpPostTask extends AsyncTask<URI, Integer, String> {
	List<NameValuePair> listPost;
	private Activity activity;
	public HttpPostTask(Activity activity){
	    this.activity=activity;

	}


	@Override
	protected String doInBackground(URI... urls) {
        //if (isCancelled()) break;
		
		HttpEntity entity = null;
		String filterResult = null;
		for (URI url : urls){
            HttpClient client = createHttpClient();
            HttpPost httpPost = new HttpPost(url);
            
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(listPost));
                HttpResponse response = client.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                
                if (statusCode == 200) {
                	 entity = response.getEntity();
     				InputStream inputStream = entity.getContent();
    				filterResult = inputStreamToString(inputStream);

                } else {
                    Log.e("doInBackground", "Erro");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
        return filterResult;
	}

	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		String saldo;
		String date;
		ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
		if (result!=null){
			try {
				final Integer saldoInicio = result.indexOf("fetch_balance_value")+"fetch_balance_value".length()+2;
				final Integer saldoFim = result.indexOf("<",saldoInicio);
				saldo = result.substring(saldoInicio, saldoFim);
				Log.d("","Index of fetch_balance_value: "+ saldo);
				final Integer dateInicio = result.indexOf("date")+"date".length()+2;
				final Integer dateFim = result.indexOf("<",dateInicio);
				date = result.substring(dateInicio, dateFim);
				Log.d("","Index of date: "+ date);
				final TextView saldoView = (TextView) activity.findViewById(R.id.saldo);
				saldoView.setText(saldo);
				saldoView.setVisibility(View.VISIBLE);
				final TextView saldoDataView = (TextView) activity.findViewById(R.id.saldoData);
				saldoDataView.setText(date);
				saldoDataView.setVisibility(View.VISIBLE);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (StringIndexOutOfBoundsException e){
				saldo = "Cartão ou Pin inválido";
				date = "";
				e.printStackTrace();
				
			}
		}
		
	}


	
	public HttpClient createHttpClient() {
	     try {
	         final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	         trustStore.load(null, null);

	         final SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	         sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	         final HttpParams params = new BasicHttpParams();
	         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	         HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
	         HttpProtocolParams.setUserAgent(params, "br.com.mazolini.cafe");
	         HttpProtocolParams.setUseExpectContinue(params, false);

	         final SchemeRegistry registry = new SchemeRegistry();
	         registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	         registry.register(new Scheme("https", sf, 443));
	         final ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	         return new DefaultHttpClient(ccm, params);
	     } catch (Exception e) {
	         return new DefaultHttpClient();
	     }
	}
	private String inputStreamToString(InputStream is) throws IOException {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    while ((line = rd.readLine()) != null) { 
	    	if (line.contains("fetch_balance_value")) {
	    		Log.d("inputStreamToStringinputStreamToString","line: "+line);
		    	    total.append(line); 	    		
	    	}
	    	if (line.contains("date")) {
	    		Log.d("inputStreamToStringinputStreamToString","line: "+line);
		    	total.append(line);		
	    	}
	    }
	    // Return full string
	    return total.toString();
	}


	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		final ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		
	}
   	
}
