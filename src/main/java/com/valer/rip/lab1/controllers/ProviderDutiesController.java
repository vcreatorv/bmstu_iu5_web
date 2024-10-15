package com.valer.rip.lab1.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.valer.rip.lab1.dto.ProviderDutyDTO;
import com.valer.rip.lab1.models.ProviderDuty;
import com.valer.rip.lab1.services.ProviderDutyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/provider-duties")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name="Услуги провайдера", description="Позволяет получить информацию об услугах провайдера")
public class ProviderDutiesController {

    private final ProviderDutyService providerDutyService;

    public ProviderDutiesController(ProviderDutyService providerDutyService) {
        this.providerDutyService = providerDutyService;
    }

    @GetMapping
    @Operation(
        summary = "Просмотр услуг провайдера",
        description = "Позволяет пользователю посмотреть доступные услуги провайдера"
    )
    public ResponseEntity<Map<String, Object>> getAllProviderDuties(@RequestParam(required = false) String title) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && !(auth instanceof AnonymousAuthenticationToken) ? auth.getName() : null;
        return ResponseEntity.status(HttpStatus.OK).body(providerDutyService.getAllProviderDuties(title, username));
    }

    @GetMapping("/{dutyID}")
    @Operation(
        summary = "Подробнее об услуге",
        description = "Позволяет пользователю получить более подробную информацию об услуге провайдера"
    )
    public ResponseEntity<?> getProviderDutyById(@PathVariable("dutyID") int dutyID) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(providerDutyService.getProviderDutyById(dutyID));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(
        summary = "Добавление новой услуги",
        description = "Позволяет модератору добавить новую услугу провайдера"
    )
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<? extends Object> createProviderDuty(@ModelAttribute ProviderDuty providerDuty) {
        try {
            // ProviderDuty providerDuty = providerDutyService.createProviderDuty(providerDuty);
            return ResponseEntity.status(HttpStatus.OK).body(providerDutyService.createProviderDuty(providerDuty));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // @PostMapping("/create")
    // @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    // public ResponseEntity<?> createProviderDuty(@ModelAttribute ProviderDutyDTO providerDutyDTO) {
    //     try {
    //         // ProviderDuty providerDuty = providerDutyService.createProviderDuty(providerDuty);
    //         return ResponseEntity.status(HttpStatus.OK).body(providerDutyService.createProviderDuty(providerDutyDTO));
    //     }
    //     catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    //     }
    // }

    @PutMapping("/{dutyID}/update")
    @Operation(
        summary = "Изменение услуги",
        description = "Позволяет модератору изменить информацию об услуге"
    )
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateProviderDuty(@PathVariable("dutyID") int dutyID, @ModelAttribute ProviderDutyDTO providerDutyDTO) {
        try {
            ProviderDuty updatedDuty = providerDutyService.updateProviderDuty(dutyID, providerDutyDTO);
            return ResponseEntity.status(HttpStatus.OK).body(updatedDuty);
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("{dutyID}/delete")
    @Operation(
        summary = "Удаление услуги",
        description = "Позволяет модератору удалить услугу провайдера"
    )
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<String> deleteProviderDuty(@PathVariable("dutyID") int dutyID) {
        try{
            providerDutyService.deleteProviderDuty(dutyID);
            return ResponseEntity.status(HttpStatus.OK).body("Услуга " + dutyID + " удалена");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{dutyID}/add")
    @Operation(
        summary = "Добавление в заявку",
        description = "Позволяет пользователю добавить услугу в заявку"
    )
    public ResponseEntity<?> addProviderDutyToRequest(@PathVariable("dutyID") int dutyID) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return ResponseEntity.status(HttpStatus.OK).body(providerDutyService.addProviderDutyToRequest(dutyID, auth.getName()));
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при добавлении услуги в заявку: " + e.getMessage());
        }
    }

    @PostMapping("/{dutyID}/image")
    @Operation(
        summary = "Добавление изображения услуги",
        description = "Позволяет модератору добавить изображение для услуги"
    )
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ResponseEntity<String> addImageToProviderDuty(@PathVariable("dutyID") int dutyID, @RequestParam("file") MultipartFile file) {
        try {
            providerDutyService.addImageToProviderDuty(dutyID, file);
            return ResponseEntity.status(HttpStatus.OK).body("Картинка была успешно добавлена/изменена");
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при добавлении/изменении изображения услуги: " + e.getMessage());
        }
    }
}
