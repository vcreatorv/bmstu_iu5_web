package com.valer.rip.lab1.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.valer.rip.lab1.config.MinioConfig;
import com.valer.rip.lab1.dto.ConnectionRequestDTO;
import com.valer.rip.lab1.dto.ProviderDutyDTO;
import com.valer.rip.lab1.models.ConnectionRequest;
import com.valer.rip.lab1.models.DutyRequest;
import com.valer.rip.lab1.models.ProviderDuty;
import com.valer.rip.lab1.models.User;
import com.valer.rip.lab1.repositories.ConnectionRequestRepository;
import com.valer.rip.lab1.repositories.DutyRequestRepository;
import com.valer.rip.lab1.repositories.ProviderDutyRepository;
import com.valer.rip.lab1.repositories.UserRepository;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;


@Service
public class ProviderDutyService {

    private final ProviderDutyRepository providerDutyRepository;
    private final ConnectionRequestRepository connectionRequestRepository;
    private final ConnectionRequestService connectionRequestService;
    private final DutyRequestRepository dutyRequestRepository;
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final ModelMapper modelMapper;


    public ProviderDutyService(ProviderDutyRepository providerDutyRepository, 
                                ConnectionRequestRepository connectionRequestRepository, 
                                DutyRequestRepository dutyRequestRepository,
                                ConnectionRequestService connectionRequestService,
                                UserRepository userRepository,
                                MinioClient minioClient,
                                MinioConfig minioConfig, 
                                ModelMapper modelMapper) {
        this.providerDutyRepository = providerDutyRepository;
        this.connectionRequestRepository = connectionRequestRepository;
        this.dutyRequestRepository = dutyRequestRepository;
        this.connectionRequestService = connectionRequestService;
        this.minioClient = minioClient;
        this.userRepository = userRepository;
        this.minioConfig = minioConfig;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void setupMapper() {
        modelMapper.getConfiguration()
        .setSkipNullEnabled(true)
        .setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(ProviderDutyDTO.class, ProviderDuty.class)
            .addMappings(mapper -> mapper.skip(ProviderDuty::setId));

        modelMapper.createTypeMap(ProviderDuty.class, ProviderDutyDTO.class);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAllProviderDuties(String titleFilter, String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("cartSize", 0);
        response.put("connectionRequestID", 0);

        if (username != null) {
            User user = userRepository.findByLogin(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            ConnectionRequest connectionRequest = connectionRequestRepository.findFirstByClientAndStatus(user, "DRAFT").orElse(null);

            if(connectionRequest != null) {
                response.put("connectionRequestID", connectionRequest.getId());
                response.put("cartSize", dutyRequestRepository.findByConnectionRequestEquals(connectionRequest).size());
            }
        }

        List<ProviderDuty> providerDuties;
        if (titleFilter != null) {
            providerDuties = providerDutyRepository.findByTitleOrderById(titleFilter);
        } else {
            providerDuties = providerDutyRepository.findByActiveTrueOrderById();
        }
    
        response.put("providerDuties", providerDuties);
        
        return response;
    }

    @Transactional(readOnly = true)
    public ProviderDuty getProviderDutyById(int dutyID) throws Exception {
        ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
                .orElseThrow(() -> new Exception("Услуга с ID " + dutyID + " не найдена"));
        
        if (providerDuty.getActive()){
            return providerDutyRepository.findById(dutyID).get();
        }
        else {
            throw new Exception("Услуга с ID " + dutyID + " не найдена");
        }
    }

    @Transactional
    public ProviderDuty createProviderDuty(ProviderDuty providerDuty) throws Exception {
        try {
            // ProviderDuty providerDuty = new ProviderDuty();
            // modelMapper.map(providerDutyDTO, providerDuty);
            return providerDutyRepository.save(providerDuty);
        } 
        catch (DataIntegrityViolationException e) {
            throw new Exception("Ошибка целостности данных: возможно, такая услуга уже существует:");
        } 
        catch (Exception e) {
            throw new Exception("Произошла ошибка при сохранении услуги");
        }
    }

    // @Transactional
    // public ProviderDuty createProviderDuty(ProviderDutyDTO providerDutyDTO) throws Exception {
    //     try {
    //         ProviderDuty providerDuty = new ProviderDuty();
    //         modelMapper.map(providerDutyDTO, providerDuty);
    //         return providerDutyRepository.save(providerDuty);
    //     } 
    //     catch (DataIntegrityViolationException e) {
    //         throw new Exception("Ошибка целостности данных: возможно, такая услуга уже существует:");
    //     } 
    //     catch (Exception e) {
    //         throw new Exception("Произошла ошибка при сохранении услуги");
    //     }
    // }

    @Transactional
    public ProviderDuty updateProviderDuty(int dutyID, ProviderDutyDTO providerDutyDTO) throws Exception {
        ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
                .orElseThrow(() -> new Exception("Услуга с ID " + dutyID + " не найдена"));

        if (!providerDuty.getActive()) {
            throw new Exception("Невозможно обновить неактивную услугу");
        }
        modelMapper.map(providerDutyDTO, providerDuty);
        return providerDutyRepository.save(providerDuty);   
    }


    @Transactional
    public void deleteProviderDuty(int dutyID) throws Exception {
        Optional<ProviderDuty> providerDutyToDelete = providerDutyRepository.findById(dutyID);
        
        if(!providerDutyToDelete.isPresent()) {
            throw new Exception("У провайдера нет такой услуги");
        } 
        else {
            if (providerDutyToDelete.get().getImgUrl() != null) {
                deleteImageFromMinio(providerDutyToDelete.get().getImgUrl());
            }
            providerDutyRepository.deleteById(dutyID);
        }
    }

    private void deleteImageFromMinio(String imageUrl) throws Exception {
        try {
            String objectName = extractObjectNameFromUrl(imageUrl);
            minioClient.removeObject(
            RemoveObjectArgs.builder()
                    .bucket("lab1")
                    .object(objectName)
                    .build());
        } 
        catch (Exception e) {
            throw new Exception("Ошибка при удалении изображения из Minio: " + e.getMessage());
        }
    }

    private String extractObjectNameFromUrl(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }

    @Transactional
    public ConnectionRequestDTO addProviderDutyToRequest(int dutyID, String username) throws Exception {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new Exception("Пользователь не найден"));

        ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
                .orElseThrow(() -> new Exception("Услуга не найдена"));

        ConnectionRequest request = connectionRequestRepository.findFirstByClientAndStatus(user, "DRAFT")
                .orElseGet(() -> {
                    ConnectionRequest newRequest = new ConnectionRequest();
                    newRequest.setClient(user);
                    newRequest.setStatus("DRAFT");
                    newRequest.setCreationDatetime(LocalDateTime.now());
                    return connectionRequestRepository.save(newRequest);
                });

        List<DutyRequest> dutyRequests = dutyRequestRepository.findByConnectionRequestEquals(request);

        boolean dutyExists = dutyRequests.stream()
                .anyMatch(dr -> dr.getProviderDuty().getId() == dutyID);

        if (!dutyExists) {
            DutyRequest dutyRequest = new DutyRequest();
            dutyRequest.setProviderDuty(providerDuty);
            dutyRequest.setConnectionRequest(request);
            dutyRequest.setAmount(1);
            dutyRequestRepository.save(dutyRequest);
            return connectionRequestService.convertToDTO(connectionRequestRepository.save(request), true);
        }

        return connectionRequestService.convertToDTO(request, true);
    }

    @Transactional
    public ProviderDuty addImageToProviderDuty(int dutyID, MultipartFile image) throws Exception {
        ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
                .orElseThrow(() -> new Exception("Услуга не найдена"));

        String fileName = providerDuty.getId() + getFileExtension(image.getOriginalFilename());

        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(image.getInputStream(), image.getSize(), -1)
                    .contentType(image.getContentType())
                    .build()
            );

            String imageUrl = minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + fileName;
            providerDuty.setImgUrl(imageUrl);
            return providerDutyRepository.save(providerDuty);
        } 
        catch (Exception e) {
            throw new Exception("Ошибка при загрузке изображения: " + e.getMessage());
        }
    }

    public ProviderDutyDTO convertToDTO(ProviderDuty providerDuty) {
        return modelMapper.map(providerDuty, ProviderDutyDTO.class);
    }


    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

}
