package io.factorialsystems.communications.mapper;

import io.factorialsystems.communications.model.dto.response.EmailMessageResponse;
import io.factorialsystems.communications.model.entity.EmailMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmailMessageMapper {

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    EmailMessageResponse toResponse(EmailMessage emailMessage);
}
