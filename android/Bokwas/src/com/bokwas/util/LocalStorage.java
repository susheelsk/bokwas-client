package com.bokwas.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

public class LocalStorage {

	private static String TAG = "LocalStorage";

	public static void storeUserData(Context context,
			UserDataStore userDataStore) {
		synchronized (LocalStorage.class) {
			Gson gson = new Gson();
			String jsonData = gson.toJson(userDataStore);
			try {
				byte[] encryptedData = encryptMsg(jsonData,
						generateKey(context));
				String encryptedString = new String(encryptedData);
				writeFileToInternalStorage(context, "userStore",
						encryptedString);
			} catch (Exception e) {
				Log.d(TAG, "Writing UserData failed");
				e.printStackTrace();
			}
		}
	}

	public static UserDataStore getUserData(Context context) {
		String jsonData = readFileFromInternalStorage(context, "userStore");
		Gson gson = new Gson();
		try {
			String decryptedString = decryptMsg(jsonData.getBytes(),
					generateKey(context));
			UserDataStore userDataStore = gson.fromJson(decryptedString,
					UserDataStore.class);
			return userDataStore;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static <T> void storeObj(Context context, T obj) {
		synchronized (LocalStorage.class) {
			Gson gson = new Gson();
			String jsonData = gson.toJson(obj);
			try {
				byte[] encryptedData = encryptMsg(jsonData,
						generateKey(context));
				String encryptedString = new String(encryptedData);
				writeFileToInternalStorage(context, "userStore",
						encryptedString);
			} catch (Exception e) {
				Log.d(TAG, "Writing UserData failed");
				e.printStackTrace();
			}
		}
	}

	public static <T> T readObj(Context context, Class<T> cls) {
		String jsonData = readFileFromInternalStorage(context, "userStore");
		Gson gson = new Gson();
		try {
			String decryptedString = decryptMsg(jsonData.getBytes(),
					generateKey(context));
			T obj = gson.fromJson(decryptedString, cls);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private static void writeFileToInternalStorage(Context context,
			String fileName, String data) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					context.openFileOutput(fileName, Context.MODE_PRIVATE)));
			writer.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String readFileFromInternalStorage(Context context,
			String fileName) {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(
					context.openFileInput(fileName)));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = input.readLine()) != null) {
				buffer.append(line);
			}
			String outputData = buffer.toString();
			return outputData;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private static SecretKey generateKey(Context context)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String password;
		if (AppData.getAppData().isDevMode() == false) {
			password = getAppKeyHash(context);
		} else {
			password = "worldNeedsBokwas";
		}
		SecretKeySpec secret = new SecretKeySpec(password.getBytes(), "AES");
		return secret;
	}

	private static String getAppKeyHash(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;

				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.d("Hash key", something);
			}
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			Log.e("name not found", e1.toString());
		}

		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			Log.e("no such an algorithm", e.toString());
		} catch (Exception e) {
			Log.e("exception", e.toString());
		}
		return null;
	}

	private static byte[] encryptMsg(String message, SecretKey secret)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidParameterSpecException,
			IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException {
		/* Encrypt the message. */
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secret);
		byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
		return cipherText;
	}

	private static String decryptMsg(byte[] cipherText, SecretKey secret)
			throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidParameterSpecException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException, UnsupportedEncodingException {

		/*
		 * Decrypt the message, given derived encContentValues and
		 * initialization vector.
		 */
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secret);
		String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
		return decryptString;
	}

}
