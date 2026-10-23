package com.namelist.repository;

import com.namelist.model.Name;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Access to the names table. Spring Data JPA generates the implementation at startup.
 * The rename and delete queries are single bulk statements, so every row with the given name is affected at once.
 */
public interface NameRepository extends JpaRepository<Name, Long> {

    /**
     * @return how many rows were renamed
     */
    @Transactional
    @Modifying
    @Query("UPDATE Name n SET n.name = :newName WHERE n.name = :currentName")
    int renameAll(String currentName, String newName);

    /**
     * @return how many rows were deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Name n WHERE n.name = :name")
    int deleteAllByName(String name);
}
