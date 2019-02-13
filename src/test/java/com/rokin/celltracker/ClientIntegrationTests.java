package com.rokin.celltracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.Client.Role;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.ClientFavoritePlace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ClientIntegrationTests {

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
  public void addClient() {
    Client client = buildClient("add.client@gmail.com", "8801674798298", PASSWORD, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");

    ResponseEntity<Client> responseEntity = testRestTemplate.postForEntity("/api/clients", request,
        Client.class);
    Client responseClient = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseClient.getEmail()).isEqualTo(client.getEmail());
    assertThat(responseClient.getCellNo()).isEqualTo(client.getCellNo());
    assertThat(responseClient.getDeviceId()).isEqualTo(client.getDeviceId());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void addClientWithDuplicateEmail_returnsBadRequest() {
    Client client = buildClient("add.duplicate.email@gmail.com", "8801674798299", PASSWORD, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    ResponseEntity<Client> clientEntity = testRestTemplate.postForEntity("/api/clients", request,
        Client.class);
    Client responseClient = clientEntity.getBody();
    assertThat(clientEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseClient.getEmail()).isEqualTo(client.getEmail());
    assertThat(responseClient.getCellNo()).isEqualTo(client.getCellNo());
    assertThat(responseClient.getDeviceId()).isEqualTo(client.getDeviceId());

    Client duplicateClient = buildClient("add.duplicate.email@gmail.com", "5501674798299", PASSWORD,
        true);
    HttpEntity<?> duplicateRequest = buildClientRequest(duplicateClient, new HttpHeaders(),
        "pervez.jpg");
    ResponseEntity<Map> responseEntity = testRestTemplate.postForEntity("/api/clients",
        duplicateRequest, Map.class);
    Map duplicateMap = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(duplicateMap.get("message")).isEqualTo(
        "email:" + duplicateClient.getEmail() + ", cellNo:" + duplicateClient.getCellNo());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void addClientWithDuplicateCellNo_returnsBadRequest() {
    Client client = buildClient("add.duplicate.cellno@gmail.com", "1101674798299", PASSWORD, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    ResponseEntity<Client> clientEntity = testRestTemplate.postForEntity("/api/clients", request,
        Client.class);
    Client responseClient = clientEntity.getBody();
    assertThat(clientEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseClient.getEmail()).isEqualTo(client.getEmail());
    assertThat(responseClient.getCellNo()).isEqualTo(client.getCellNo());
    assertThat(responseClient.getDeviceId()).isEqualTo(client.getDeviceId());

    Client duplicateClient = buildClient("add.duplicate.newcellno@gmail.com", "1101674798299",
        PASSWORD, true);
    HttpEntity<?> duplicateRequest = buildClientRequest(duplicateClient, new HttpHeaders(),
        "pervez.jpg");
    ResponseEntity<Map> responseEntity = testRestTemplate.postForEntity("/api/clients",
        duplicateRequest, Map.class);
    Map duplicateMap = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(duplicateMap.get("message")).isEqualTo(
        "email:" + duplicateClient.getEmail() + ", cellNo:" + duplicateClient.getCellNo());
  }

  @Test
  public void addClientWithoutDeviceInfo() {
    Client client = buildClient("sajjad@gmail.com", "8801674798200", PASSWORD, false);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");

    ResponseEntity<Client> responseEntity = testRestTemplate.postForEntity("/api/clients", request,
        Client.class);
    Client responseClient = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(responseClient.getEmail()).isEqualTo(client.getEmail());
    assertThat(responseClient.getCellNo()).isEqualTo(client.getCellNo());
    assertThat(responseClient.getDeviceId()).isEqualTo(client.getDeviceId());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Test
  public void userLoginWithValidCradentials_returnsAccessToken()
      throws JsonParseException, JsonMappingException, IOException {
    createLoginAndLoadAuthorizationOfUser("user.login@gmail.com", "770167798798", PASSWORD);
    ResponseEntity<Map> responseEntity = login("user.login@gmail.com", PASSWORD);
    Map jwtMap = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(jwtMap.get("token_type")).isEqualTo("bearer");
    assertThat(jwtMap.get("scope")).isEqualTo("read write");
    assertThat(jwtMap.containsKey("access_token")).isTrue();
    assertThat(jwtMap.containsKey("expires_in")).isTrue();
    assertThat(jwtMap.containsKey("jti")).isTrue();
    String accessToken = (String) jwtMap.get("access_token");
    Jwt jwtToken = JwtHelper.decode(accessToken);
    String claims = jwtToken.getClaims();
    Map claimsMap = new ObjectMapper().readValue(claims, Map.class);
    assertThat(((List<String>) claimsMap.get("aud")).get(0)).isEqualTo(resourceIds);
    assertThat(claimsMap.get("client_id")).isEqualTo(clientId);
    assertThat(claimsMap.get("user_name")).isEqualTo("user.login@gmail.com");
    assertThat(((List<String>) claimsMap.get("scope")).get(0)).isEqualTo("read");
    assertThat(((List<String>) claimsMap.get("scope")).get(1)).isEqualTo("write");
    assertThat(((List<String>) claimsMap.get("authorities")).get(0)).isEqualTo("CLIENT");
  }

  @Test
  public void getPageableClients() {
    HttpHeaders headers = createLoginAndLoadAuthorizationOfUser("get.pageable.clients@gmail.com",
        "880167798798", PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(headers);
    UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/clients")
        .queryParam("page", 0).queryParam("size", 2);

    ResponseEntity<PagedResources<Client>> responseEntity = testRestTemplate.exchange(
        builder.toUriString(), HttpMethod.GET, request,
        new ParameterizedTypeReference<PagedResources<Client>>() {
        });
    Collection<Client> clients = responseEntity.getBody().getContent();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(clients.size()).isGreaterThan(0).isLessThanOrEqualTo(2);
  }

  @Test
  public void getOwnDetails() {
    HttpHeaders headers = createLoginAndLoadAuthorizationOfUser("get.own.details@gmail.com",
        "220167798798", PASSWORD);
    HttpEntity<?> request = new HttpEntity<>(headers);

    ResponseEntity<Client> responseEntity = testRestTemplate.exchange("/api/clients/details",
        HttpMethod.GET, request, Client.class);
    Client client = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(client.getId()).isPositive();
    assertThat(client.getEmail()).isEqualTo("get.own.details@gmail.com");
    assertThat(client.getCellNo()).isEqualTo("220167798798");
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void getClientDetailsById() {
    Client client = buildClient("get.client.details.by.id@gmail.com", "330167798798", PASSWORD,
        true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    client = testRestTemplate.postForEntity("/api/clients", request, Client.class).getBody();
    Map jwtMap = login(client.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    request = new HttpEntity<>(headers);

    ResponseEntity<Client> responseEntity = testRestTemplate
        .exchange("/api/clients/" + client.getId(), HttpMethod.GET, request, Client.class);
    Client retrivedClient = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(retrivedClient.getId()).isEqualTo(client.getId());
    assertThat(retrivedClient.getEmail()).isEqualTo(client.getEmail());
    assertThat(retrivedClient.getCellNo()).isEqualTo(client.getCellNo());
    assertThat(retrivedClient.getDeviceId()).isEqualTo(client.getDeviceId());
    assertThat(retrivedClient.getImageFileName()).isEqualTo(client.getImageFileName());
    assertThat(retrivedClient.getPushNotificationId()).isEqualTo(client.getPushNotificationId());
    assertThat(retrivedClient.getRoles().get(0)).isEqualTo(client.getRoles().get(0));
    assertThat(retrivedClient.getTimestamp()).isEqualTo(client.getTimestamp());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void updateClientPushNotificationId() {
    Client client = buildClient("update.client@gmail.com", "910167798798", PASSWORD, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    Client clientToUpdate = testRestTemplate.postForEntity("/api/clients", request, Client.class)
        .getBody();
    Map jwtMap = login(client.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    clientToUpdate.setPassword(PASSWORD);
    clientToUpdate.setPushNotificationId("new_push_not");
    HttpEntity<?> updateRequest = buildClientRequest(clientToUpdate, headers, "pervez.jpg");

    ResponseEntity<Client> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + clientToUpdate.getId(), HttpMethod.PUT, updateRequest, Client.class);
    Client updatedClient = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedClient.getPushNotificationId())
        .isEqualTo(clientToUpdate.getPushNotificationId());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void deleteClient() {
    Client client = buildClient("delete.client@gmail.com", "910163398798", PASSWORD, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    Client clientToDelete = testRestTemplate.postForEntity("/api/clients", request, Client.class)
        .getBody();
    clientToDelete.setPassword(PASSWORD);
    Map jwtMap = login(client.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    HttpEntity<?> deleteRequest = buildClientRequest(clientToDelete, headers, "pervez.jpg");

    ResponseEntity<Client> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + clientToDelete.getId(), HttpMethod.DELETE, deleteRequest, Client.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void addClientDeviceInfo() {
    Client c = buildClient("add.device.info@gmail.com", "910000098798", PASSWORD, true);
    HttpEntity<?> req = buildClientRequest(c, new HttpHeaders(), "pervez.jpg");
    Client client = testRestTemplate.postForEntity("/api/clients", req, Client.class).getBody();
    client.setPassword(PASSWORD);
    Map jwtMap = login(c.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    headers.setContentType(MediaType.APPLICATION_JSON);
    ClientDeviceInfo clientDeviceInfo = ClientDeviceInfo.builder().lat(1.0).lon(1.0).altitude(1.0)
        .speed(1.0).accuracy(1.0).bearing(1.0).battery(1).provider("1").build();
    HttpEntity<ClientDeviceInfo> request = new HttpEntity<>(clientDeviceInfo, headers);

    ResponseEntity<Boolean> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/deviceinfo", HttpMethod.POST, request, Boolean.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isTrue();
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void addClientFavoritePlace() {
    Client c = buildClient("add.favorite.place@gmail.com", "910000000000", PASSWORD, true);
    HttpEntity<?> req = buildClientRequest(c, new HttpHeaders(), "pervez.jpg");
    Client client = testRestTemplate.postForEntity("/api/clients", req, Client.class).getBody();
    client.setPassword(PASSWORD);
    Map jwtMap = login(c.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    headers.setContentType(MediaType.APPLICATION_JSON);
    ClientFavoritePlace clientFavorintePlace = ClientFavoritePlace.builder().lat(1.0).lon(1.0)
        .tagName("Home").active(true).build();
    HttpEntity<ClientFavoritePlace> request = new HttpEntity<>(clientFavorintePlace, headers);

    ResponseEntity<ClientFavoritePlace> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces", HttpMethod.POST, request,
        ClientFavoritePlace.class);
    ClientFavoritePlace cfp = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(cfp.getId()).isPositive();
    assertThat(cfp.getLat()).isEqualTo(clientFavorintePlace.getLat());
    assertThat(cfp.getLon()).isEqualTo(clientFavorintePlace.getLon());
    assertThat(cfp.getTagName()).isEqualTo(clientFavorintePlace.getTagName());
    assertThat(cfp.getActive()).isEqualTo(clientFavorintePlace.getActive());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void updateClientFavoritePlaceTagName() {
    Client c = buildClient("update.favorite.place@gmail.com", "910000000001", PASSWORD, true);
    HttpEntity<?> req = buildClientRequest(c, new HttpHeaders(), "pervez.jpg");
    Client client = testRestTemplate.postForEntity("/api/clients", req, Client.class).getBody();
    client.setPassword(PASSWORD);
    Map jwtMap = login(c.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    headers.setContentType(MediaType.APPLICATION_JSON);
    ClientFavoritePlace clientFavorintePlace = ClientFavoritePlace.builder().lat(1.0).lon(1.0)
        .tagName("Home").active(true).build();
    HttpEntity<ClientFavoritePlace> request = new HttpEntity<>(clientFavorintePlace, headers);
    ResponseEntity<ClientFavoritePlace> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces", HttpMethod.POST, request,
        ClientFavoritePlace.class);
    ClientFavoritePlace cfp = responseEntity.getBody();
    cfp.setTagName("Work");
    request = new HttpEntity<>(cfp, headers);

    responseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces/" + cfp.getId(), HttpMethod.PUT,
        request, ClientFavoritePlace.class);

    ClientFavoritePlace updatedCfp = responseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedCfp.getId()).isEqualTo(cfp.getId());
    assertThat(updatedCfp.getLat()).isEqualTo(cfp.getLat());
    assertThat(updatedCfp.getLon()).isEqualTo(cfp.getLon());
    assertThat(updatedCfp.getTagName()).isEqualTo(cfp.getTagName());
    assertThat(updatedCfp.getActive()).isEqualTo(cfp.getActive());
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void deleteClientFavoritePlace() {
    Client c = buildClient("delete.favorite.place@gmail.com", "910000000002", PASSWORD, true);
    HttpEntity<?> req = buildClientRequest(c, new HttpHeaders(), "pervez.jpg");
    Client client = testRestTemplate.postForEntity("/api/clients", req, Client.class).getBody();
    client.setPassword(PASSWORD);
    Map jwtMap = login(c.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    headers.setContentType(MediaType.APPLICATION_JSON);
    ClientFavoritePlace clientFavorintePlace = ClientFavoritePlace.builder().lat(1.0).lon(1.0)
        .tagName("Home").active(true).build();
    HttpEntity<ClientFavoritePlace> request = new HttpEntity<>(clientFavorintePlace, headers);
    ResponseEntity<ClientFavoritePlace> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces", HttpMethod.POST, request,
        ClientFavoritePlace.class);
    ClientFavoritePlace cfp = responseEntity.getBody();
    cfp.setTagName("Work");
    request = new HttpEntity<>(cfp, headers);

    ResponseEntity<Boolean> deleteResponseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces/" + cfp.getId(), HttpMethod.DELETE,
        request, Boolean.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(deleteResponseEntity.getBody()).isTrue();
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void getClientFavoritePlaces() {
    Client c = buildClient("get.favorite.places@gmail.com", "910000000019", PASSWORD, true);
    HttpEntity<?> req = buildClientRequest(c, new HttpHeaders(), "pervez.jpg");
    Client client = testRestTemplate.postForEntity("/api/clients", req, Client.class).getBody();
    client.setPassword(PASSWORD);
    Map jwtMap = login(c.getEmail(), PASSWORD).getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    headers.setContentType(MediaType.APPLICATION_JSON);
    ClientFavoritePlace clientFavorintePlace = ClientFavoritePlace.builder().lat(1.0).lon(1.0)
        .tagName("Home").active(true).build();
    HttpEntity<ClientFavoritePlace> request = new HttpEntity<>(clientFavorintePlace, headers);
    ResponseEntity<ClientFavoritePlace> responseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces", HttpMethod.POST, request,
        ClientFavoritePlace.class);
    ClientFavoritePlace cfp = responseEntity.getBody();
    cfp.setTagName("Work");
    request = new HttpEntity<>(cfp, headers);

    ResponseEntity<List<ClientFavoritePlace>> getResponseEntity = testRestTemplate.exchange(
        "/api/clients/" + client.getId() + "/favoriteplaces", HttpMethod.GET,
        request, new ParameterizedTypeReference<List<ClientFavoritePlace>>() {
        });
    List<ClientFavoritePlace> cfpList = getResponseEntity.getBody();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(cfpList.size()).isEqualTo(1);
  }

  @SuppressWarnings("rawtypes")
  private HttpHeaders createLoginAndLoadAuthorizationOfUser(String email, String cellNo,
      String password) {
    Client client = buildClient(email, cellNo, password, true);
    HttpEntity<?> request = buildClientRequest(client, new HttpHeaders(), "pervez.jpg");
    testRestTemplate.postForEntity("/api/clients", request, Client.class);

    ResponseEntity<Map> responseEntity = login(email, password);
    Map jwtMap = responseEntity.getBody();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtMap.get("access_token"));
    return headers;
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
