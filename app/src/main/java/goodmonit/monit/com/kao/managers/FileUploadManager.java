package goodmonit.monit.com.kao.managers;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import goodmonit.monit.com.kao.constants.Configuration;

public class FileUploadManager {
	private static final String TAG = Configuration.BASE_TAG + "FileUploadMgr";
	private static final boolean DBG = Configuration.DBG;

	private static Context mContext;
	private PreferenceManager mPreferenceMgr;
	private ServerQueryManager mServerQueryMgr;
	private ArrayList<String> mNotDeleteFileList;
	private String storageContainer;
	private String storageConnectionString;

	public FileUploadManager(Context context) {
		mContext = context;
		mServerQueryMgr = ServerQueryManager.getInstance(mContext);
		mPreferenceMgr = PreferenceManager.getInstance(mContext);
		storageConnectionString = mServerQueryMgr.getParameter(202);
		storageContainer = mServerQueryMgr.getParameter(203);
	}

	protected boolean storeFileInBlobStorage(String filename){
		FileInputStream fis = null;
		try {
			if (DBG) Log.d(TAG, "storeFileInBlobStorage : " + filename);
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);

			// Create the blob client.
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

			// Retrieve reference to a previously created container.
			CloudBlobContainer container = blobClient.getContainerReference(storageContainer);
			// Create the container if it does not exist
			container.createIfNotExists();

			// Make the container public
			// Create a permissions object
			BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

			// Include public access in the permissions object
			containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

			// Set the permissions on the container
			container.uploadPermissions(containerPermissions);

			// Create or overwrite the blob (with the name "example.jpeg") with contents from a local file.
			String blobname = mPreferenceMgr.getAccountId() + "_" + filename;
			CloudBlockBlob blob = container.getBlockBlobReference(blobname);
			File source = new File(mContext.getFilesDir(), filename);
			fis = new FileInputStream(source);
			blob.upload(fis, source.length());
			fis.close();
		} catch (Exception e) {
			if (DBG) Log.e(TAG, "store failed: " + e.toString());
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {

				}
			}
			return false;
		}
		File file = new File(mContext.getFilesDir(), filename);
		file.delete();
		return true;
	}

    public void uploadFiles() {
		// Chat Message 저장
		//DebugManager.getInstance(mContext).saveChatMessageInternalFile();

		// File목록 읽어오기
		ArrayList<String> uploadFileList = new ArrayList<>();
		mNotDeleteFileList = new ArrayList<>();

		// 시간 설정
		String currDateString = DateFormat.format("yyMMdd", System.currentTimeMillis()).toString();
		//String currDateString = DateFormat.format("HHmm", System.currentTimeMillis()).toString();
		long currDate = Long.parseLong(currDateString);

		// 펌웨어파일 삭제
		String[] fileList = mContext.fileList();
		for (String fileName : fileList) {
			if (fileName == null) continue;
			if (fileName.contains("fw_")) {
				if (DBG) Log.d(TAG, "delete : [" + fileName + "]");
				File file = new File(mContext.getFilesDir(), fileName);
				file.delete();
			}
		}

		fileList = mContext.fileList();
		boolean isValid = false;
		for (String fileName : fileList) {
			String log = fileName;
			String[] parse = fileName.split("_");
			if (parse == null) continue;

			isValid = false;
			for (String str : parse) {
				if ("debug".equals(str)) {
					isValid = true;
				} else if (parse.length > 4) {
					isValid = true;
				}
			}
			if (isValid) {
				try {
					long fileDate = Long.parseLong(parse[0]);
					if (fileDate < currDate) {
						uploadFileList.add(fileName);
						log += "=>Added";
					} else {
						log += "=>Invalid: " + fileDate + " / " + currDate;
					}
				} catch (Exception e) {

				}
			}
			if (DBG) Log.d(TAG, "uploadFile : " + log);
		}
		if (DBG) Log.d(TAG, "uploadFiles : ~ " + currDateString + ", count : " + uploadFileList.size() + "/" + fileList.length);

		for (String fileName : uploadFileList) {
			final String target = fileName;

			Thread thread = new Thread() {
				@Override
				public void run() {
					if (storeFileInBlobStorage(target)) {
						if (DBG) Log.i(TAG, "Upload SUCCEEDED");
					} else {
						if (DBG) Log.e(TAG, "Upload ERROR");
					}
				}
			};
        	thread.start();

		}
    }
}
