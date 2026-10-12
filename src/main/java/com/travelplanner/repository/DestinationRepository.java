package com.travelplanner.repository;
import com.travelplanner.entity.Destination; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param; import java.util.*;
public interface DestinationRepository extends JpaRepository<Destination,Long>{ Optional<Destination> findByNameIgnoreCase(String name);
 @Query("select d from Destination d where lower(d.name) like lower(concat('%',:q,'%')) or lower(d.location) like lower(concat('%',:q,'%')) or lower(d.type) like lower(concat('%',:q,'%')) or lower(d.interests) like lower(concat('%',:q,'%')) or lower(d.highlights) like lower(concat('%',:q,'%'))") List<Destination> search(@Param("q") String q);
}
