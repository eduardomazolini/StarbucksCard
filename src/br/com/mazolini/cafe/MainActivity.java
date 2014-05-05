package br.com.mazolini.cafe;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import br.com.mazolini.cafe.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	final String TAG = "STARBUCKS";
	SharedPreferences cardPref;
	Editor cardPrefEditor;
	TextView cardNumberView;
	TextView cardPinView;
	TextView debugView;
	CheckBox saveView;
	String cardNumber;
	String cardPin;
	boolean save;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy);
		cardPref = getSharedPreferences("br.com.mazolini.StarbucksCar", MODE_PRIVATE);
		cardPrefEditor = cardPref.edit();
		cardNumberView = (TextView) findViewById(R.id.cardNumber);
		cardPinView = (TextView) findViewById(R.id.cardPin);
		saveView = (CheckBox) findViewById(R.id.save);
		cardNumber = cardPref.getString("cardNumber", "");
		cardPin = cardPref.getString("cardPin", "");
		save = cardPref.getBoolean("save", false);
		Log.d(TAG,"cardNumber: "+cardNumber);
		Log.d(TAG,"cardPin: "+"********");
		Log.d(TAG,"save: "+save);
		cardNumberView.setText(cardNumber);
		cardPinView.setText(cardPin);
		saveView.setChecked(save);
		
		
		//Animacao de entrade
		Animation slideDown = AnimationUtils.loadAnimation(this, R.animator.slide_down);
		LinearLayout fundoVerde = (LinearLayout) findViewById(R.id.bgVerde);
		fundoVerde.startAnimation(slideDown);
		ImageView imgLogo = (ImageView) findViewById(R.id.imgLogo);
		imgLogo.startAnimation(slideDown);
		
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
		fadeIn.setDuration(3000);
		LinearLayout content = (LinearLayout) findViewById(R.id.layoutContent);
		content.setAnimation(fadeIn);
				
	}

	
	public void onSaveClicked(View v){
		save = saveView.isChecked();
		Log.d(TAG,"OnSaveClicked-save: "+save);
		if (!save){
			cardPrefEditor.putString("cardNumber", "").putString("cardPin", "").putBoolean("save", false).commit();
		} else {
			cardPrefEditor.putString("cardNumber", cardNumber).putString("cardPin", cardPin).putBoolean("save", save).commit();
		}
	}
	public void onSubmitClicked(View v){		
		cardNumber = cardNumberView.getText().toString();
		cardPin = cardPinView.getText().toString();
		save = saveView.isChecked();
		
		if(cardNumber.length()<16){
			Toast.makeText(this, getResources().getString(R.string.numberEmptyError),Toast.LENGTH_LONG).show();
			return;
		}
		if(cardPin.length()<6){
			Toast.makeText(this, getResources().getString(R.string.pinEmptyError), Toast.LENGTH_LONG).show();
			return;
		}
		
		//Limpa o resultado
		TextView saldoView = (TextView)findViewById(R.id.saldo);
		saldoView.setVisibility(View.GONE);
		TextView saldoDataView = (TextView)findViewById(R.id.saldoData);
		saldoDataView.setVisibility(View.GONE);
		
		
		if (save){
			cardPrefEditor.putString("cardNumber", cardNumber).putString("cardPin", cardPin).putBoolean("save", save).commit();
		}
			
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("Card.Number", cardNumber));
        nameValuePairs.add(new BasicNameValuePair("Card.Pin", cardPin));
        HttpPostTask consulta = new HttpPostTask(this);
        consulta.listPost = nameValuePairs;
 		consulta.execute(URI.create("https://www.starbucks.com.br/card/guestbalance"));
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); //Dismiss keyboard
 		imm.hideSoftInputFromWindow(cardNumberView.getWindowToken(), 0);
	}

}
