package ssafy.nawanolza.server.dto;

import lombok.Data;
import ssafy.nawanolza.server.domain.entity.dto.HideAndSeekProperties;

@Data
public class CreateGameRequest {
    private Long hostId;
    private double lat;
    private double lng;
    private long playTime = 1000 * 60 * 10L;
    private long hideTime = 1000 * 60;
    private int range;

    public HideAndSeekProperties makeHideAndSeekProperties() {
        return new HideAndSeekProperties(lat, lng, playTime, hideTime, range);
    }
}
