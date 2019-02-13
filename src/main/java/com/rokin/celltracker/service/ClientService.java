package com.rokin.celltracker.service;

import com.rokin.celltracker.ImageUploader;
import com.rokin.celltracker.ImageUploader.FileInfo;
import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.ClientFavoritePlace;
import com.rokin.celltracker.exception.FavoritePlaceNotFoundException;
import com.rokin.celltracker.exception.UserAlreadyExistsException;
import com.rokin.celltracker.exception.UserMismatchException;
import com.rokin.celltracker.exception.UserNotFoundException;
import com.rokin.celltracker.repository.ClientDeviceInfoRepository;
import com.rokin.celltracker.repository.ClientFavouritePlaceRepository;
import com.rokin.celltracker.repository.ClientRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClientService {

  private final ClientRepository clientRepo;
  private final ImageUploader imageUploader;
  private final ClientDeviceInfoRepository clientDeviceInfoRepo;
  private final ClientFavouritePlaceRepository clientFavouritePlaceRepo;
  private final PasswordEncoder passwordEncoder;

  /**
   * Add client.
   * 
   * @param client
   *          client
   * @param uploadfile
   *          image file
   * @return
   */
  @Transactional
  public Client addClient(Client client, MultipartFile uploadfile) {

    Optional<Client> c = clientRepo.findByEmailOrCellNo(client.getEmail(), client.getCellNo());
    if (!c.isPresent()) {
      Optional<String> imageFileName = Optional.empty();
      if (uploadfile != null) {
        List<FileInfo> fileInfoList = imageUploader.saveUploadedFiles(Arrays.asList(uploadfile));
        if (fileInfoList.size() == 1) {
          imageFileName = Optional.of(fileInfoList.get(0).getFileName());
        }
      }
      client.setImageFileName(imageFileName.orElse(null));
      client.setPassword(passwordEncoder.encode(client.getPassword()));
      client.setTimestamp(System.currentTimeMillis());
      ClientDeviceInfo cdi = client.getLatestDeviceInfo();
      client.setLatestDeviceInfo(null);
      Client newClient = clientRepo.save(client);
      updateClientDeviceInfo(newClient.getId(), cdi);
      return newClient;
    }
    throw new UserAlreadyExistsException(
        "email:" + client.getEmail() + ", cellNo:" + client.getCellNo());
  }

  /**
   * Update client.
   * 
   * @param client
   *          clent
   * @param uploadfile
   *          image file
   * @return
   */
  @Transactional
  public Client updateClient(String username, Client client, MultipartFile uploadfile) {
    Client c = getClientByUserName(username);
    if (c.getId() == client.getId()) {
      client.setTimestamp(System.currentTimeMillis());
      if (uploadfile != null) {
        List<FileInfo> fileInfoList = imageUploader.saveUploadedFiles(Arrays.asList(uploadfile));
        if (fileInfoList.size() == 1) {
          client.setImageFileName(fileInfoList.get(0).getFileName());
        }
      }
      updateClientDeviceInfo(client.getId(), client.getLatestDeviceInfo());
      return clientRepo.save(client);
    }
    throw new UserMismatchException(String.valueOf(client.getId()));
  }

  /**
   * delete client.
   * 
   * @param username
   *          userame
   */
  public Boolean deleteClient(String username, Long clientId) {
    Client client = getClientByUserName(username);
    if (clientId == client.getId()) {
      client.setEnabled(false);
      client.setTimestamp(System.currentTimeMillis());
      clientRepo.save(client);
      return true;
    }
    throw new UserMismatchException(String.valueOf(clientId));
  }

  public Page<Client> getClients(Pageable pageable) {
    return clientRepo.findAll(pageable);
  }

  public Boolean addClientDeviceInfo(String username, ClientDeviceInfo clientDeviceInfo) {
    Client client = getClientByUserName(username);
    return updateClientDeviceInfo(client.getId(), clientDeviceInfo);
  }

  /**
   * Update client device information.
   * 
   * @param clientId
   *          cleint id
   * @param clientDeviceInfo
   *          client device info
   */
  public Boolean updateClientDeviceInfo(Long clientId, ClientDeviceInfo clientDeviceInfo) {
    if (clientDeviceInfo != null) {
      Client client = getClient(clientId);
      clientDeviceInfo.setClient(client);
      clientDeviceInfo.setTimestamp(System.currentTimeMillis());
      ClientDeviceInfo cdi = clientDeviceInfoRepo.save(clientDeviceInfo);
      client.setLatestDeviceInfo(cdi);
      clientRepo.save(client);
      return true;
    }
    return false;
  }

  public Client getClient(Long id) {
    return clientRepo.findById(id).orElseThrow(() -> new UserNotFoundException(String.valueOf(id)));
  }

  public Client getClientByUserName(String username) {
    return clientRepo.findByEmailOrCellNo(username, username)
        .orElseThrow(() -> new UserNotFoundException(username));
  }

  public Client getClientByEmail(String email) {
    return clientRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
  }

  public Client getClientByCellNo(String cellNo) {
    return clientRepo.findByCellNo(cellNo).orElseThrow(() -> new UserNotFoundException(cellNo));
  }

  /**
   * add Client Favorite Place.
   * 
   * @param username
   *          username
   * @param clientFavoritePlace
   *          client favorite place
   * @return
   */
  public ClientFavoritePlace addClientFavoritePlace(String username,
      ClientFavoritePlace clientFavoritePlace) {
    Client client = getClientByUserName(username);
    clientFavoritePlace.setClient(client);
    clientFavoritePlace.setActive(true);
    clientFavoritePlace.setTimestamp(System.currentTimeMillis());
    return clientFavouritePlaceRepo.save(clientFavoritePlace);
  }

  /**
   * update client favorite place.
   * 
   * @param username
   *          username
   * @param clientFavoritePlace
   *          cleint favorite place
   * @return
   */
  public ClientFavoritePlace updateClientFavoritePlace(String username,
      @Valid ClientFavoritePlace clientFavoritePlace) {
    Client client = getClientByUserName(username);
    clientFavoritePlace.setClient(client);
    clientFavoritePlace.setTimestamp(System.currentTimeMillis());
    return clientFavouritePlaceRepo.save(clientFavoritePlace);
  }

  /**
   * delete Client Favorite Place.
   * 
   * @param username username
   * @param fovoritePlaceId client favorite place id
   * @return
   */
  public Boolean deleteClientFavoritePlace(String username, Long fovoritePlaceId) {
    Client client = getClientByUserName(username);
    Optional<ClientFavoritePlace> cfp = clientFavouritePlaceRepo.findById(fovoritePlaceId);
    if (cfp.isPresent() && cfp.get().getClient().getId() == client.getId()) {
      cfp.get().setClient(client);
      cfp.get().setActive(false);
      cfp.get().setTimestamp(System.currentTimeMillis());
      clientFavouritePlaceRepo.save(cfp.get());
      return true;
    }
    throw new FavoritePlaceNotFoundException(String.valueOf(fovoritePlaceId));
  }

  public List<ClientFavoritePlace> getClientFavoritePlaces(String username) {
    Client client = getClientByUserName(username);
    return clientFavouritePlaceRepo.findByClient(client);
  }

}
