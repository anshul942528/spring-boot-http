package com.ansh.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.stream.Collectors;

import com.ansh.pojo.ResponseObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * APIs to make rest requests, internally using {@link HttpClient}
 */
@Slf4j
public class HttpApacheUtils {
	final static int connectionTimeout = 1000;
	final static int readTimeout = 10*1000;
	
	private static String getResponse(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream))
				.lines().parallel().collect(Collectors.joining());
	}
	
	private static CloseableHttpClient getClient(int connectionTimeout, int readTimeout) {
		RequestConfig requestConfig =  RequestConfig.custom()
													.setConnectTimeout(connectionTimeout)
													.setSocketTimeout(readTimeout)
													.build();
		return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
	}

	/**
	 * HTTP GET Request
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and response from get request made on url (argument)
	 * @param closeableHttpClient customized Closeable Http Client for the request
	 * @param url Url on which to make Get Request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject getData(CloseableHttpClient closeableHttpClient, String url, Map<String, String> headerMap) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		if(headerMap != null)
			headerMap.forEach((k,v) -> httpGet.addHeader(k, v));
		
		CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
		return new ResponseObject(closeableHttpResponse.getStatusLine().getStatusCode(),getResponse(closeableHttpResponse.getEntity().getContent()));
	}
	
	/**
	 * HTTP GET Request
	 * Returns the {@link ResponseObject} that can be used to extract status code 
	 * and the response from the get request made on url (argument)
	 * @param url Url on which to make Get request. It contains the url variables, So it is the responsibility of client to provide full url 
	 * @param headerMap map of key, value pairs to be set as headers
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject getData(String url,  Map<String, String> headerMap) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}", url, headerMap);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = getData(closeableHttpClient, url, headerMap);
		log.info("Response for the get request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}


	/**
	 * HTTP GET Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and the response from the get request made on url (argument)
	 * @param url Url on which to make Get request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject getData(String url,  Map<String, String> headerMap, int retry) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}, retry: {}", url, headerMap, retry);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while (true) {
			try {
				responseObject = getData(closeableHttpClient, url, headerMap);
				break;
			} catch (SocketTimeoutException ex) {
				if (--retry > 0) {
					log.info("Retrying due to  socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					continue;
				} else {
					log.error("Exception : ", ex);
					log.error("socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					throw ex;
				}
			}
		}
		log.info("Response for the get request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP GET Request
	 * Returns the {@link ResponseObject} that can be used to extract status code 
	 * and the response from the get request made on url (argument)
	 * @param url Url on which to make Get request. It contains the url variables, So it is the responsibility of client to provide full url 
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject getData(String url, Map<String, String> headerMap, int connectionTimeout, int readTimeout) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}, connectionTimeout: {}, readTimeout: {}", url, headerMap, connectionTimeout, readTimeout);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = getData(closeableHttpClient, url, headerMap);
		log.info("Response for the get request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP GET Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract status code
	 * and the response from the get request made on url (argument)
	 * @param url Url on which to make Get request. It contains the url variables, So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject getData(String url, Map<String, String> headerMap, int connectionTimeout,
										 int readTimeout, int retry) throws IOException {
		log.info("Get Request, url: {}, headerMap: {}, connectionTimeout: {}, readTimeout: {}, retry: {}",
														url, headerMap, connectionTimeout, readTimeout, retry);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while (true) {
			try {
				responseObject = getData(closeableHttpClient, url, headerMap);
				break;
			} catch (SocketTimeoutException ex) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					continue;
				} else {
				    log.error("Exception : ", ex);
					log.error("socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					throw ex;
				}
			}
		}
		log.info("Response for the get request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP POST Request
	 * Returns the {@link ResponseObject} that can be used to extract the status code
	 * and the response from the post request made on given url with iven parameters
	 * @param closeableHttpClient customized closeable http client for the request
	 * @param url Url on which to make Post request. It contains the url variables. So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data Data to be posted in json format
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject postData(CloseableHttpClient closeableHttpClient, String url, Map<String, String> headerMap, Object data) throws IOException {
		HttpPost httpPost = new HttpPost(url);
		if(headerMap != null) 
			headerMap.forEach((k,v) -> httpPost.addHeader(k, v));
		
		ObjectMapper objectMapper = new ObjectMapper();
		StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(data), ContentType.APPLICATION_JSON);
		httpPost.setEntity(stringEntity);
		
		CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
		return new ResponseObject(closeableHttpResponse.getStatusLine().getStatusCode(), getResponse(closeableHttpResponse.getEntity().getContent()));
	}
	
	/**
	 * HTTP POST Request
	 * Returns the {@link ResponseObject} that can be used to extract the status code
	 * and the response from the post request made on given url with iven parameters
	 * @param url Url on which to make Post request. It contains the url variables. So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data Data to be posted in json format
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject postData(String url, Map<String, String> headerMap, Map<String , Object> data) throws IOException {
		log.info("Post Request, url: {}, headerMap: {}, data: {}", url, headerMap, data);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = postData(closeableHttpClient, url, headerMap, data);
		log.info("Response for the post request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}


	/**
	 * HTTP POST Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract the status code
	 * and the response from the post request made on given url with iven parameters
	 * @param url Url on which to make Post request. It contains the url variables. So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data Data to be posted in json format
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static ResponseObject postData(String url, Map<String, String> headerMap,
										  Map<String , Object> data, int retry) throws IOException {
		log.info("Post Request, url: {}, headerMap: {}, data: {}, retry:{}", url, headerMap, data, retry);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while (true) {
			try {
				responseObject = postData(closeableHttpClient, url, headerMap, data);
				break;
			} catch (SocketTimeoutException ex) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					continue;
				} else {
					log.error("Exception :", ex);
					log.error("socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					throw ex;
				}
			}
		}
		log.info("Response for the post request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP POST Request
	 * Returns the {@link ResponseObject} that can be used to extract the status code
	 * and the response from the post request made on given url with iven parameters
	 * @param url Url on which to make Post request. It contains the url variables. So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data Data to be posted in json format
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
 	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public  static ResponseObject postData(String url, Map<String, String> headerMap, Map<String, Object> data, int connectionTimeout, int readTimeout) throws IOException {
		log.info("Post Request, url: {}, headerMap: {}, data: {}, connectionTimeout: {}, readTimeout: {}",
												url, headerMap, data, connectionTimeout, readTimeout);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = postData(closeableHttpClient, url, headerMap, data);
		log.info("Response for the post request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}

	/**
	 * HTTP POST Request with retry mechanism
	 * Returns the {@link ResponseObject} that can be used to extract the status code
	 * and the response from the post request made on given url with iven parameters
	 * @param url Url on which to make Post request. It contains the url variables. So it is the responsibility of client to provide full url
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data Data to be posted in json format
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @param retry retry count for the request
	 * @return ResponseObject contains status code and response from the request
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public  static ResponseObject postData(String url, Map<String, String> headerMap,
										   Map<String, Object> data, int connectionTimeout,
										   int readTimeout, int retry) throws IOException {
		log.info("Post Request, url: {}, headerMap: {}, data: {}, connectionTimeout: {}, readTimeout: {}, retry:{}",
											url, headerMap, data, connectionTimeout, readTimeout, retry);
		CloseableHttpClient closeableHttpClient = getClient(connectionTimeout, readTimeout);
		ResponseObject responseObject = null;
		while (true) {
			try {
				responseObject = postData(closeableHttpClient, url, headerMap, data);
				break;
			} catch (SocketTimeoutException ex) {
				if (--retry > 0) {
					log.info("Retrying due to socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					continue;
				} else {
				    log.error("Exception : ", ex);
					log.error("socket timeout exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, ex.getMessage());
					throw ex;
				}
			}
		}
		log.info("Response for the post request made on the url :{}, response :{}", url, responseObject);
		return responseObject;
	}
}