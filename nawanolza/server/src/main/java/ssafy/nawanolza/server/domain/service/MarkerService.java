package ssafy.nawanolza.server.domain.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ssafy.nawanolza.server.config.MarkerConfig;
import ssafy.nawanolza.server.domain.entity.Character;
import ssafy.nawanolza.server.domain.entity.Game;
import ssafy.nawanolza.server.domain.entity.dto.Marker;
import ssafy.nawanolza.server.domain.repository.MapCharacterRedisRepository;
import ssafy.nawanolza.server.domain.repository.RedisLockRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
@Slf4j
@RequiredArgsConstructor
public class MarkerService {

    private final GameService gameService;
    private final CharacterService characterService;
    private final MapCharacterRedisRepository mapCharacterRedisRepository;
    private final RedisLockRepository redisLockRepository;
//    static int flag = 0;

    /*
     * true : 퀘스트 시작, 해당 마커 락 걸림
     * false : 퀘스트 시작 X, 해당 마커 다른 사람이 락 걸어놓음
     * */
    public boolean questStart(Long key) throws InterruptedException {

        while (!redisLockRepository.lock(key)) {
            Thread.sleep(100);
        }
        Marker marker = mapCharacterRedisRepository.findById(key).orElseThrow();
        marker.increase();
        mapCharacterRedisRepository.save(marker);
        System.out.println("저장 후 : " + marker.getIsPlayGame());
        redisLockRepository.unLock(key);
        return false;
    }

    /*
     * true : 락 해제
     * false : 락이 이미 해제됨, 에러반환 해야함
     * */
    public boolean questSuccess(Long key) throws InterruptedException {
        redisLockRepository.lock(key);
        Thread.sleep(1000);
        redisLockRepository.unLock(key);
        return redisLockRepository.unLock(key);
    }

    /*
     * true : 락 해제
     * false : 락이 이미 해제됨, 에러반환 해야함
     * */
    public boolean questFail(Long key) {
        return redisLockRepository.unLock(key);
    }

    /*
     * 마커 만들기
     * */
    @Scheduled(cron = "0 0 6,18 * * *", zone = "Asia/Seoul")
    public void insertMarker(){
        log.info("!! Marker Update...");

        // 기존 마커 제거
        if (checkMarkerInRedis()){
            mapCharacterRedisRepository.deleteAll();
        }

        List<Character> normalCharacters = characterService.findNormalCharacters();
        List<Character> rareCharacters = characterService.findRareCharacters();
        List<Game> games = gameService.findGame();
        makeMarkers(normalCharacters, rareCharacters, games);
        log.info("Marker Update Success!!");
    }

    public boolean checkMarkerInRedis() {
        long markerCnt = mapCharacterRedisRepository.count();
        return markerCnt == 0 ? false : true;
    }

    public void makeMarkers(List<Character> normalCharacters, List<Character> rareCharacters, List<Game> games) {
        List<Marker> markerList = new ArrayList<>();
        for (int i = 1; i <= MarkerConfig.MAX_COUNT_MARKERS; i++) {
            Character selectCharacter = randomCharacter(rareCharacters, normalCharacters);
            int quest = randomQuest(games.size());

            // 게임이면 게임시간 설정
            int gameTime = 0;
            if (quest > 0)
                gameTime = games.get(quest).getTime();

            // 랜덤 좌표 생성
            LatLng location = getRandomLocation();

            // 마커 생성
            Marker newMarker = Marker.builder().characterId(selectCharacter.getId())
                    .rare(selectCharacter.isRare()).questType(quest)
                    .time(gameTime).lat(location.lat).lng(location.lng).build();

            markerList.add(newMarker);
        }
        mapCharacterRedisRepository.saveAll(markerList);
    }

    /*
     * 랜덤하게 캐릭터 선택
     * 레어 10%, 노말 90% 확률
     * */
    private Character randomCharacter(List<Character> rareCharacters, List<Character> normalCharacters) {
        Random random = new Random();
        int selectNumber = random.nextInt(9);       // 0 : 레어 캐릭터 1~9 : 노말 캐릭터

        if (selectNumber == 0) {
            return rareCharacters.get(random.nextInt(rareCharacters.size()));
        } else {
            return normalCharacters.get(random.nextInt(normalCharacters.size()));
        }
    }

    /*
     * 랜덤하게 퀘스트 타입 선택
     * 0 : 퀴즈 1~N : 게임번호
     * */
    private int randomQuest(int maxCountGame) {
        Random random = new Random();
        return random.nextInt(maxCountGame);
    }

    /*
     * LAT, LNG 좌표 위치에서 MAX_RADIUS 반지름을 가지는 원에서
     * 랜덤한 좌표값을 리턴함
     * LAT, LNG는 구미와 대구 중간 칠곡으로 임의로 정함
     * */
    public LatLng getRandomLocation() {
        double d2r = Math.PI / 180;
        double r2d = 180 / Math.PI;
        double earth_rad = 6378000f; //지구 반지름 근사값(미터 단위)

        double r = new Random().nextInt(MarkerConfig.MAX_RADIUS) + new Random().nextDouble(); // 호의 길이
        double rlat = (r / earth_rad) * r2d;
        double rlng = rlat / Math.cos(MarkerConfig.LAT * d2r);

        double theta = Math.PI * (new Random().nextInt(2) + new Random().nextDouble());
        double y = MarkerConfig.LNG + (rlng * Math.cos(theta));
        double x = MarkerConfig.LAT + (rlat * Math.sin(theta));
        return new LatLng(x, y);
    }

    @AllArgsConstructor
    static class LatLng {
        double lat;
        double lng;
    }

    public static byte[] longToByteArray(long data) {
        return new byte[] {
            (byte)((data >> 56) & 0xff),
            (byte)((data >> 48) & 0xff),
            (byte)((data >> 40) & 0xff),
            (byte)((data >> 32) & 0xff),
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8 ) & 0xff),
            (byte)((data >> 0) & 0xff),
        };
    }
}
