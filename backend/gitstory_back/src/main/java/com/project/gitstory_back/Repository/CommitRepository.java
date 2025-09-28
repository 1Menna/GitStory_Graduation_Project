package com.project.gitstory_back.Repository;

import com.project.gitstory_back.Models.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommitRepository extends JpaRepository<Commit, UUID> {

    List<Commit> findByMessageContainingIgnoreCase(String keyword);


}
