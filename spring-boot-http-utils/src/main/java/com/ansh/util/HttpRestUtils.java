package com.ansh.util;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;


/**
 *
 * APIs to make rest requests, internally using {@link RestTemplate}
 */
@Slf4j
public class HttpRestUtils {
	
	final static Duration connectTimeout = Duration.ofMillis(10*1000L);
	final static Duration readTimeout = Duration.ofMillis(60*1000L);
	
	private static RestTemplate getRestTemplate(Duration connectTimeout, Duration readTimeout) {
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		return restTemplateBuilder.setConnectTimeout(connectTimeout)
									.setReadTimeout(readTimeout)
									.build();
	}
	
	/**
	 * HTTP POST Request
	 * Returns the response in U format, or null in case no exception and the data is not available
	 * This method will not have retry mechanism
	 * @param restTemplate customized rest template for the request
	 * @param url URL on which to make the request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data data of Type T
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @return response in U format, or null
	 * @throws RestClientException
	 */
	public static <T, U> U postData(RestTemplate restTemplate, String url, Map<String, String> headerMap,
									T data, Map<String, Object> uriVariables,
									Class<U> responseType) throws RestClientException {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if(headerMap != null)
			headerMap.forEach((k,v) -> httpHeaders.set(k, v));
		HttpEntity<T> httpEntity = new HttpEntity<T>(data, httpHeaders);
		
		ResponseEntity<U> responseEntity = (uriVariables == null ) ? restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType)
																	: restTemplate.exchange(url, HttpMethod.POST, httpEntity, responseType, uriVariables);
		if(responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null)
			return responseEntity.getBody();
		return null;
	}
	
	/**
	 * HTTP POST Request
	 * Returns the response in U format, or null in case no exception and the data is not available
	 * @param url URL on which to make the request
	 * @param data data of Type T
	 * @param responseType class type of response expected
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	public static <T, U> U postData(String url, T data, Class<U> responseType) throws RestClientException {
		log.info("Post request, url: {}, data: {}, connectionTimeout: {}, readTimeout: {}",
													url, data, connectTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(connectTimeout, readTimeout);
		U u =  postData(restTemplate, url, null, data, null, responseType);
		log.info("Response for the post request made on url: {}, request data: {}, response: {}", url, data, u);
		return u;
	}

	/**
	 * HTTP POST Request with retry mechanism
	 * Returns the response in U format, or null in case no exception and the data is not available
	 * @param url URL on which to make the request
	 * @param data data of type T
	 * @param responseType class of response expected
	 * @param retry response in U format or null
	 * @param <T> type of data argument
	 * @param <U> type of return value
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	public static <T,U> U postData(String url, T data, Class<U> responseType, int retry) throws RestClientException {
		log.info("Post Request, url: {}, data: {}, connectionTimeout: {}, readTimeout: {}, retry :{}",
															url, data, connectTimeout, readTimeout, retry);
		RestTemplate restTemplate = getRestTemplate(connectTimeout, readTimeout);
		U u = null;
		while( true ) {
			try {
				u = postData(restTemplate, url, null, data, null, responseType);
				break;
			} catch (ResourceAccessException e) {
				if (--retry > 0) {
					log.info("Retrying due to resource access exception occurred for the request. url: {}, data: {}, retry count: {}, " +
							"error: {}", url, data, retry, e.getMessage());
					continue;
				} else {
				    log.error("Exception : ", e);
					log.error("Resource access exception occurred and number of retries are over for the request. url: {}, data: {}" +
							"retry count: {}, error: {}", url, data, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for the post request made on url: {}, request data: {}, response: {}", url, data, u);
		return u;
	}

	/**
	 * HTTP POST Request
	 * Returns the response in U format, or null in case no exception and the data is not available
	 * @param url URL on which to make the request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data data of Type T
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	public static <T, U> U postData(String url, Map<String, String> headerMap, T data,
									Map<String, Object> uriVariables, Class<U> responseType, int connectionTimeout,
									int readTimeout) throws RestClientException {
		log.info("Post request, url: {}, data: {}, connectionTimeout: {}, readTimeout: {}",
													url, data, connectionTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(Duration.ofMillis(connectionTimeout), Duration.ofMillis(readTimeout));
		U u =  postData(restTemplate, url, headerMap, data, uriVariables, responseType);
		log.info("Response for the post request made on url: {}, request data: {}, response: {}", url, data, u);
		return u;
	}


	/**
	 * HTTP POST Request with retry mechanism
	 * Returns the response in U format, or null in case no exception and the data is not available
	 * @param url URL on which to make the request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data data of type T
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class of response expected
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @param retry response in U format or null
	 * @param <T> type of data argument
	 * @param <U> type of return value
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	public static <T,U> U postData(String url, Map<String, String> headerMap, T data,
								   Map<String, Object> uriVariables, Class<U> responseType,
								   int connectionTimeout, int readTimeout, int retry) throws RestClientException {
		log.info("Post request, url: {}, data: {}, connectionTimeout: {}, readTimeout: {}, retryCount: {}",
				url, data, connectionTimeout , readTimeout, retry);
		RestTemplate restTemplate = getRestTemplate(Duration.ofMillis(connectionTimeout), Duration.ofMillis(readTimeout));
		U u = null;
		while (true) {
			try {
				u = postData(restTemplate, url, headerMap, data, uriVariables, responseType);
				break;
			} catch (ResourceAccessException e) {
				if (--retry > 0) {
					log.info("Retrying due to resource access exception occurred for the request. url: {}, data: {}, retrycount: {}, error: {}",
							url, data, retry, e.getMessage());
					continue;
				} else {
				    log.error("Exception : ", e);
					log.info("Resource access exception occurred for the request. url: {}, data: {}, retrycount: {}, error: {}",
							url, data, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for the post request made on url: {}, request data: {}, response: {}", url, data, u);
		return u;
	}

	
	/**
	 * HTTP GET Request
	 * Returns the response in U format, or null in case no exception and data is not available
	 * @param restTemplate  customized rest template to make requests
	 * @param url URL on which to make get request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <U> U getData(RestTemplate restTemplate, String url, Map<String, String> headerMap,
								Map<String, Object> uriVariables, Class<U> responseType) throws RestClientException {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		if(headerMap != null)
			headerMap.forEach((k,v) -> httpHeaders.set(k, v));
		HttpEntity httpEntity = new HttpEntity(httpHeaders);
		
		ResponseEntity<U> responseEntity = (uriVariables == null) ? restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType)
																	: restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType, uriVariables);
		if(responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null)
			return responseEntity.getBody();
		return null;
	}
	
	/**
	 * HTTP GET Request
	 * Returns the response in U format, or null in case no exception and data is not available
	 * @param url URL on which get request to be made
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @return response in U format, or null
	 * @throws RestClientException
	 */
	public static <U> U getData(String url, Map<String, String> headerMap, Map<String, Object> uriVariables,
																Class<U> responseType) throws RestClientException {
		log.info("Get request, url: {}, connectionTimeout: {}, readTimeout: {}", url, connectTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(connectTimeout, readTimeout);
		U u = getData(restTemplate, url, headerMap, uriVariables, responseType);
		log.info("Response for the get request made on url: {}, response: {}", url, u);
		return u;
	}

	/**
	 * HTTP GET Request
	 * Returns the response in U format, or null in case no exception and data is not available
	 * @param url URL on which get request to be made
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @param retry retry count for the request
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	public static <U> U getData(String url, Map<String, String> headerMap, Map<String, Object> uriVariables,
								Class<U> responseType, int retry) throws RestClientException {
		log.info("Get request, url: {}, connectionTimeout: {}, readTimeout: {}, retry : {}",
																url, connectTimeout, readTimeout, retry);
		RestTemplate restTemplate = getRestTemplate(connectTimeout, readTimeout);
		U u = null;
		while (true) {
			try {
				u = getData(restTemplate, url, headerMap, uriVariables, responseType);
				break;
			} catch (ResourceAccessException e) {
				if (--retry > 0) {
					log.info("Retying due to resource access exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, e.getMessage());
					continue;
				} else {
                    log.error("Exception : ", e);
					log.error("Resource access exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for the get request made on url: {}, response: {}", url, u);
		return u;
	}

	/**
	 * HTTP GET Request
	 * Returns the response in U format,or null in case no exception and data is not available.
	 * @param url URL in which get request to be made
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @return response in U format or null
	 * @throws RestClientException
	 */
	public static <U> U getData(String url, Map<String, String> headerMap, Map<String, Object> uriVariables,
								Class<U> responseType, int connectionTimeout, int readTimeout) throws RestClientException {
		log.info("Get request, url: {}, connectionTimeout: {}, readTimeout: {}", url, connectionTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(Duration.ofMillis(connectionTimeout), Duration.ofMillis(readTimeout));
		U u = getData(restTemplate, url, headerMap, uriVariables, responseType);
		log.info("Response for the get request made on url: {}, response: {}", url, u);
		return u;
	}

	/**
	 * HTTP GET Request
	 * Returns the response in U format or null in case no exception and data is not available.
	 * @param url URL in which get request to be made
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param responseType class type of response expected
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @param  retry retry count for the request
	 * @return response in U format, or null
	 * @throws RestClientException
	 */
	public static <U> U getData(String url, Map<String, String> headerMap, Map<String, Object> uriVariables,
								Class<U> responseType, int connectionTimeout,
								int readTimeout, int retry) throws RestClientException {
		log.info("Get request, url: {}, connectionTimeout: {}, readTimeout: {}", url, connectionTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(Duration.ofMillis(connectionTimeout), Duration.ofMillis(readTimeout));
		U u = null;
		while (true) {
			try {
				u = getData(restTemplate, url, headerMap, uriVariables, responseType);
				break;
			} catch (ResourceAccessException e) {
				if (--retry > 0) {
					log.info("Retrying due to resource access exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, e.getMessage());
					continue;
				} else {
					log.error("Resource access exception occurred for the request. url: {}, retry count: {}, error: {}",
							url, retry, e.getMessage());
					throw e;
				}
			}
		}
		log.info("Response for the get request made on url: {}, response: {}", url, u);
		return u;
	}

	/**
	 * HTTP PUT Request
	 * @param restTemplate customized rest template for the request
	 * @param url URL on which to make put request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param map data to send as load for put call
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @throws RestClientException
	 */
	public static <T> void putData(RestTemplate restTemplate, String url, Map<String, String> headerMap,
								   				T map, Map<String, Object> uriVariables) throws RestClientException {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if(headerMap != null)
			headerMap.forEach((k,v) -> httpHeaders.set(k, v));
		HttpEntity<T> httpEntity = new HttpEntity<T>(map, httpHeaders);
			
		if(uriVariables == null) 
			restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
		else
			restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class, uriVariables);
	}
	
	/**
	 * HTTP PUT Request
	 * @param url URL on which to make put request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data data to send as load for put call
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @throws RestClientException
	 */
	public static <T> void putData(String url, Map<String, String> headerMap, T data,
								   					Map<String, Object> uriVariables) throws RestClientException {
		log.info("Put request, url: {}, data: {}, connectionTimeout: {}, readTimeout: {}", url, data, connectTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(connectTimeout, readTimeout);
		putData(restTemplate, url, headerMap, data, uriVariables);
	}

	/**
	 * HTTP PUT Request
	 * @param url URL on which to make put request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data data to send as load for put call
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param connectionTimeout connection timeout in millis
	 * @param readTimeout read timeout in millis
	 * @throws RestClientException
	 */
	public static <T> void putData(String url, Map<String, String> headerMap, T data,
								   					Map<String, Object> uriVariables, int connectionTimeout,
								   					int readTimeout) throws RestClientException {
		log.info("Put request, url: {}, data: {}, connectionTimeout: {}, readTimeout: {}", url, data, connectionTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(Duration.ofMillis(connectionTimeout), Duration.ofMillis(readTimeout));
		putData(restTemplate, url, headerMap, data, uriVariables);
	}
	
	
	/**
	 * HTTP DELETE Request
	 * @param restTemplate customized rest template for the request
	 * @param url URL on which to make delete request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data Optional data to be sent if required
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @throws RestClientException
	 */
	public static <T> void deleteData(RestTemplate restTemplate, String url, Map<String, String> headerMap,
									  			T data, Map<String, Object> uriVariables) throws RestClientException {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		if(headerMap != null)
			headerMap.forEach((k,v) -> httpHeaders.set(k, v));
		HttpEntity<T> httpEntity = new HttpEntity<T>(data, httpHeaders);
		if(uriVariables == null) 
			restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Void.class);
		else
			restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Void.class, uriVariables);
	}
	
	/**
	 * HTTP DELETE Request
	 * @param url URL for delete request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param data (Optional) data to be sent if required
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @throws RestClientException
	 */
	public static <T> void deleteData(String url, Map<String, String> headerMap, T data,
									  			Map<String, Object> uriVariables ) throws RestClientException {
		log.info("Delete request, url: {}, connectionTimeout: {}, readTimeout: {}", url, connectTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(connectTimeout, readTimeout);
		deleteData(restTemplate, url, headerMap, data, uriVariables);
	}
	
	/**
	 * HTTP DELETE Request
	 * @param url URL for delete request
	 * @param headerMap map of key, value pairs to be set as headers
	 * @param map (Optional) data to be sent if required
	 * @param uriVariables key, value pairs to be sent for url placeholders
	 * @param connectionTimeout connection timeout in milli second
	 * @param readTimeout read timeout in millis second
	 * @throws RestClientException
	 */
	public static <T> void deleteData(String url, Map<String, String> headerMap, T map,
									  						Map<String, Object> uriVariables, int connectionTimeout,
									  						int readTimeout) throws RestClientException {
		log.info("Delete request, url: {}, connectionTimeout: {}, readTimeout: {}", url, connectionTimeout, readTimeout);
		RestTemplate restTemplate = getRestTemplate(Duration.ofMillis(connectionTimeout), Duration.ofMillis(readTimeout));
		deleteData(restTemplate, url, headerMap, map, uriVariables);
	}
}
