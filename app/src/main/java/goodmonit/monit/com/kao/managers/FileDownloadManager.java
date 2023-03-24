package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import goodmonit.monit.com.kao.constants.Configuration;
import goodmonit.monit.com.kao.dfu.FirmwareUpdateActivity;

public class FileDownloadManager extends AsyncTask<String, Integer, String> {
	private static final String TAG = Configuration.BASE_TAG + "FileDownloadMgr";
	private static final boolean DBG = Configuration.DBG;

	private static Context mContext;
	private String mUrl;
	private String mFileName;
	private FileDownloadListener mListener;
	private Handler mProgressHandler;
	private boolean mAllowAnyCertificate = true;

	public FileDownloadManager(Context context) {
		mContext = context;
	}

	public void setDownloadListener(FileDownloadListener listener) {
		mListener = listener;
	}

	public void setDownloadUrl(String url) {
		mUrl = url;
	}

	public void setDownloadFileName(String name) {
		mFileName = name;
	}

	public String getDownloadFileName() {
		return mFileName;
	}

	public void setProgressHandler(Handler handler) {
		mProgressHandler = handler;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	private void _trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[]{};
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain,
					String authType)
					throws java.security.cert.CertificateException {
				if (mAllowAnyCertificate) {
					if (DBG) Log.d(TAG, "ClientTrusted");
				} else {
					throw new java.security.cert.CertificateException();
				}
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain,
					String authType)
					throws java.security.cert.CertificateException {
				if (mAllowAnyCertificate) {
					if (DBG) Log.d(TAG, "ServerTrusted");
				} else {
					throw new java.security.cert.CertificateException();
				}
			}
		}};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		if (DBG) Log.d(TAG, "doInBackground : " + mUrl);
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			URL downloadUrl = new URL(mUrl);
			//서버와 접속하는 클라이언트 객체 생성
			HttpURLConnection conn = null;

			_trustAllHosts();
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) downloadUrl.openConnection();
			httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String s, SSLSession sslSession) {
					if (mAllowAnyCertificate) {
						return true;
					} else {
						return false;
					}
				}
			});
			conn = httpsURLConnection;
			int response = conn.getResponseCode();
			String filePath = mContext.getFilesDir().getPath() + "/" + mFileName;
			/*
			String filePath = Environment.getExternalStorageDirectory().getPath() + "/monit";
			File folder = new File(filePath);
			if (!folder.exists()) {
				folder.mkdir();
			}
			filePath += "/" + mFileName;
			*/
			File file = new File(filePath);
			inStream = conn.getInputStream();
			outStream = new FileOutputStream(file);

			byte[] buf = new byte[1024];
			int len = 0;
			int total = 0;
			int fileSize = conn.getContentLength();
			while ((len = inStream.read(buf)) > 0) {
				outStream.write(buf, 0, len);
				total += len;
				mProgressHandler.obtainMessage(FirmwareUpdateActivity.MSG_FIRMWARE_DOWNLOAD_PROGRESS, (int)((total + 0.0) / fileSize * 100), total).sendToTarget();
			}

			outStream.close();
			inStream.close();
			conn.disconnect();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, "exception : " + e.toString());
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException ioe) {
				}
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException ioe) {
				}
			}
			return "300";
		}

		return "200";
	}

	@Override
	protected void onPostExecute(String obj) {
		super.onPostExecute(obj);
		if (DBG) Log.d(TAG, "post : " + obj);
		if (mListener != null) {
			if ("200".equals(obj)) {
				mListener.onSucceeded(mFileName);
			} else {
				mListener.onFailed(mFileName);
			}
		}
	}

	// Listener
	public static class FileDownloadListener {
		public void onSucceeded(String filename) {}
		public void onFailed(String filename) {}
	}

}
