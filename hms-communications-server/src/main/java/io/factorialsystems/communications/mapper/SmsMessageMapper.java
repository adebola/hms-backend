package io.factorialsystems.communications.mapper;

import io.factorialsystems.communications.model.dto.response.SmsMessageResponse;
import io.factorialsystems.communications.model.entity.SmsMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SmsMessageMapper {

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    SmsMessageResponse toResponse(SmsMessage smsMessage);
}
