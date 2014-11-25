package ca.uqac.game.android;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.content.Context;

public class UploadService extends Thread {
	static final String URL = "http://tooth.phoenixxie.com/upload.php";

	static boolean running = true;

	Context context;
	String uuid;

	LinkedBlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>();

	public UploadService(Context context, String uuid) {
		this.uuid = uuid;
		this.context = context;
	}

	public void addFile(String file) {
		try {
			fileQueue.put(file);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				String filepath = fileQueue.poll(500, TimeUnit.MILLISECONDS);
				if (filepath != null) {
					upload(uuid, filepath);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void upload(String uuid, String filepath) {
		MultipartEntity entity = new MultipartEntity();

		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
		HttpPost httppost = new HttpPost(URL);

		File file = new File(filepath);

		try {
			entity.addPart("mp4", new FileBody(file));
			entity.addPart("uuid", new StringBody(uuid));
			entity.addPart("key", new StringBody("justakey"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		httppost.setEntity(entity);

		try {
			httpclient.execute(httppost);
		} catch (Exception e) {
		}

		file.delete();
	}
}
