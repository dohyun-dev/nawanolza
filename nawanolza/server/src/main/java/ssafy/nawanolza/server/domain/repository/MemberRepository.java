package ssafy.nawanolza.server.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ssafy.nawanolza.server.domain.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Query("select m from Member m where m.id in :memberIds")
    List<Member> findAllByMemberId(@Param("memberIds") List<Long> memberIds);
}
