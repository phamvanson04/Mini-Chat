package com.minichat.chat.repository;

import com.minichat.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
	List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);
	Page<ChatMessage> findByRoomId(String roomId, Pageable pageable);
	List<ChatMessage> findByRoomIdOrderByIdDesc(String roomId, Pageable pageable);
	List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(
			String roomId,
			String id,
			Pageable pageable
	);
	List<ChatMessage> findByRoomIdAndIdGreaterThanOrderByIdAsc(
			String roomId,
			String id,
			Pageable pageable
	);
}

