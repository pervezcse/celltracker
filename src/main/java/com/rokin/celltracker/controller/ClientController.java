package com.rokin.celltracker.controller;

import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.ClientFavoritePlace;
import com.rokin.celltracker.service.ClientService;
import java.security.Principal;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clients")
@PreAuthorize("hasAuthority('CLIENT')")
public class ClientController {

  private final ClientService clientService;

  /**
   * Add client.
   * 
   * @param client
   *          client
   * @param uploadFile
   *          image
   * @return
   */
  @PostMapping
  @PreAuthorize("isAnonymous()")
  public ResponseEntity<Client> addClient(@Valid Client client,
      @RequestParam("file") MultipartFile uploadFile) {
    Client newClient = clientService.addClient(client, uploadFile);
    if (newClient == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } else {
      return new ResponseEntity<>(newClient, HttpStatus.CREATED);
    }
  }

  @GetMapping
  public Page<Client> getClients(Pageable pageable) {
    return clientService.getClients(pageable);
  }

  @GetMapping(value = "/details")
  public Client getClient(Principal principal) {
    return clientService.getClientByUserName(principal.getName());
  }

  @GetMapping(value = "/{id}")
  public Client getClient(@PathVariable(value = "id", required = true) Long id) {
    return clientService.getClient(id);
  }

  /**
   * update client.
   * 
   * @param id
   *          of client
   * @param client
   *          client info
   * @param uploadFile
   *          of client image
   * @param principal
   *          of client
   * @return
   */
  @PutMapping(value = "/{id}")
  public Client updateClient(@PathVariable(value = "id", required = true) Long id,
      @Valid Client client, @RequestParam("file") MultipartFile uploadFile, Principal principal) {
    if (id == client.getId()) {
      return clientService.updateClient(principal.getName(), client, uploadFile);
    }
    throw new IllegalArgumentException("client info not valid");
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<String> deleteClient(@PathVariable(value = "id", required = true) Long id,
      Principal principal) {
    clientService.deleteClient(principal.getName(), id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping(value = "/{id}/deviceinfo")
  public Boolean addClientDeviceInfo(@PathVariable(value = "id", required = true) Long id,
      @RequestBody @Valid ClientDeviceInfo clientDeviceInfo, Principal principal) {
    return clientService.addClientDeviceInfo(principal.getName(), clientDeviceInfo);
  }

  @PostMapping(value = "/{id}/favoriteplaces")
  public ClientFavoritePlace addClientFavoritePlace(
      @PathVariable(value = "id", required = true) Long id,
      @RequestBody @Valid ClientFavoritePlace clientFavoritePlace, Principal principal) {
    return clientService.addClientFavoritePlace(principal.getName(), clientFavoritePlace);
  }

  @PutMapping(value = "/{id}/favoriteplaces/{fovoritePlaceId}")
  public ClientFavoritePlace updateClientFavoritePlace(
      @PathVariable(value = "id", required = true) Long id,
      @PathVariable(value = "fovoritePlaceId", required = true) Long fovoritePlaceId,
      @RequestBody @Valid ClientFavoritePlace clientFavoritePlace, Principal principal) {
    return clientService.updateClientFavoritePlace(principal.getName(), clientFavoritePlace);
  }

  @DeleteMapping(value = "/{id}/favoriteplaces/{fovoritePlaceId}")
  public Boolean deleteClientFavoritePlace(@PathVariable(value = "id", required = true) Long id,
      @PathVariable(value = "fovoritePlaceId", required = true) Long fovoritePlaceId,
      Principal principal) {
    return clientService.deleteClientFavoritePlace(principal.getName(), fovoritePlaceId);
  }

  @GetMapping(value = "/{id}/favoriteplaces")
  public List<ClientFavoritePlace> getClientFavoritePlaces(
      @PathVariable(value = "id", required = true) Long id, Principal principal) {
    return clientService.getClientFavoritePlaces(principal.getName());
  }

}
