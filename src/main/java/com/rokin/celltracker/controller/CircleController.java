package com.rokin.celltracker.controller;

import com.rokin.celltracker.domain.Circle;
import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import com.rokin.celltracker.domain.Message;
import com.rokin.celltracker.dto.CircleInfo;
import com.rokin.celltracker.service.CircleService;
import java.security.Principal;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@RestController
@RequestMapping("/api/circles")
@PreAuthorize("hasAuthority('CLIENT')")
public class CircleController {

  @Resource
  CircleService circleService;

  /**
   * Add circle.
   * 
   * @param circleName
   *          circle name
   * @param principal
   *          user session id
   * @return
   */
  @PostMapping
  public ResponseEntity<Circle> addCircle(
      @RequestParam(value = "circlename", required = true) String circleName, Principal principal) {
    Circle newCircle = circleService.addCircle(principal.getName(), circleName);
    if (newCircle == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } else {
      return new ResponseEntity<>(newCircle, HttpStatus.CREATED);
    }
  }

  /**
   * update circle.
   * 
   * @param circleId
   *          of circle
   * @param circle
   *          of user
   * @param principal
   *          of user
   * @return
   */
  @PutMapping(value = "/{circleId}")
  public Circle updateCircle(@PathVariable(value = "circleId", required = true) String circleId,
      @RequestBody @Valid Circle circle, Principal principal) {
    if (circleId.equals(circle.getId())) {
      return circleService.updateCircle(principal.getName(), circle);
    }
    throw new IllegalArgumentException(circleId);
  }

  @DeleteMapping(value = "/{circleId}")
  public Boolean deleteCircle(@PathVariable(value = "circleId", required = true) String circleId,
      Principal principal) {
    return circleService.deleteCircle(principal.getName(), circleId);
  }

  @GetMapping
  public List<CircleInfo> getCircles(Principal principal) {
    return circleService.getCircles(principal.getName());
  }

  @GetMapping(value = "/{circleId}/members")
  public List<Client> getCircleMemebers(
      @PathVariable(value = "circleId", required = true) String circleId, Principal principal) {
    return circleService.getCircleMemebers(principal.getName(), circleId);
  }

  @GetMapping(value = "/{circleId}/members/{memberId}/locationhistory")
  public List<ClientDeviceInfo> getCircleMemberLocationHistory(
      @PathVariable(value = "circleId", required = true) String circleId,
      @PathVariable(value = "memberId", required = true) Long memeberId,
      @RequestParam(value = "fromtime", required = true) Long fromTime,
      @RequestParam(value = "totime", required = true) Long toTime, Principal principal) {
    return circleService.getCircleMemberLocationHistory(principal.getName(), circleId, memeberId,
        fromTime, toTime);
  }

  /**
   * Join circle.
   * 
   * @param circleCode
   *          circle code
   * @param principal
   *          user session info
   * @return
   */
  @GetMapping(value = "/{circleCode}/join")
  public ResponseEntity<Circle> joinCircle(
      @PathVariable(value = "circleCode", required = true) String circleCode, Principal principal) {
    Circle circle = circleService.joinCircle(principal.getName(), circleCode);
    if (circle == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } else {
      return new ResponseEntity<>(circle, HttpStatus.OK);
    }
  }

  @GetMapping(value = "/{circleId}/leave")
  public Boolean leaveCircle(@PathVariable(value = "circleId", required = true) String circleId,
      Principal principal) {
    return circleService.leaveCircle(principal.getName(), circleId);
  }

  @GetMapping(value = "/{circleId}/newcode")
  public String refreshCode(@PathVariable(value = "circleId", required = true) String circleId,
      Principal principal) {
    return circleService.refreshCode(principal.getName(), circleId);
  }

  @PostMapping(value = "/push")
  public ResponseEntity<String> pushMessage(@Valid @RequestBody Message message,
      Principal principal) {
    circleService.sendMessage(principal.getName(), message);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
