package ca.uqac.game.android;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class VideoServer {
	public interface ClientEvent {
		void onConnected(FileDescriptor fd);

		void onDisconnected();
	}

	static final int NETWORKBUFFERSIZE = 65536;
	static final String TAG = VideoServer.class.getSimpleName();

	static final String LOCALADDR = "ca.uqac.videoserver";
	static final String SERVERIP = "23.94.26.24";
	static final int SERVERPORT = 8080;
	static final InetSocketAddress SERVERADDR = new InetSocketAddress(SERVERIP,
			SERVERPORT);

	LocalSocket localClient;
	LocalSocket localServer;
	LocalServerSocket localListen;
	
	Thread tunnelThread;
	Thread clientThread;

	ClientEvent eventHandler = null;
	LinkedBlockingQueue<byte[]> packetQueue = new LinkedBlockingQueue<byte[]>();

	boolean running = true;

	public VideoServer() {
		try {
			localListen = new LocalServerSocket(LOCALADDR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean initialize() {
		localClient = new LocalSocket();

		try {
			localClient.connect(new LocalSocketAddress(LOCALADDR));
			localClient.setReceiveBufferSize(NETWORKBUFFERSIZE);
			localClient.setSendBufferSize(NETWORKBUFFERSIZE);
			localServer = localListen.accept();
			localServer.setReceiveBufferSize(NETWORKBUFFERSIZE);
			localServer.setSendBufferSize(NETWORKBUFFERSIZE);

			Log.i(TAG, "Initialized");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		clientThread = new ClientThread();
		tunnelThread = new TunnelThread();

		clientThread.start();
		tunnelThread.start();

		return true;
	}

	public void setEventHandler(ClientEvent handler) {
		this.eventHandler = handler;
	}

	public FileDescriptor getVideoFD() {
		return localServer.getFileDescriptor();
	}

	class TunnelThread extends Thread {

		@Override
		public void run() {
			byte[] buffer = new byte[1024 * 1024];
			InputStream inputStream;
			try {
				inputStream = localClient.getInputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			
			FileOutputStream fop;
			File f = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/Download/video1.3gp");
			try {
				fop = new FileOutputStream(f);
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
				return;
			}

			int rlen = 0;
			while (running) {
				rlen = 0;
				try {
					rlen = inputStream.read(buffer);
				} catch (IOException e1) {
					e1.printStackTrace();
					break;
				}
				
				if (rlen < 0) {
					break;
				}

				System.out.println("read " + rlen + " bytes");
				if (rlen > 0) {
					try {
						fop.write(buffer, 0, rlen);
						fop.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						packetQueue.put(Arrays.copyOfRange(buffer, 0, rlen));
					} catch (InterruptedException e2) {
						e2.printStackTrace();
						break;
					}
				}
			}
			
			try {
				fop.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ClientThread extends Thread {

		@Override
		public void run() {
			Socket client = null;
			int cnt = 0;
			
			while (running) {
				try {
					if (client == null) {
						Log.i(TAG, "Connecting to server " + SERVERIP + ":"
								+ SERVERPORT);
						client = new Socket(SERVERIP, SERVERPORT);
						Log.i(TAG, "Connected to server " + SERVERIP + ":"
								+ SERVERPORT);
						client.setSendBufferSize(NETWORKBUFFERSIZE);
					}

					byte[] packet = packetQueue
							.poll(300, TimeUnit.MILLISECONDS);
					if (packet != null) {
						OutputStream out = client.getOutputStream();
						out.write(packet);

						System.out.println("written " + packet.length + " bytes");

						++cnt;

						if (cnt >= 50) {
							Log.i(TAG, "Sent 50 packets, there are still "
									+ packetQueue.size() + " in the queue.");
							cnt = 0;
						}
					}
				} catch (IOException e) {
					if (client != null) {
						Log.i(TAG, "Closing the connection");
						try {
							client.close();
						} catch (IOException e1) {
						}
					}
					client = null;
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}

			if (client != null) {
				Log.i(TAG, "Closing the connection");
				try {
					client.close();
				} catch (IOException e1) {
				}
			}

		}
	}
	
	public void close() {
		running = false;
		Log.i(TAG, "Closing VideoServer");
		try {
			clientThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			tunnelThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Log.i(TAG, "Closed VideoServer");
	}
}
