package com.rokin.celltracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.rokin.celltracker.domain.Circle;
import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.Client.Role;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.Message;
import com.rokin.celltracker.domain.Message.MessageScope;
import com.rokin.celltracker.domain.Message.MessageType;
import com.rokin.celltracker.dto.CircleInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CircleIntegrationTest {

  private static final String PASSWORD = "password";
  @Value("${security.jwt.client-id}")
  private String clientId;
  @Value("${security.jwt.client-secret}")
  private String clientSecret;
  @Value("${security.jwt.resource-ids}")
  private String resourceIds;

  @Resource
  private TestRestTemplate testRestTemplate;

  @Test
  public void addCircle() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("add.circle@gmail.com", "222222200000",
        PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());

    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(circle.getId()).isNotBlank();
    assertThat(circle.getActive()).isTrue();
    assertThat(circle.getName()).isEqualTo("circle-1");
    assertThat(circle.getOwner().getEmail()).isEqualTo("add.circle@gmail.com");
    assertThat(circle.getOwner().getCellNo()).isEqualTo("222222200000");
  }

  @Test
  public void updateCircle() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("update.circle@gmail.com", "222222222222",
        PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    circle.setName("circle-1_updated");
    request = new HttpEntity<>(circle, cwh.getHeaders());

    ResponseEntity<Circle> updateResponseEntity = testRestTemplate
        .exchange("/api/circles/" + circle.getId(), HttpMethod.PUT, request, Circle.class);
    Circle updatesCircle = responseEntity.getBody();

    assertThat(updateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatesCircle.getActive()).isEqualTo(circle.getActive());
    assertThat(updatesCircle.getCode()).isEqualTo(circle.getCode());
    assertThat(updatesCircle.getId()).isEqualTo(circle.getId());
    assertThat(updatesCircle.getName()).isEqualTo(circle.getName());
  }

  @Test
  public void deleteCircle() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("delete.circle@gmail.com", "222222222200",
        PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    request = new HttpEntity<>(circle, cwh.getHeaders());

    ResponseEntity<Boolean> updateResponseEntity = testRestTemplate
        .exchange("/api/circles/" + circle.getId(), HttpMethod.DELETE, request, Boolean.class);

    assertThat(updateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponseEntity.getBody()).isTrue();
  }

  @Test
  public void getCircles() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("get.circles@gmail.com", "222222222211",
        PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    testRestTemplate.exchange(uri, HttpMethod.POST, request, Circle.class);
    request = new HttpEntity<>(cwh.getHeaders());

    ResponseEntity<List<CircleInfo>> listResponseEntity = testRestTemplate.exchange("/api/circles/",
        HttpMethod.GET, request, new ParameterizedTypeReference<List<CircleInfo>>() {
        });
    List<CircleInfo> circleInfos = listResponseEntity.getBody();

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(circleInfos.size()).isGreaterThan(0);
  }

  @Test
  public void getCircleMembers() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("get.circle.members@gmail.com",
        "322222221111", PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    request = new HttpEntity<>(cwh.getHeaders());

    ResponseEntity<List<Client>> listResponseEntity = testRestTemplate.exchange(
        "/api/circles/" + circle.getId() + "/members", HttpMethod.GET, request,
        new ParameterizedTypeReference<List<Client>>() {
        });

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void getCircleMemberLocationHistory() {
    ClientWithHeaders cwh = createClientAndAuthHeaders(
        "get.circle.member.location.history@gmail.com", "442222001111", PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    request = new HttpEntity<>(cwh.getHeaders());
    uri = UriComponentsBuilder.fromPath("/api/circles/").path(circle.getId()).path("/members/")
        .path(String.valueOf(cwh.getClient().getId())).path("/locationhistory")
        .queryParam("fromtime", 0).queryParam("totime", System.currentTimeMillis()).toUriString();

    ResponseEntity<List<ClientDeviceInfo>> listResponseEntity = testRestTemplate.exchange(uri,
        HttpMethod.GET, request, new ParameterizedTypeReference<List<ClientDeviceInfo>>() {
        });

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponseEntity.getBody().size()).isGreaterThan(0);
  }

  @Test
  public void joinCircle() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("join.circle.owner@gmail.com",
        "440022001111", PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    cwh = createClientAndAuthHeaders("join.circle.member@gmail.com", "440022771111", PASSWORD);
    request = new HttpEntity<>(cwh.getHeaders());

    uri = UriComponentsBuilder.fromPath("/api/circles/").path(circle.getCode()).path("/join")
        .toUriString();
    ResponseEntity<Circle> listResponseEntity = testRestTemplate.exchange(uri, HttpMethod.GET,
        request, Circle.class);
    Circle joinedCircle = responseEntity.getBody();

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(joinedCircle.getActive()).isEqualTo(circle.getActive());
    assertThat(joinedCircle.getId()).isEqualTo(circle.getId());
    assertThat(joinedCircle.getName()).isEqualTo(circle.getName());
    assertThat(joinedCircle.getOwner().getCellNo()).isEqualTo(circle.getOwner().getCellNo());
  }

  @Test
  public void leaveCircleByOwner() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("leave.circle.owner@gmail.com",
        "440077001111", PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    request = new HttpEntity<>(cwh.getHeaders());

    uri = UriComponentsBuilder.fromPath("/api/circles/").path(circle.getId()).path("/leave")
        .toUriString();
    ResponseEntity<Boolean> listResponseEntity = testRestTemplate.exchange(uri, HttpMethod.GET,
        request, Boolean.class);

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponseEntity.getBody()).isTrue();
  }

  @Test
  public void refreshCode() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("refresh.code@gmail.com", "442222991111",
        PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    request = new HttpEntity<>(cwh.getHeaders());
    uri = UriComponentsBuilder.fromPath("/api/circles/").path(circle.getId()).path("/newcode/")
        .toUriString();

    ResponseEntity<String> listResponseEntity = testRestTemplate.exchange(uri, HttpMethod.GET,
        request, String.class);

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponseEntity.getBody()).isNotBlank();
  }

  @Test
  public void pushReachHomeMessageToCircle() {
    ClientWithHeaders cwh = createClientAndAuthHeaders("push.message@gmail.com", "442222841111",
        PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(cwh.getHeaders());
    String uri = UriComponentsBuilder.fromPath("/api/circles").queryParam("circlename", "circle-1")
        .toUriString();
    ResponseEntity<Circle> responseEntity = testRestTemplate.exchange(uri, HttpMethod.POST, request,
        Circle.class);
    Circle circle = responseEntity.getBody();
    Message message = Message.builder().deviceInfo(cwh.getClient().getLatestDeviceInfo())
        .fromClientId(cwh.getClient().getId()).isSent(false).meassgeType(MessageType.REACHEDHOME)
        .messageScope(MessageScope.CIRCLE).toCircleId(circle.getId()).build();
    request = new HttpEntity<>(message, cwh.getHeaders());
    uri = UriComponentsBuilder.fromPath("/api/circles/").path("/push").toUriString();

    ResponseEntity<String> listResponseEntity = testRestTemplate.exchange(uri, HttpMethod.POST,
        request, String.class);

    assertThat(listResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Data
  @RequiredArgsConstructor
  static class ClientWithHeaders {
    private final HttpHeaders headers;
    private final Client client;
  }

  @SuppressWarnings("rawtypes")
  private ClientWithHeaders createClientAndAuthHeaders(String email, String cellNo,
      String password) {
    Client client = buildClient(email, cellNo, password, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    Client createdClient = testRestTemplate.postForObject("/api/clients", request, Client.class);

    ResponseEntity<Map> responseEntity = login(email, password);
    Map jwtMap = responseEntity.getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    return new ClientWithHeaders(headers, createdClient);
  }

  @SuppressWarnings("rawtypes")
  private ResponseEntity<Map> login(String username, String password) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.set("username", username);
    body.set(PASSWORD, password);
    body.set("grant_type", PASSWORD);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<?> request = new HttpEntity<>(body, headers);
    return testRestTemplate.withBasicAuth(clientId, clientSecret).postForEntity("/oauth/token",
        request, Map.class);
  }

  private HttpEntity<?> buildClientRequest(Client client, HttpHeaders headers, String imageName) {
    MultipartBodyBuilder multipartBuilder = new MultipartBodyBuilder();
    multipartBuilder.part("email", client.getEmail(), MediaType.TEXT_PLAIN);
    multipartBuilder.part("cellNo", client.getCellNo(), MediaType.TEXT_PLAIN);
    multipartBuilder.part(PASSWORD, client.getPassword(), MediaType.TEXT_PLAIN);
    multipartBuilder.part("deviceId", client.getDeviceId(), MediaType.TEXT_PLAIN);
    multipartBuilder.part("pushNotificationId", client.getPushNotificationId(),
        MediaType.TEXT_PLAIN);
    client.getRoles().stream()
        .forEach(r -> multipartBuilder.part("roles", r.toString(), MediaType.TEXT_PLAIN));
    multipartBuilder.part("enabled", String.valueOf(client.isEnabled()), MediaType.TEXT_PLAIN);
    ClientDeviceInfo cdi = client.getLatestDeviceInfo();
    if (cdi != null) {
      multipartBuilder.part("latestDeviceInfo.lat", String.valueOf(cdi.getLat()),
          MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.lon", String.valueOf(cdi.getLon()),
          MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.altitude", String.valueOf(cdi.getAltitude()),
          MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.accuracy", String.valueOf(cdi.getAccuracy()),
          MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.speed", String.valueOf(cdi.getSpeed()),
          MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.bearing", String.valueOf(cdi.getBearing()),
          MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.provider", cdi.getProvider(), MediaType.TEXT_PLAIN);
      multipartBuilder.part("latestDeviceInfo.battery", String.valueOf(cdi.getBattery()),
          MediaType.TEXT_PLAIN);
    }
    multipartBuilder.part("file",
        new ClassPathResource("static" + File.separator + "image" + File.separator + imageName),
        MediaType.IMAGE_JPEG);
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    return new HttpEntity<>(multipartBuilder.build(), headers);
  }

  private Client buildClient(String email, String cellNo, String password,
      Boolean isDeviceInfoAdded) {
    List<Role> roles = new ArrayList<>(Arrays.asList(Role.CLIENT));
    ClientDeviceInfo clientDeviceInfo = null;
    if (isDeviceInfoAdded) {
      clientDeviceInfo = ClientDeviceInfo.builder().lat(45.0).lon(90.0).altitude(1.0).speed(1.0)
          .accuracy(1.0).bearing(1.0).battery(1).provider("provider").build();
    }
    Client client = Client.builder().email(email).cellNo(cellNo).deviceId("deviceId")
        .pushNotificationId("notId").roles(roles).enabled(true).password(password)
        .latestDeviceInfo(clientDeviceInfo).build();
    if (isDeviceInfoAdded) {
      clientDeviceInfo.setClient(client);
    }
    return client;
  }

}
