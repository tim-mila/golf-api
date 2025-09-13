package com.alimmit.golf.courses.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default GolfCourseApiClient implementation
 */
class DefaultGolfCourseApiClientImpl implements GolfCourseApiClient {

  private enum Path {
    SEARCH("/v1/search?search_query="), FETCH("/v1/courses/");

    private final String value;

    private Path(String value) {
      this.value = value;
    }
  }

  private final String host;
  private final String apiKey;
  private final ObjectMapper objectMapper;

  public DefaultGolfCourseApiClientImpl(String host, String apiKey) {
    this.host = host;
    this.apiKey = apiKey;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public GolfCourse fetch(Long id) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder(URI.create(host + Path.FETCH.value + id)).GET();
    GolfCourseFetchResponse response = makeRequest(builder, GolfCourseFetchResponse.class);
    return response.course();
  }

  @Override
  public List<GolfCourse> search(String terms) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder(URI.create(host + Path.SEARCH.value + terms)).GET();
    GolfCourseSearchResponse response = makeRequest(builder, GolfCourseSearchResponse.class);
    return response.courses();
  }

  private <T> T makeRequest(HttpRequest.Builder requestBuilder, Class<T> type) {

    requestBuilder.header("Authorization", "Key " + apiKey);

    try (HttpClient client = HttpClient.newBuilder().build()) {
      HttpResponse<InputStream> response =
          client.send(requestBuilder.build(), BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
        throw new GolfCourseApiException(response.statusCode());
      }

      return objectMapper.readValue(response.body(), type);

    } catch (IOException e) {
      throw new GolfCourseApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GolfCourseApiException(e);
    }
  }
}
