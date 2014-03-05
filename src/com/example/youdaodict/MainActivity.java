package com.example.youdaodict;

import java.io.IOException;
import java.net.URLEncoder;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static String translateContent;
	public static String result;
	public static String url;
	public static String jsonString;
	protected Runnable doGet;
	ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button)findViewById(R.id.btn_translate);
		final EditText et_translate = (EditText)findViewById(R.id.et_translate);
		final TextView translate_content = (TextView)findViewById(R.id.translate_content);
		final TextView basic_content = (TextView)findViewById(R.id.basic_content);
		final TextView web_content = (TextView)findViewById(R.id.web_content);
		btn.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				if(isNectworkConnected() == false){
					Toast.makeText(MainActivity.this, "No Network.", Toast.LENGTH_SHORT).show();
					return;
				}
				translateContent = et_translate.getText().toString();
				if(translateContent.equals("")){
					Toast.makeText(MainActivity.this, "Empty KeyWord.", Toast.LENGTH_SHORT).show();
					return;
				}
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setMessage("Translating...");
				progressDialog.setProgressStyle(TRIM_MEMORY_UI_HIDDEN);
				progressDialog.show();
				new Thread(doGet).start();
			}
		});
		final Handler handler = new Handler(){
			public void handleMessage(Message msg){
				switch(msg.what){
				case 0:
					try {
						displayQueryResult(result);
						if(progressDialog.isShowing()){
							progressDialog.hide();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				}
				super.handleMessage(msg);
			}

			/**
			 * Parsing the JSON format string 
			 * @param result
			 * @throws JSONException
			 */
			private void displayQueryResult(String result) throws JSONException {
				String text = "";
				JSONObject json = new JSONObject(result);
				int errorCode = json.getInt("errorCode");
				if(errorCode != 0){
					switch(errorCode){
					case 20:
						text += "The String is too long\n";
						break;
					case 30:
						text +="Translation error\n";
						break;
					case 40:
						text +="Unavailable String Type\n";
						break;
					case 50:
						text +="Unavailable KEY\n";
						break;
					default :
						break;
					}
					translate_content.setText(text);
					return;
				}

				String query = json.optString("query");
				JSONArray translation = json.optJSONArray("translation");
				
				for(int i=0;i<translation.length();i++){
					text += translation.optString(i);
					text += " ";
				}
				text += "\n";
				translate_content.setText(text);
				text = "";
				if(!json.isNull("basic")){
					text += "Original: " + query + "\n";
					JSONObject basic = json.optJSONObject("basic");
					text += "Phonetic: ";
					text += "["+basic.optString("phonetic") + "]" + "\n";
					JSONArray explains = basic.optJSONArray("explains");
					text += "Translate: ";
					for(int i=0;i<explains.length();i++){
						text += explains.optString(i);
						text += " ";
					}
					text += "\n";
				}
				basic_content.setText(text);
				text = "";
				if(!json.isNull("web")){
					JSONArray web = json.optJSONArray("web");
					for(int i=0;i<web.length();i++){
						JSONArray value = web.optJSONObject(i).optJSONArray("value");
						String key = web.optJSONObject(i).optString("key");
						text +=  i+1 + "." + key + "\n";
						for(int j=0;j<value.length();j++){
							text += value.optString(j);
							text += ",";
						}
						text = text.substring(0, text.length()-1);
						text += "\n";
					}
				}
				web_content.setText(text);
				text = "";
			}
		};
		
		/**
		 * get the API result
		 */
		doGet = new Runnable(){
			@Override
			public void run(){
				try{
					url = "http://fanyi.youdao.com/openapi.do?keyfrom=buzhidao&key=947500854&type=data&doctype=json&version=1.1&q="+URLEncoder.encode(translateContent,"UTF-8");
					HttpPost httppost = new HttpPost(url);
					try{
						HttpParams p = new BasicHttpParams();
						HttpClient httpclient = new DefaultHttpClient(p);
						ResponseHandler<String> responseHander = new BasicResponseHandler();
						String responseBody = httpclient.execute(httppost,responseHander);
						result = responseBody;
						Message msg = new Message();
						msg.what = 0;
						handler.sendMessage(msg);
					}catch(ClientProtocolException e){
						e.printStackTrace();
					}
				}catch(IOException e){
					e.printStackTrace();
				}

			}
		};
    }
    
    /**
     * Check whether the network is available or not
     */
    public boolean isNectworkConnected(){
    	ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
		if(networkInfo != null){
			return networkInfo.isAvailable();
		}
    	return false;
    }
}















