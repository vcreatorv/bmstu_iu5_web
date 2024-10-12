package com.valer.rip.lab1.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.Converter;

import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.valer.rip.lab1.dto.ConnectionRequestDTO;
import com.valer.rip.lab1.dto.ProviderDutyDTO;
import com.valer.rip.lab1.models.ConnectionRequest;
import com.valer.rip.lab1.models.DutyRequest;
import com.valer.rip.lab1.models.User;
import com.valer.rip.lab1.repositories.ConnectionRequestRepository;
import com.valer.rip.lab1.repositories.DutyRequestRepository;

import jakarta.annotation.PostConstruct;


@Service
public class ConnectionRequestService {
    private final ConnectionRequestRepository connectionRequestRepository;
    private final DutyRequestRepository dutyRequestRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public ConnectionRequestService(ConnectionRequestRepository connectionRequestRepository, 
                                    UserService userService,
                                    DutyRequestRepository dutyRequestRepository,
                                    ModelMapper modelMapper) {
        this.connectionRequestRepository = connectionRequestRepository;
        this.userService = userService;
        this.dutyRequestRepository = dutyRequestRepository;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    public void setupMapper() {
        modelMapper.getConfiguration()
        .setSkipNullEnabled(true)
        .setMatchingStrategy(MatchingStrategies.STRICT);
        
        modelMapper.createTypeMap(ConnectionRequest.class, ConnectionRequestDTO.class)
        .addMappings(mapper -> {
            // mapper.map(src -> src.getManager() != null ? src.getManager().getLogin() : null, ConnectionRequestDTO::setManager);
            // mapper.map(src -> src.getClient() != null ? src.getClient().getLogin() : null, ConnectionRequestDTO::setClient);
            mapper.skip(ConnectionRequestDTO::setManager);
            mapper.skip(ConnectionRequestDTO::setClient);
        });

        modelMapper.createTypeMap(ConnectionRequestDTO.class, ConnectionRequest.class)
            .addMappings(mapper -> {
                mapper.skip(ConnectionRequest::setId);
                mapper.skip(ConnectionRequest::setStatus);
                mapper.skip(ConnectionRequest::setCreationDatetime);
                mapper.skip(ConnectionRequest::setFormationDatetime);
                mapper.skip(ConnectionRequest::setCompletionDatetime);
                mapper.skip(ConnectionRequest::setTotalPrice);
                mapper.skip(ConnectionRequest::setManager);
                mapper.skip(ConnectionRequest::setClient);
            });

        
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestDTO> getAllConnectionRequests() {
        List<ConnectionRequest> requests = connectionRequestRepository.findAllExceptDeletedAndDraft();
        return requests.stream()
            .map(request -> convertToDTO(request, false))
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteConnectionRequest(int requestID) throws Exception {
        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
                .orElseThrow(() -> new Exception("Такой заявки на подключение нет"));
        connectionRequest.setStatus("DELETED");
    }

    @Transactional(readOnly = true)
    public ConnectionRequestDTO getConnectionRequestById(int requestID) throws Exception {
        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
                .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));
        
        if ("DELETED".equals(connectionRequest.getStatus()) || "DRAFT".equals(connectionRequest.getStatus())) {
            throw new Exception("Заявка либо удалена, либо является черновиком");
        }
        
        return convertToDTO(connectionRequest, true);
    }

    @Transactional
    public ConnectionRequestDTO updateConnectionRequest(int requestID, ConnectionRequestDTO requestDTO) throws Exception {
        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
                .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));

        if ("DELETED".equals(connectionRequest.getStatus())) {
            throw new Exception("Невозможно обновить удаленную заявку");
        }

        modelMapper.map(requestDTO, connectionRequest);
        ConnectionRequest updatedRequest = connectionRequestRepository.save(connectionRequest);
        return convertToDTO(updatedRequest, false);
    }

    @Transactional
    public ConnectionRequestDTO formConnectionRequest(int requestID) throws Exception {
        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
                .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));

        if (!"DRAFT".equals(connectionRequest.getStatus())) {
            throw new Exception("Заявка не является черновиком");
        }

        if (connectionRequest.getConsumer() != null && connectionRequest.getPhoneNumber() != null) {
            connectionRequest.setStatus("FORMED");
            connectionRequest.setFormationDatetime(LocalDateTime.now());
            ConnectionRequest updatedRequest = connectionRequestRepository.save(connectionRequest);
            return convertToDTO(updatedRequest, false);
        } 
        else {
            throw new Exception("Поля consumer и phoneNumber должны быть заполнены");
        }
    }

    // @Transactional
    // public ConnectionRequestDTO closeConnectionRequest(int requestID, String status) throws Exception {
    //     ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));

    //     if (!"FORMED".equals(connectionRequest.getStatus())) {
    //         throw new Exception("Заявка не сформирована");
    //     }

    //     int userID = userService.getUserID();
    //     User user = userService.findById(userID).get();
    //     connectionRequest.setManager(user);

    //     connectionRequest.setStatus(status);
    //     connectionRequest.setCompletionDatetime(LocalDateTime.now());
    //     connectionRequest.setTotalPrice(countTotalPrice(connectionRequest));
        
    //     ConnectionRequest updatedRequest = connectionRequestRepository.save(connectionRequest);
    //     return convertToDTO(updatedRequest, false);
    // }

    public int countTotalPrice(ConnectionRequest connectionRequest) {
        List<DutyRequest> dutyRequestList =  dutyRequestRepository.findByConnectionRequestEquals(connectionRequest);
        return dutyRequestList.stream()
            .mapToInt(dutyRequest -> dutyRequest.getAmount() * dutyRequest.getProviderDuty().getPrice())
            .sum();
    }

    public ConnectionRequestDTO convertToDTO(ConnectionRequest connectionRequest, boolean includeProviderDuties) {
       
        ConnectionRequestDTO dto = modelMapper.map(connectionRequest, ConnectionRequestDTO.class);

        if (connectionRequest.getManager() != null) {
            dto.setManager(connectionRequest.getManager().getLogin());
        }
        if (connectionRequest.getClient() != null) {
            dto.setClient(connectionRequest.getClient().getLogin());
        }
        
        if (includeProviderDuties) {
            List<ProviderDutyDTO> dutyDtoList = dutyRequestRepository.findByConnectionRequestEquals(connectionRequest)
                .stream()
                .map(dutyRequest -> {
                    ProviderDutyDTO providerDutyDTO = new ProviderDutyDTO();
                    BeanUtils.copyProperties(dutyRequest.getProviderDuty(), providerDutyDTO);
                    providerDutyDTO.setAmount(dutyRequest.getAmount());
                    return providerDutyDTO;
                })
                .collect(Collectors.toList());

            dto.setDuties(dutyDtoList);
        }
        return dto;
        
        // ConnectionRequestDTO dto = modelMapper.map(connectionRequest, ConnectionRequestDTO.class);
    
        // if (connectionRequest.getManager() != null) {
        //     dto.setManager(connectionRequest.getManager().getLogin());
        // }
        // if (connectionRequest.getClient() != null) {
        //     dto.setClient(connectionRequest.getClient().getLogin());
        // }
        
        // List<ProviderDutyDTO> dutyDtoList = dutyRequestRepository.findByConnectionRequestEquals(connectionRequest)
        //     .stream()
        //     .map(dutyRequest -> {
        //         ProviderDutyDTO providerDutyDTO = new ProviderDutyDTO();
        //         BeanUtils.copyProperties(dutyRequest.getProviderDuty(), providerDutyDTO);
        //         providerDutyDTO.setAmount(dutyRequest.getAmount());
        //         return providerDutyDTO;
        //     })
        //     .collect(Collectors.toList());

        // dto.setDuties(dutyDtoList);

        // return dto;



         // ConnectionRequestDTO dto = modelMapper.map(connectionRequest, ConnectionRequestDTO.class);
        
        // List<ProviderDutyDTO> dutyDtoList = dutyRequestRepository.findByConnectionRequestEquals(connectionRequest)
        //     .stream()
        //     .map(dutyRequest -> {
        //         ProviderDutyDTO providerDutyDTO = new ProviderDutyDTO();
        //         BeanUtils.copyProperties(dutyRequest.getProviderDuty(), providerDutyDTO);
        //         providerDutyDTO.setAmount(dutyRequest.getAmount());
        //         return providerDutyDTO;
        //     })
        //     .collect(Collectors.toList());

        // dto.setDuties(dutyDtoList);

        // return dto;
    }

    // @PostConstruct
    // public void setupMapper() {
    //     modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        
    //     modelMapper.createTypeMap(ConnectionRequestDTO.class, ConnectionRequest.class)
    //         .addMappings(mapper -> {
    //             mapper.skip(ConnectionRequest::setId);
    //             mapper.skip(ConnectionRequest::setStatus);
    //             mapper.skip(ConnectionRequest::setCreationDatetime);
    //             mapper.skip(ConnectionRequest::setFormationDatetime);
    //             mapper.skip(ConnectionRequest::setCompletionDatetime);
    //             mapper.skip(ConnectionRequest::setTotalPrice);
    //             mapper.skip(ConnectionRequest::setManager);
    //             mapper.skip(ConnectionRequest::setClient);
    //         });
    // }

    // @Transactional(readOnly = true)
    // public List<ConnectionRequest> getAllConnectionRequests() {
    //     return connectionRequestRepository.findAllExceptDeletedAndDraft();
    // }

    // @Transactional
    // public void deleteConnectionRequest(int requestID) throws Exception {
    //     ConnectionRequest connectionRequest= connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Такой заявки на подключение нет"));
    //     connectionRequest.setStatus("DELETED");
    // }

    // @Transactional(readOnly = true)
    // public ConnectionRequestDTO getConnectionRequestById(int requestID) throws Exception {
    //     ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));
        
    //     if ("DELETED".equals(connectionRequest.getStatus()) || "DRAFT".equals(connectionRequest.getStatus())) {
    //         throw new Exception("Заявка либо удалена, либо является черновиком");
    //     }
        
    //     ConnectionRequestDTO response = new ConnectionRequestDTO();

    //     return initDTO(response, connectionRequest);
    // }

    // @Transactional
    // public ConnectionRequest updateConnectionRequest(int requestID, ConnectionRequestDTO requestDTO) throws Exception {
    //     ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));

    //     if ("DELETED".equals(connectionRequest.getStatus()) || "DRAFT".equals(connectionRequest.getStatus())) {
    //         throw new Exception("Невозможно обновить удаленную заявку или черновик");
    //     }

    //     modelMapper.map(requestDTO, connectionRequest);

    //     return connectionRequestRepository.save(connectionRequest);
    // }

    // @Transactional
    // public ConnectionRequest formConnectionRequest(int requestID) throws Exception {
    //     ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));

    //     if (!"DRAFT".equals(connectionRequest.getStatus())) {
    //         throw new Exception("Заявка не является черновиком");
    //     }

    //     if (connectionRequest.getConsumer() != null && connectionRequest.getPhoneNumber() != null) {
    //         connectionRequest.setStatus("FORMED");
    //         connectionRequest.setFormationDatetime(LocalDateTime.now());
    //         return connectionRequest;
    //     } 
    //     else {
    //         throw new Exception("Поля consumer и phoneNumber должны быть заполнены");
    //     }
    // }

    // @Transactional
    // public ConnectionRequest closeConnectionRequest(int requestID, String status) throws Exception {
    //     ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));

    //     if (!"FORMED".equals(connectionRequest.getStatus())) {
    //         throw new Exception("Заявка не сформирована");
    //     }

    //     int userID = userService.getUserID();
    //     User user = userService.findById(userID).get();
    //     connectionRequest.setManager(user);

    //     connectionRequest.setStatus(status);
    //     connectionRequest.setCompletionDatetime(LocalDateTime.now());
    //     connectionRequest.setTotalPrice(countTotalPrice(connectionRequest));
        
    //     return connectionRequest;
    // }


    // public int countTotalPrice(ConnectionRequest connectionRequest) {
    //     List<DutyRequest> dutyRequestList =  dutyRequestRepository.findByConnectionRequestEquals(connectionRequest);
    //     return dutyRequestList.stream()
    //         .mapToInt(dutyRequest -> dutyRequest.getAmount() * dutyRequest.getProviderDuty().getPrice())
    //         .sum();
    // }

    // public ConnectionRequestDTO initDTO(ConnectionRequestDTO response, ConnectionRequest connectionRequest) {
    //     BeanUtils.copyProperties(connectionRequest, response);

    //     // if (connectionRequest.getManager() != null) {
    //     //     response.setManagerId(connectionRequest.getManager().getId());
    //     // }
    //     // response.setClientId(connectionRequest.getClient().getId());

    //     List<ProviderDutyDTO> dutyDtoList = dutyRequestRepository.findByConnectionRequestEquals(connectionRequest)
    //         .stream()
    //         .map(dutyRequest -> {
    //             ProviderDutyDTO providerDutyDTO = new ProviderDutyDTO();
    //             BeanUtils.copyProperties(dutyRequest.getProviderDuty(), providerDutyDTO);
    //             providerDutyDTO.setAmount(dutyRequest.getAmount());
    //             return providerDutyDTO;
    //         })
    //         .collect(Collectors.toList());

    //     response.setDuties(dutyDtoList);

    //     return response;
    // }
}
