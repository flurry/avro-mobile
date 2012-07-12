package com.flurry.avroclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.util.Utf8;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.avroserver.protocol.v1.AdRequest;

public class AvroClientActivity extends Activity implements OnClickListener
{
	private String kLogTag = getClass().getSimpleName();
	private Button fSendRequestButton;
	private EditText fAdSpaceNameEditText;
	private TextView fResponseOutputTextView;
	private String serverUrl;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.main);

		fSendRequestButton = (Button) findViewById(R.id.send_request_button);
		fAdSpaceNameEditText = (EditText) findViewById(R.id.ad_space_name_edit_text);
		fResponseOutputTextView = (TextView) findViewById(R.id.response_output_text_view);

		fSendRequestButton.setOnClickListener(this);

		serverUrl = getResources().getString(R.string.server_url);
	}

	@Override
	public void onClick(View view)
	{
		if (view == fSendRequestButton)
		{
			String adSpaceName = fAdSpaceNameEditText.getText().toString();
			if (adSpaceName.length() == 0)
			{
				Toast.makeText(this, "Please enter an ad space name", Toast.LENGTH_LONG).show();
			}
			else
			{
				new AvroRequestAsyncTask().execute(adSpaceName);
			}
		}
	}

	private class AvroRequestAsyncTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... adSpaceNames)
		{
			if(adSpaceNames.length == 0)
			{
				return null;
			}

			try
	        {
	        	// endcode Avro binary
	        	AdRequest adRequest = AdRequest.newBuilder().setAdSpaceName(adSpaceNames[0]).build();
	        	SpecificDatumWriter<AdRequest> writer = new SpecificDatumWriter<AdRequest>(AdRequest.class);
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
	            
	            writer.write(adRequest, encoder);
	            encoder.flush();

	        	// do HTTP POST
				AndroidHttpClient http = AndroidHttpClient.newInstance("AvroClient");
				HttpResponse res = null;
				HttpPost post = new HttpPost(serverUrl);
				post.setHeader("Accept", "application/json");
				post.setHeader("Content-Type", "avro/binary");
				post.setEntity(new ByteArrayEntity(outputStream.toByteArray()));
				res = http.execute(post);
				
				outputStream = new ByteArrayOutputStream(1024);
				res.getEntity().writeTo(outputStream);
				return outputStream.toString();
	        }
	        catch (IOException ioe)
	        {
	        	Log.e(kLogTag, ioe.toString());
	        	return null;
	        }
		}

		@Override
		protected void onPostExecute(String result)
		{
			if(result == null)
			{
				return;
			}

			fResponseOutputTextView.setText(result);
		}
	}
}