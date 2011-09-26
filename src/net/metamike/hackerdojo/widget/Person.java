package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Person {
	private static final String TAG = "Person";

	private String name;
	private String time;
	private URL gravatar;
	private Bitmap image;

	
	public boolean verify() {
		//Check that there is something set for name
		if (name == null || "".equals(name)) { return false; }
		
		//Check for image
//		if (image == null || "".equals(name)) { return false; }
		
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public URL getGravatarURL() {
		return gravatar;
	}
	
	public Bitmap getGravatarImage() {
		return image;
	}

	public void setGravatar(URL gravatar) {
		this.gravatar = gravatar;
		//This might not be the best way
		//Might want to try a static ECS queue
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection)gravatar.openConnection();
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream is = connection.getInputStream();
				image = BitmapFactory.decodeStream(is);
				is.close();
			}
			connection.disconnect();
		} catch (IOException e) {
			Log.e(TAG, "IO Error.", e);
			e.printStackTrace();
		}
	}
	
	public void setGravatar(Resources rez, int id) {
		image = BitmapFactory.decodeResource(rez, id);
	}
}
