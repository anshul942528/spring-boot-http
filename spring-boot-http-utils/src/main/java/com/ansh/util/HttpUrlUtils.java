package com.ansh.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;
import com.ansh.pojo.ResponseObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

/**
 * APIs to make rest requests, internally using {@link HttpURLConnection}
 */
@Slf4j
public class HttpUrlUtils {
	final static int readTimeout = 10*1000;
	final static int connectionTimeout = 1000;
	
	private static String getResponse(InputStream inputStream) {
		return  new BufferedReader(new InputStreamReader(inputStream))
				.lines().parallel().collect(Collectors.joining());
	}
	
	private static HttpURLConnection getHttpURLConnection(String url, int connectionTimeout, int readTimeout) throws IOException {
		URL url2  = new URL(url);
		HttpURLConnection httpURLConnection = (HttpURLConnection)url2.openConnection();
		httpURLConnection.setConnectTimeout(connectionTimeout);
		httpURLConnection.setReadTimeout(readTimeout);
		return httpURLConnection;
	}
	
	
	/**
	 * HTTP POST Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Post request made on given url with given data
	 * @param httpURLConnection customized Http Url Connection for the request
	 * @param data data to be posted in json format
	 * @param headerMap map of key, value pairs to be set as headers
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject postData(HttpURLConnection httpURLConnection, Object data, Map<String, String> headerMap) throws IOException {
		httpURLConnection.setDoInput(true);
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		if(headerMap != null) 
			headerMap.forEach((k,v) -> httpURLConnection.setRequestProperty(k, v));
		httpURLConnection.connect();
		ObjectMapper objectMapper = new ObjectMapper();
		httpURLConnection.getOutputStream().write(objectMapper.writeValueAsBytes(data));
		
		ResponseObject responseObject = null;
		if( httpURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
			log.info("Post Request with response coming from input stream");
			responseObject = new ResponseObject(httpURLConnection.getResponseCode(), getResponse(httpURLConnection.getInputStream()));
		} else {
			log.info("Post Request with response coming from error stream");
			responseObject = new ResponseObject(httpURLConnection.getResponseCode(), getResponse(httpURLConnection.getErrorStream()));
		}
		httpURLConnection.disconnect();
		return responseObject;
	}
	
	/**
	 * HTTP POST Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Post request made on given url with given data
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param data Data to be posted in json format
	 * @param headerMap map of key, value pairs to be set as headers
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject postData(String url, Object data, Map<String, String> headerMap) throws IOException {
		log.info("Post Request, url: {}, data: {}, headerMap: {}", url, data, headerMap);
		HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
		ResponseObject responseObject = postData(httpURLConnection, data, headerMap);
		log.info("Response for post request made on the url :{}, response: {}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP POST Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Post request made on given url with given data
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param data Data to be posted in json format
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject postData(String url, Object data, Map<String, String> headerMap, int retry) throws IOException {
		log.info("Post Request, url: {}, data: {}, headerMap: {}", url, data, headerMap);
		ResponseObject responseObject = null;
		while(true) {
			try {
				HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
				responseObject = postData(httpURLConnection, data, headerMap);
				break;
			} catch (SocketTimeoutException e) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					continue;
				} else {
				    log.error("Exception : ", e);
					log.error("socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for post request made on the url :{}, response: {}", url, responseObject);
		return  responseObject;
	}
	
	/**
	 * HTTP POST Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Post request made on given url with given data
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param data Data to be posted in json format
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject postData(String url, Object data, Map<String, String> headerMap, int connectionTimeout, int readTimeout) throws IOException {
		log.info("Post Request, url: {}, data: {}, headerMap: {}, connectionTimeout: {}, readTimeout: {}", url, data, headerMap, connectionTimeout, readTimeout);
		HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
		ResponseObject responseObject = postData(httpURLConnection, data, headerMap);
		log.info("Response for post request made on the url :{}, response: {}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP POST Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Post request made on given url with given data
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param data Data to be posted in json format
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject postData(String url, Object data, Map<String, String> headerMap,
										  int connectionTimeout, int readTimeout, int retry) throws IOException {
		log.info("Post Request, url: {}, data: {}, headerMap: {}, connectionTimeout: {}, readTimeout: {}", url, data, headerMap, connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while(true) {
			try {
				HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
				responseObject = postData(httpURLConnection, data, headerMap);
				break;
			} catch (SocketTimeoutException e) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					continue;
				} else {
				    log.error("Exception : ", e);
					log.error("socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for post request made on the url :{}, response: {}", url, responseObject);
		return responseObject;
	}


	/**
	 * HTTP GET Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Get request made on given url
	 * Note*** Do not use this method if you do not know what you want
	 * @param httpURLConnection customized Http URL Connection for the request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject getData(HttpURLConnection httpURLConnection, Map<String, String> headerMap) throws IOException {
		httpURLConnection.setDoInput(true);
		httpURLConnection.setRequestMethod("GET");
		if(headerMap != null)
			headerMap.forEach((k,v) -> httpURLConnection.setRequestProperty(k, v));
		httpURLConnection.connect();

		ResponseObject responseObject = null;
		if( httpURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
			log.info("Get request with response coming from inpout stream");
			responseObject = new ResponseObject(httpURLConnection.getResponseCode(), getResponse(httpURLConnection.getInputStream()));
		} else {
			log.info("Get request with response coming from error stream");
			responseObject = new ResponseObject(httpURLConnection.getResponseCode(), getResponse(httpURLConnection.getErrorStream()));
		}
		httpURLConnection.disconnect();
		return responseObject;
	}
	
	
	/**
	 * HTTP GET Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Get request made on given url
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject getData(String url, Map<String, String> headerMap) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}", url, headerMap);
		HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
		ResponseObject response =  getData(httpURLConnection, headerMap);
		log.info("Response for get request made on the url : {}, response :{}", url, response);
		return response;
	}

	/**
	 * HTTP GET Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Get request made on given url
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject getData(String url, Map<String, String> headerMap, int retry) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}", url, headerMap);
		HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while(true) {
			try {
				responseObject = getData(httpURLConnection, headerMap);
				break;
			} catch (SocketTimeoutException e) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					continue;
				} else {
					log.error("Exception : ", e);
					log.error("socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for get request made on the url : {}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP GET Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Get request made on given url
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject getData(String url, Map<String, String> headerMap, int connectionTimeout, int readTimeout) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}, connectionTimeout: {}, readTimeout: {}", url, headerMap, connectionTimeout, readTimeout);
		HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
		ResponseObject responseObject = getData(httpURLConnection, headerMap);
		log.info("Response for get request made on the url : {}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP GET Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from Get request made on given url
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public static ResponseObject getData(String url, Map<String, String> headerMap, int connectionTimeout,
										 int readTimeout, int retry) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}, connectionTimeout: {}, readTimeout: {}", url, headerMap, connectionTimeout, readTimeout);
		HttpURLConnection httpURLConnection = getHttpURLConnection(url, connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while(true) {
			try {
				responseObject = getData(httpURLConnection, headerMap);
				break;
			} catch (SocketTimeoutException e) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					continue;
				} else {
					log.error("Exception : ", e);
					log.error("socket timeout exception occurred for the request. url: {}, retrycount: {}, error: {}",
							url, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for get request made on the url : {}, response :{}", url, responseObject);
		return responseObject;
	}
}
