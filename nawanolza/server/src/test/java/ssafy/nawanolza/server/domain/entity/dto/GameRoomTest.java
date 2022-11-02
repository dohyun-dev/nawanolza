package ssafy.nawanolza.server.domain.entity.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ssafy.nawanolza.server.domain.utils.CreateRoomUtil;

import static org.junit.jupiter.api.Assertions.*;

class GameRoomTest {

    @Test
    void getEntryCode() {
        HideAndSeekGameRoom hideAndSeekGameRoom = HideAndSeekGameRoom.create(null, null, null);
        String entryCode = hideAndSeekGameRoom.getEntryCode();
        Assertions.assertThat(entryCode.matches("\\d{4}")).isTrue();
    }
}