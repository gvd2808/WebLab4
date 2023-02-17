package ru.combyte.jsonSended.answers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import ru.combyte.Utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.combyte.enitities.Shot;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShotAnswer {
    @JsonProperty("hit")
    Boolean hit;
    // see getDateAsISO8601
    @JsonProperty("datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Date datetime;
    @JsonProperty("processing_time_nano")
    Long processingTimeNano;

    @JsonProperty("wrong_type")
    @Setter(AccessLevel.PROTECTED) List<String> wrongTypeValues;
    @JsonProperty("wrong_value")
    @Setter(AccessLevel.PROTECTED) List<String> wrongValueValues;

    /**
     * wrongTypeValues remains null
     * @throws NullPointerException if hit, datetime or processingTimeNano is null
     */
    public ShotAnswer(@NonNull Shot shot) {
        this(shot.getHit(), shot.getDatetime(), shot.getProcessingTimeNano());
    }

    /**
     * wrongTypeValues remains null
     * @throws NullPointerException if datetime is null
     */
    public ShotAnswer(boolean hit, @NonNull Date datetime, long processingTimeNano) {
        this.hit = hit;
        this.datetime = datetime;
        this.processingTimeNano = processingTimeNano;
    }

    /**
     * hit, datetime, processingTimeNano and wrongValueValue remain null
     * @throws NullPointerException if wrongTypeValues is null
     * @throws IllegalArgumentException if wrongTypeValues is empty
     */
    public static ShotAnswer initWrongTypeValuesAnswer(@NonNull List<String> wrongTypeValues) {
        var shotAnswer = new ShotAnswer();
        shotAnswer.setWrongTypeValues(wrongTypeValues);
        return shotAnswer;
    }

    /**
     * hit, datetime, processingTimeNano and wrongTypeValues remain null
     * @throws NullPointerException if wrongValueValues is null
     * @throws IllegalArgumentException if wrongValueValues is empty
     */
    public static ShotAnswer initWrongValueValuesAnswer(@NonNull List<String> wrongValueValues) {
        var shotAnswer = new ShotAnswer();
        shotAnswer.setWrongValueValues(wrongValueValues);
        return shotAnswer;
    }
}
