package com.rokin.celltracker.service;

import com.rokin.celltracker.RandomUtil;
import com.rokin.celltracker.domain.Circle;
import com.rokin.celltracker.domain.Circle.CircleBuilder;
import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientCircle;
import com.rokin.celltracker.domain.ClientCircle.Role;
import com.rokin.celltracker.domain.ClientCirclePk;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.Message;
import com.rokin.celltracker.dto.CircleInfo;
import com.rokin.celltracker.exception.CircleAlreadyExistsException;
import com.rokin.celltracker.exception.CircleCodeNotFoundEception;
import com.rokin.celltracker.exception.CircleNotFoundException;
import com.rokin.celltracker.exception.MemberNotFoundException;
import com.rokin.celltracker.exception.UserNotFoundException;
import com.rokin.celltracker.repository.CircleRepository;
import com.rokin.celltracker.repository.ClientCircleRepository;
import com.rokin.celltracker.repository.ClientDeviceInfoRepository;
import com.rokin.celltracker.repository.MessageRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CircleService {

  private static final Long CIRCLECODEEXPIRYTIME = 5 * 24 * 60 * 60 * 1000L;

  private final ClientService clientService;
  private final PushService pushService;
  private final CircleRepository circleRepo;
  private final ClientCircleRepository clientCircleRepo;
  private final MessageRepository messageRepo;
  private final ClientDeviceInfoRepository clientDeviceInfoRepo;

  /**
   * Add circle.
   * 
   * @param username
   *          client email or cell no
   * @param circleName
   *          circle name
   * @return
   */
  public Circle addCircle(String username, String circleName) {
    Client client = clientService.getClientByUserName(username);
    if (!client.isEnabled()) {
      throw new UserNotFoundException(username);
    }
    Long clientId = client.getId();
    Boolean circleExists = circleRepo.existsById(circleName + "_" + clientId);
    if (!circleExists) {
      CircleBuilder builder = Circle.builder().id(circleName + "_" + clientId).name(circleName);
      String generatedCode = "";
      do {
        generatedCode = RandomUtil.createWord(6);
      } while (circleRepo.findByCode(generatedCode).isPresent());
      builder.code(generatedCode).codeUpdateTimestamp(System.currentTimeMillis()).owner(client)
          .active(true);
      Circle circle = circleRepo.save(builder.build());
      ClientCircle clientCircle = ClientCircle.builder().circle(circle).client(client)
          .role(Role.OWNER).isInCircle(true).jointimestamp(System.currentTimeMillis()).build();
      clientCircleRepo.save(clientCircle);
      return circle;
    }
    throw new CircleAlreadyExistsException(circleName);
  }

  /**
   * Update circle.
   * 
   * @param username
   *          username
   * @param circle
   *          circle
   * @return
   */
  public Circle updateCircle(String username, Circle circle) {
    Client client = clientService.getClientByUserName(username);
    Optional<Circle> circleOpt = circleRepo.findByIdAndOwner(circle.getId(), client);
    if (circleOpt.isPresent()) {
      circle.setCodeUpdateTimestamp(System.currentTimeMillis());
      return circleRepo.save(circle);
    }
    throw new CircleNotFoundException(circle.getId());
  }

  /**
   * Get circles.
   * 
   * @param username
   *          of client
   * @return
   */
  public List<CircleInfo> getCircles(String username) {
    Client client = clientService.getClientByUserName(username);
    List<ClientCircle> clientCircles = clientCircleRepo.findByClient(client);
    List<Circle> circles = clientCircles.stream().map(ClientCircle::getCircle)
        .collect(Collectors.toList());
    return circles.stream().map(c -> new CircleInfo(c, clientCircleRepo.findByCircle(c).size()))
        .collect(Collectors.toList());
  }

  /**
   * Delete circle.
   * 
   * @param username
   *          username
   * @param circleId
   *          circle id
   * @return
   */
  public Boolean deleteCircle(String username, String circleId) {
    Client client = clientService.getClientByUserName(username);
    Optional<Circle> circleOpt = circleRepo.findByIdAndOwner(circleId, client);
    if (circleOpt.isPresent()) {
      Circle c = circleOpt.get();
      c.setCodeUpdateTimestamp(System.currentTimeMillis());
      c.setActive(false);
      circleRepo.save(c);
      return true;
    }
    return false;
  }

  /**
   * Get circle members.
   * 
   * @param username
   *          of client
   * @param circleId
   *          of circle
   * @return
   */
  public List<Client> getCircleMemebers(String username, String circleId) {
    Client client = clientService.getClientByUserName(username);
    Circle circle = Circle.builder().id(circleId).build();
    List<ClientCircle> clientCircles = clientCircleRepo.findByCircle(circle);
    Boolean isMember = clientCircles.stream()
        .anyMatch(cc -> cc.getClient().getId() == client.getId() && cc.getIsInCircle());
    if (isMember) {
      return clientCircles.stream().map(ClientCircle::getClient)
          .filter(c -> c.getId() != client.getId()).collect(Collectors.toList());
    }
    throw new CircleNotFoundException(circleId);
  }

  /**
   * Get member location history.
   * 
   * @param username
   *          of client
   * @param circleId
   *          of circle
   * @param memeberId
   *          of circle member
   * @param fromTime
   *          of location history
   * @param toTime
   *          of location history
   * @return
   */
  public List<ClientDeviceInfo> getCircleMemberLocationHistory(String username, String circleId,
      Long memeberId, Long fromTime, Long toTime) {
    Client client = clientService.getClientByUserName(username);
    Client member = Client.builder().id(memeberId).build();
    Circle circle = Circle.builder().id(circleId).build();
    List<ClientCircle> clientCircles = clientCircleRepo.findByCircle(circle);
    Boolean isBuddy = clientCircles.stream()
        .anyMatch(cc -> cc.getClient().getId().equals(client.getId()) && cc.getIsInCircle())
        && clientCircles.stream()
            .anyMatch(cc -> cc.getClient().getId().equals(memeberId) && cc.getIsInCircle());
    if (isBuddy) {
      return clientDeviceInfoRepo.findByClientAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
          member, fromTime, toTime);
    }
    throw new MemberNotFoundException(String.valueOf(memeberId));
  }

  /**
   * Join circle.
   * 
   * @param username
   *          client email or cell no
   * @param circleCode
   *          circle code
   * @return
   */
  public Circle joinCircle(String username, String circleCode) {
    Client client = clientService.getClientByUserName(username);
    Optional<Circle> circleOpt = circleRepo.findByCode(circleCode);
    if (circleOpt.isPresent()) {
      Circle circle = circleOpt.get();
      Role clientRole = circle.getOwner().getId().equals(client.getId()) ? Role.OWNER : Role.MEMBER;
      ClientCircle clientCircle = ClientCircle.builder().circle(circle).client(client)
          .role(clientRole).isInCircle(true).jointimestamp(System.currentTimeMillis()).build();
      ;
      clientCircleRepo.save(clientCircle);
      return circle;
    }
    throw new CircleCodeNotFoundEception(circleCode);
  }

  /**
   * leave circle.
   * 
   * @param username
   *          client email or cell no
   * @param circleId
   *          circle id
   * @return
   */
  public Boolean leaveCircle(String username, String circleId) {
    Client client = clientService.getClientByUserName(username);
    Optional<Circle> circleOpt = circleRepo.findById(circleId);
    if (circleOpt.isPresent()) {
      Circle circle = circleOpt.get();
      ClientCirclePk pk = new ClientCirclePk(client.getId(), circle.getId());
      Optional<ClientCircle> clientCircleOpt = clientCircleRepo.findById(pk);
      if (clientCircleOpt.isPresent()) {
        ClientCircle clientCircle = clientCircleOpt.get();
        clientCircle.setIsInCircle(false);
        clientCircleRepo.save(clientCircle);
        return true;
      }
    }
    throw new CircleNotFoundException(circleId);
  }

  /**
   * get circle code.
   * 
   * @param username
   *          client email or cell no
   * @param circleId
   *          circle id
   * @return
   */
  public String refreshCode(String username, String circleId) {
    Client client = clientService.getClientByUserName(username);
    Optional<Circle> circle = circleRepo.findByIdAndOwner(circleId, client);
    if (circle.isPresent()) {
      if (System.currentTimeMillis()
          - circle.get().getCodeUpdateTimestamp() > CIRCLECODEEXPIRYTIME) {
        String generatedCode = "";
        do {
          generatedCode = RandomUtil.createWord(6);
        } while (circleRepo.findByCode(generatedCode).isPresent());
        circle.get().setCode(generatedCode);
        circleRepo.save(circle.get());
      }
      return circle.get().getCode();
    }
    throw new CircleNotFoundException(circleId);
  }

  /**
   * Send message.
   * 
   * @param fromClientUserName
   *          client email or cell no
   * @param message
   *          message
   */
  public void sendMessage(String fromClientUserName, Message message) {
    Client fromClient = clientService.getClientByUserName(fromClientUserName);
    message.setFromClientId(fromClient.getId());
    message.setIsSent(false);
    clientService.updateClientDeviceInfo(fromClient.getId(), message.getDeviceInfo());
    message = messageRepo.save(message);
    Optional<Circle> circleOpt = circleRepo.findById(message.getToCircleId());
    switch (message.getMessageScope()) {
      case UNICAST:
        if (circleOpt.isPresent()) {
          Circle circle = circleOpt.get();
          Client toClient = clientService.getClient(message.getToClientId());
          Optional<ClientCircle> fromClientCircle = clientCircleRepo
              .findByClientAndCircleAndIsInCircle(fromClient, circle, true);
          Optional<ClientCircle> toClientCircle = clientCircleRepo
              .findByClientAndCircleAndIsInCircle(toClient, circle, true);
          if (fromClientCircle.isPresent() && toClientCircle.isPresent()) {
            List<String> regIdList = Arrays.asList(toClient.getPushNotificationId());
            pushService.sendMessage(message, regIdList).thenAccept(msg -> {
              msg.setIsSent(true);
              messageRepo.save(msg);
            });
            return;
          }
          throw new MemberNotFoundException(String.valueOf(message.getToClientId()));
        }
        throw new CircleNotFoundException(message.getToCircleId());
      case CIRCLE:
        if (circleOpt.isPresent()) {
          Circle circle = circleOpt.get();
          Optional<ClientCircle> fromClientCircle = clientCircleRepo
              .findByClientAndCircleAndIsInCircle(fromClient, circle, true);
          if (fromClientCircle.isPresent()) {
            List<ClientCircle> clientCircleList = clientCircleRepo.findByCircle(circle);
            List<String> regIdList = clientCircleList.stream()
                .filter(
                    cc -> cc.getIsInCircle() && !cc.getClient().getId().equals(fromClient.getId()))
                .map(cc -> cc.getClient().getPushNotificationId()).collect(Collectors.toList());
            pushService.sendMessage(message, regIdList).thenAccept(msg -> {
              msg.setIsSent(true);
              messageRepo.save(msg);
            });
            return;
          }
          throw new MemberNotFoundException(fromClientUserName);
        }
        throw new CircleNotFoundException(message.getToCircleId());
      default:
        break;
    }
  }

}
