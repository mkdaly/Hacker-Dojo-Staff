package net.metamike.hackerdojo.widget;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Person implements Parcelable {
	private static final String TAG = "Person";

	private long md5;
	private String name;
	private String time;
	private Bitmap image;

	public Person() {}

	public Person(String name, String time, String imageURL) {
		this.name = name;
		this.time = time;
		try {
			setGravatar(new URL(imageURL));
		} catch (MalformedURLException mfu) {
			Log.e(TAG, "IO Error.", mfu);
		}
	}
	
	private Person(Parcel src) {
		name = src.readString();
		time = src.readString();
		md5 = src.readLong();
		image = src.readParcelable(null);
	}
	
	public boolean verify() {
		//Check that there is something set for name
		if (name == null || "".equals(name)) { return false; }
		
		//Check for image
//		if (image == null || "".equals(name)) { return false; }
		
		return true;
	}
	
	public void setMD5(long md5) {
		this.md5 = md5;
	}
	
	public long getMD5() {
		return this.md5;
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
	
	public Bitmap getGravatarImage() {
		return image;
	}

	public void setGravatar(URL gravatar) {
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(time);
		dest.writeLong(md5);
		dest.writeParcelable(image, flags);	
	}
	
	public static final Parcelable.Creator<Person> CREATOR = 
		new Parcelable.Creator<Person>() {
			@Override
			public Person createFromParcel(Parcel in) {
				return new Person(in);	
			}

			@Override
			public Person[] newArray(int size) {
				return new Person[size];
			}
		};
}