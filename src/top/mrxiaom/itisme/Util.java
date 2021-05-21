package top.mrxiaom.itisme;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Map;

import net.mamoe.mirai.internal.message.OnlineImage;

import java.awt.image.BufferedImage;
import java.util.Hashtable;
import org.jetbrains.annotations.Nullable;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class Util {
	public static String getTime() {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_MONTH, -5);

		int y = now.get(Calendar.YEAR);
		int m = now.get(Calendar.MONTH) + 1;
		int d = now.get(Calendar.DAY_OF_MONTH);

		String month = "" + m;
		String day = "" + d;

		if (day.length() < 2) {
			day = "0" + day;
		}
		if (month.length() < 2) {
			month = "0" + month;
		}
		return y + "-" + month + "-" + day;
	}
	
	@Nullable
	public static String decode(BufferedImage image) throws Exception {
		if (image == null) {
			return "";
		}
		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
		Result result = new MultiFormatReader().decode(bitmap, hints);
		if(result == null) {
			return "";
		}
		String resultStr = result.getText();
		return resultStr;
	}
	
	public static String getUrlFromImage(net.mamoe.mirai.message.data.Image image) {
		return ((OnlineImage) image).getOriginUrl();
	}
	
	protected static String getThrowableMessage(Throwable e) {
		String result = "" + e.getClass().getName() + " : " + e.getMessage();
		for (int i = 0; i < 10; i++) {
			if (i < e.getStackTrace().length) {
				result = result + "\n" + e.getStackTrace()[i];
			} else
				break;
		}
		return result;
	}

	public static String readFile(File file) {
		String result = "";
		InputStreamReader read = null;

		try {
			read = new InputStreamReader(new FileInputStream(file), "GBK");

			BufferedReader bufferedReader = new BufferedReader(read);

			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				result += lineTxt + "\r\n";
			}
			read.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Throwable writeFile(File file, String content) {
		Throwable t = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(  
	                new FileOutputStream(file), "GBK"));  
	        writer.write(content);
	        writer.close();
		} catch (Throwable e) {
			t = e;
		}
		return t;
	}

	public static String sendPost(String url, Map<String, Object> params) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			System.out.println(postData);
			URL realUrl = new URL(url);
			// �򿪺�URL֮�������

			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
			// 10�볬ʱ
			conn.setReadTimeout(10000);
			// ����Ϊ POST
			conn.setRequestMethod("POST");
			// ���û���
			conn.setUseCaches(false);
			// ����ͨ�õ�����
			conn.setRequestProperty("accept", "application/json");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

			conn.setDoOutput(true);// ����POST�������������������
			conn.setDoInput(true);

			out = new PrintWriter(conn.getOutputStream());// ��ȡURLConnection�����Ӧ�������s
			out.print(postData);// �����������
			out.flush();// flush������Ļ���
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));// ����BufferedReader����������ȡURL����Ӧ
			String line;
			while ((line = in.readLine()) != null) {
				result += "\n" + line;
			}
		} catch (Exception e) {
			System.out.println("����POST��������쳣��" + e);
			e.printStackTrace();
		}
		// ʹ��finally�����ر��������������
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public static String getNewLocation(String url) {
		String result = url;
		try {
			URL serverUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
			conn.setRequestMethod("GET");
			// ��������false��������Զ�redirect��Location�ĵ�ַ
			conn.setInstanceFollowRedirects(false);
			conn.addRequestProperty("Accept-Charset", "UTF-8;");
			conn.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
			conn.connect();
			result = conn.getHeaderField("Location");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String sendGet(String httpurl) {
		return sendGet(httpurl, -1);
	}
	
	public static String sendGet(String httpurl, int type) {
		HttpURLConnection connection = null;
		InputStream is = null;
		BufferedReader br = null;
		String result = "";// ���ؽ���ַ���
		try {
			// ����Զ��url���Ӷ���
			URL url = new URL(httpurl);
			// ͨ��Զ��url���Ӷ����һ�����ӣ�ǿת��httpURLConnection��
			connection = (HttpURLConnection) url.openConnection();
			// �������ӷ�ʽ��get
			connection.setRequestMethod("GET");
			// �������������������ĳ�ʱʱ�䣺15000����
			connection.setConnectTimeout(15000);
			// ���ö�ȡԶ�̷��ص�����ʱ�䣺60000����
			connection.setReadTimeout(60000);
			if(type == 0) {
				connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"); 
				connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63");
				connection.setRequestProperty("referer", "api.vc.bilibili.com");
			}
			
			// ��������
			connection.connect();
			// ͨ��connection���ӣ���ȡ������
			if (connection.getResponseCode() == 200) {
				is = connection.getInputStream();
				// ��װ������is����ָ���ַ���
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				// �������
				StringBuffer sbf = new StringBuffer();
				String temp = null;
				while ((temp = br.readLine()) != null) {
					sbf.append(temp);
					sbf.append("\r\n");
				}
				result = sbf.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// �ر���Դ
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			connection.disconnect();// �ر�Զ������
		}

		return result;
	}

	public static boolean startsWithIgnoreCase(String text, String prefix) {
		if (text.length() >= prefix.length())
			return text.substring(0, prefix.length()).equalsIgnoreCase(prefix);
		else
			return false;
	}

	public static long strToLong(String s, long nullReturnValue) {
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}

	public static int strToInt(String s, int nullReturnValue) {
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}
}
