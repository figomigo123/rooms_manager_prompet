package com.roomsmanager.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usage")
@CompoundIndexes({
    @CompoundIndex(name = "appId_date_idx", def = "{'appId': 1, 'date': -1}"),
    @CompoundIndex(name = "roomId_userId_idx", def = "{'roomId': 1, 'userId': 1}")
})
public class Usage {

    @Id
    private String id;

    @Indexed
    private String appId;

    @Indexed
    private String roomId;

    @Indexed
    private String userId;

    private double minutes;

    @Indexed
    private LocalDate date;

    private long createdAt = System.currentTimeMillis();
}
