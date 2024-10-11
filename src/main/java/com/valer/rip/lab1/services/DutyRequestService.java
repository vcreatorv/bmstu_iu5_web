package com.valer.rip.lab1.services;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.valer.rip.lab1.dto.DutyRequestDTO;
import com.valer.rip.lab1.dto.ProviderDutyDTO;
import com.valer.rip.lab1.models.ConnectionRequest;
import com.valer.rip.lab1.models.DutyRequest;
import com.valer.rip.lab1.models.ProviderDuty;
import com.valer.rip.lab1.repositories.ConnectionRequestRepository;
import com.valer.rip.lab1.repositories.DutyRequestRepository;
import com.valer.rip.lab1.repositories.ProviderDutyRepository;


@Service
public class DutyRequestService {
    private final ProviderDutyRepository providerDutyRepository;
    private final ConnectionRequestRepository connectionRequestRepository;
    private final DutyRequestRepository dutyRequestRepository;
    private final ModelMapper modelMapper;

    private final ProviderDutyService providerDutyService;
    private final ConnectionRequestService connectionRequestService;

    public DutyRequestService(ProviderDutyRepository providerDutyRepository, 
                                ConnectionRequestRepository connectionRequestRepository, 
                                DutyRequestRepository dutyRequestRepository,
                                ProviderDutyService providerDutyService,
                                ConnectionRequestService connectionRequestService,
                                ModelMapper modelMapper) {
        this.providerDutyRepository = providerDutyRepository;
        this.connectionRequestRepository = connectionRequestRepository;
        this.dutyRequestRepository = dutyRequestRepository;
        this.providerDutyService = providerDutyService;
        this.connectionRequestService = connectionRequestService;
        this.modelMapper = modelMapper;
    }

    // @PostConstruct
    // public void setupMapper() {
    //     modelMapper.getConfiguration()
    //         .setSkipNullEnabled(true)
    //         .setMatchingStrategy(MatchingStrategies.STRICT);
        
    //     TypeMap<DutyRequest, DutyRequestDTO> dutyRequestMap = modelMapper.createTypeMap(DutyRequest.class, DutyRequestDTO.class);
    //     dutyRequestMap.addMappings(mapper -> {
    //         mapper.map(DutyRequest::getId, DutyRequestDTO::setId);
    //         mapper.map(DutyRequest::getAmount, (dest, v) -> {
    //             if (dest.getProviderDuty() != null && v != null) {
    //                 dest.getProviderDuty().setAmount((Integer) v);
    //             }
    //         });
    //     });
    
    //     TypeMap<ConnectionRequest, ConnectionRequestDTO> connectionRequestMap = modelMapper.createTypeMap(ConnectionRequest.class, ConnectionRequestDTO.class);
    //     connectionRequestMap.addMappings(mapper -> {
    //         mapper.map(src -> src.getManager() != null ? src.getManager().getLogin() : null, ConnectionRequestDTO::setManager);
    //         mapper.map(src -> src.getClient() != null ? src.getClient().getLogin() : null, ConnectionRequestDTO::setClient);
    //     });
    
    //     TypeMap<ProviderDuty, ProviderDutyDTO> providerDutyMap = modelMapper.createTypeMap(ProviderDuty.class, ProviderDutyDTO.class);
    //     providerDutyMap.addMappings(mapper -> {
    //         mapper.map(ProviderDuty::getActive, ProviderDutyDTO::setActive);
    //         mapper.map(ProviderDuty::getMonthlyPayment, ProviderDutyDTO::setMonthlyPayment);
    //     });
    // }

    @Transactional
    public void deleteProviderDutyFromConnectionRequest(int dutyID, int requestID) throws Exception {
        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
                .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));
        
        if ("COMPLETED".equals(connectionRequest.getStatus()) || "REJECTED".equals(connectionRequest.getStatus()) || "DELETED".equals(connectionRequest.getStatus())) {
            throw new Exception("Нельзя удалить услуги из завершенной или отклоненной заявки");
        }

        ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
                .orElseThrow(() -> new Exception("Услуга с ID " + dutyID + " не найдена"));

        dutyRequestRepository.deleteByConnectionRequestAndProviderDuty(connectionRequest, providerDuty);
    }


    // @Transactional
    // public DutyRequest updateAmountInDutyRequest(int dutyID, int requestID, int amount) throws Exception {
    //     ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
    //             .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));
        
    //     if ("COMPLETED".equals(connectionRequest.getStatus()) || "REJECTED".equals(connectionRequest.getStatus()) || "DELETED".equals(connectionRequest.getStatus())) {
    //         throw new Exception("Нельзя удалить услуги из завершенной или отклоненной заявки");
    //     }

    //     ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
    //             .orElseThrow(() -> new Exception("Услуга с ID " + dutyID + " не найдена"));

    //     DutyRequest dutyRequest = dutyRequestRepository.findByConnectionRequestAndProviderDuty(connectionRequest, providerDuty)
    //         .orElseThrow(() -> new Exception("Заявки с такой услугой не существует"));
        
    //     dutyRequest.setAmount(amount);
    //     return dutyRequest;
    // }

    @Transactional
    public DutyRequestDTO updateAmountInDutyRequest(int dutyID, int requestID, int amount) throws Exception {
        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestID)
                .orElseThrow(() -> new Exception("Заявка на подключение с ID " + requestID + " не найдена"));
        
        if ("COMPLETED".equals(connectionRequest.getStatus()) || 
            "REJECTED".equals(connectionRequest.getStatus()) || 
            "DELETED".equals(connectionRequest.getStatus())) {
            throw new Exception("Нельзя изменить услуги в завершенной или отклоненной заявке");
        }

        ProviderDuty providerDuty = providerDutyRepository.findById(dutyID)
                .orElseThrow(() -> new Exception("Услуга с ID " + dutyID + " не найдена"));

        DutyRequest dutyRequest = dutyRequestRepository.findByConnectionRequestAndProviderDuty(connectionRequest, providerDuty)
            .orElseThrow(() -> new Exception("Заявки с такой услугой не существует"));
        
        dutyRequest.setAmount(amount);
        DutyRequest updatedDutyRequest = dutyRequestRepository.save(dutyRequest);
        
        return convertToDTO(updatedDutyRequest);
    }

    public DutyRequestDTO convertToDTO(DutyRequest dutyRequest) {

        DutyRequestDTO dto = modelMapper.map(dutyRequest, DutyRequestDTO.class);
        
        dto.setConnectionRequest(connectionRequestService.convertToDTO(dutyRequest.getConnectionRequest(), false));
        
        dto.setProviderDuty(providerDutyService.convertToDTO(dutyRequest.getProviderDuty()));
       

        if (dto.getProviderDuty() != null) {
            dto.getProviderDuty().setAmount(dutyRequest.getAmount());
        }
        
        return dto;
    }
}
