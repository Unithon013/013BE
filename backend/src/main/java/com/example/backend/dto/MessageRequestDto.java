package com.example.backend.dto;

import com.example.backend.entity.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDto {
    private String messageContent;
    private Message.MessageType messageType;
}
