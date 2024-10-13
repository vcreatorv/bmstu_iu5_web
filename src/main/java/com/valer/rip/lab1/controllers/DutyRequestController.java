package com.valer.rip.lab1.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.valer.rip.lab1.dto.DutyRequestDTO;
import com.valer.rip.lab1.services.DutyRequestService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/duties-requests")
@SecurityRequirement(name = "Bearer Authentication")
public class DutyRequestController {

    private final DutyRequestService dutyRequestService;

    public DutyRequestController( DutyRequestService dutyRequestService) {
        this.dutyRequestService = dutyRequestService;
    }
   
    @DeleteMapping("/{dutyID}/{requestID}/delete")
    @PreAuthorize("hasAuthority('BUYER') and @userService.isOwnerOfRequest(#requestID, authentication.name)")
    public ResponseEntity<String> deleteProviderDutyFromConnectionRequest(@PathVariable("dutyID") int dutyID, @PathVariable("requestID") int requestID) {
        try {
            dutyRequestService.deleteProviderDutyFromConnectionRequest(dutyID, requestID);
            return ResponseEntity.status(HttpStatus.OK).body("Услуга с ID = " + dutyID + " успешно удалена из заявки с ID = " + requestID);
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении услуги с ID = " + dutyID + "из заявки c ID = " + requestID + ": " + e.getMessage());
        }
    }

    // @PutMapping("/{dutyID}/{requestID}/update")
    // public ResponseEntity<? extends Object> updateAmountInDutyRequest(@PathVariable("dutyID") int dutyID, @PathVariable("requestID") int requestID, @RequestParam("amount") int amount) {
    //     try {
    //         DutyRequest updatedDutyRequest = dutyRequestService.updateAmountInDutyRequest(dutyID, requestID, amount);
    //         return ResponseEntity.status(HttpStatus.OK).body(updatedDutyRequest);
    //     } 
    //     catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при изменении поля amount услуги с ID = " + dutyID + "в заявке c ID = " + requestID + ": " + e.getMessage());
    //     }
    // }
    @PutMapping("/{dutyID}/{requestID}/update")
    @PreAuthorize("hasAuthority('BUYER') and @userService.isOwnerOfRequest(#requestID, authentication.name)")
    public ResponseEntity<?> updateAmountInDutyRequest(@PathVariable("dutyID") int dutyID, 
                                                    @PathVariable("requestID") int requestID, 
                                                    @RequestParam("amount") int amount) {
        try {
            DutyRequestDTO updatedDutyRequest = dutyRequestService.updateAmountInDutyRequest(dutyID, requestID, amount);
            return ResponseEntity.status(HttpStatus.OK).body(updatedDutyRequest);
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при изменении поля amount услуги с ID = " + dutyID + 
                    " в заявке c ID = " + requestID + ": " + e.getMessage());
        }
    }
}
